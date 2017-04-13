package com.helospark.mycraft.mycraft.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector3f;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.helospark.mycraft.mycraft.game.LightSource.LitVertex;
import com.helospark.mycraft.mycraft.mathutils.BoundingBox;
import com.helospark.mycraft.mycraft.mathutils.CullingResult;
import com.helospark.mycraft.mycraft.mathutils.IntVector;
import com.helospark.mycraft.mycraft.mathutils.VectorMathUtils;
import com.helospark.mycraft.mycraft.messages.BlockDestroyedMessage;
import com.helospark.mycraft.mycraft.messages.BlockDestroyingMessage;
import com.helospark.mycraft.mycraft.messages.GenericIntegerVectorMessage;
import com.helospark.mycraft.mycraft.raytracer.RayTracerResult;
import com.helospark.mycraft.mycraft.raytracer.RayTracerType;
import com.helospark.mycraft.mycraft.render.RenderableBlockNode;
import com.helospark.mycraft.mycraft.services.RayTracer;
import com.helospark.mycraft.mycraft.services.RayTracerProperty;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageHandler;
import com.helospark.mycraft.mycraft.window.MessageListener;
import com.helospark.mycraft.mycraft.window.MessageTypes;

@Service
public class ChunkOptimizer implements MessageListener {
    private final MessageHandler messager;
    private final GameMap map;

    @Autowired
    RayTracer rayTracer;

    public ChunkOptimizer() {
        ApplicationContext context = Singleton.getInstance().getContext();
        messager = context.getBean(MessageHandler.class);
        messager.registerListener(this, MessageTypes.CHUNK_GENERATED_MESSAGE);
        messager.registerListener(this, MessageTypes.BLOCK_DESTROYED_MESSAGE);
        messager.registerListener(this, MessageTypes.BLOCK_DESTROYING_STARTED);
        messager.registerListener(this, MessageTypes.BLOCK_DESTROYING_ENDED);
        messager.registerListener(this, MessageTypes.BLOCK_PLACEMENT_HANDLED);
        messager.registerListener(this, MessageTypes.BLOCK_DESTRUCTION_HANDLED);
        map = context.getBean(GameMap.class);
    }

    @Override
    public boolean receiveMessage(Message message) {
        if (message.getType() == MessageTypes.CHUNK_GENERATED_MESSAGE) {
            GenericIntegerVectorMessage chunkMessage = (GenericIntegerVectorMessage) message;
            int x = chunkMessage.getX();
            int y = chunkMessage.getY();
            int z = chunkMessage.getZ();
            System.out.println("Generated " + x + " " + y + " " + z);
            Chunk chunk = map.getChunk(x, y, z);
            chunk.clearVisibleObjects();
            for (int i = 0; i < Chunk.CHUNK_BLOCK_SIZE; ++i) {
                for (int j = 0; j < Chunk.CHUNK_BLOCK_SIZE; ++j) {
                    for (int k = 0; k < Chunk.CHUNK_BLOCK_SIZE; ++k) {
                        checkAllBlockSides(chunk, i, j, k);
                        recalculateLightingAfterBlockChangeAtPosition(new IntVector(i, j, k),
                                chunk.getBlock(i, j, k));
                    }
                }
            }
            System.out.println("Optimized " + x + " " + y + " " + z);
            messager.sendMessage(new GenericIntegerVectorMessage(
                    MessageTypes.BLOCK_RENDER_OPTIMIZED, Message.MESSAGE_TARGET_ANYONE, x, y, z));
        } else if (message.getType() == MessageTypes.BLOCK_DESTROYED_MESSAGE) {
            BlockDestroyedMessage blockDestroyed = (BlockDestroyedMessage) message;
            Block block = Blocks.getBlockForId(blockDestroyed.getBlockId());
            int x = blockDestroyed.getX();
            int y = blockDestroyed.getY();
            int z = blockDestroyed.getZ();
            IntVector position = new IntVector(x, y, z);
            BoundingBox boundingBox = blockDestroyed.getBoundingBox();

            removeBlockAtPosition(position, boundingBox, false);

            // if (block.isLightEnabled()) {
            // LightSource lightSource = getLightSourceAtPosition(position);
            // removeLightSourceEffect(lightSource);
            // removeLightSourceFromChunkLists(position);
            // }

            // recalculateLightingAfterBlockChangeAtPosition(position, null);
        } else if (message.getType() == MessageTypes.BLOCK_DESTROYING_STARTED) {
            BlockDestroyingMessage blockMessage = (BlockDestroyingMessage) message;
            IntVector position = blockMessage.getPosition();
            BoundingBox boundingBox = Blocks
                    .getBlockForId(blockMessage.getBlockId())
                    .getBoundingBox()
                    .getBoundingBoxWithPosition(position.x * Block.SIZE, position.y * Block.SIZE,
                            position.z * Block.SIZE);

            removeBlockAtPosition(position, boundingBox, true);
        } else if (message.getType() == MessageTypes.BLOCK_DESTROYING_ENDED) {

            BlockDestroyingMessage blockMessage = (BlockDestroyingMessage) message;
            IntVector position = blockMessage.getPosition();
            handleBlockChange(position);
        } else if (message.getType() == MessageTypes.BLOCK_DESTRUCTION_HANDLED
                || message.getType() == MessageTypes.BLOCK_PLACEMENT_HANDLED) {
            GenericIntegerVectorMessage blockMessage = (GenericIntegerVectorMessage) message;
            IntVector position = new IntVector(blockMessage.getX(), blockMessage.getY(),
                    blockMessage.getZ());
            handleBlockChange(position);
        }
        return false;
    }

    private void handleBlockChange(IntVector position) {
        checkAroundPosition(position);
        Block block = map.getBlockAtPosition(position);
        // recalculateLightingAfterBlockChangeAtPosition(position, block);
    }

    private LightSource getLightSourceAtPosition(IntVector position) {
        Map<String, Chunk> loadedChunks = map.getLoadedChunks();
        LightSource lightSource = null;
        for (Map.Entry<String, Chunk> entry : loadedChunks.entrySet()) {
            lightSource = entry.getValue().getLightSourceAtPosition(position);
            if (lightSource != null) {
                return lightSource;
            }
        }
        return null;
    }

    private void removeLightSourceFromChunkLists(IntVector position) {
        Map<String, Chunk> loadedChunks = map.getLoadedChunks();
        for (Map.Entry<String, Chunk> entry : loadedChunks.entrySet()) {
            entry.getValue().removeLightSourceAtPositionIfContains(position);
        }
    }

    private void checkAroundPosition(IntVector position) {
        Chunk chunk = map.getChunkForPosition(position.x, position.y, position.z);
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                for (int k = -1; k <= 1; ++k) {
                    checkAllBlockSides(chunk, position.x + i, position.y + j, position.z + k);
                }
            }
        }
    }

    private void addLightingEffect(LightSource lightSource) {
        int lightLength = lightSource.getBlock().getLightDistance();
        RayTracerProperty property = new RayTracerProperty(
                new RayTracerType[] { RayTracerType.BLOCK }, -1);
        property.addIgnored(lightSource.getPosition());
        Vector3f direction = new Vector3f();
        Vector3f lightSourcePosition = lightSource.getPosition().toVector3f();
        lightSourcePosition.x += 0.5f * Block.SIZE;
        lightSourcePosition.y += 0.5f * Block.SIZE;
        lightSourcePosition.z += 0.5f * Block.SIZE;
        for (int i = -lightLength; i <= lightLength; ++i) {
            for (int j = -lightLength; j <= lightLength; ++j) {
                for (int k = -lightLength; k <= lightLength; ++k) {
                    IntVector lightPosition = lightSource.getPosition();
                    IntVector newPosition = new IntVector(lightPosition.x + i, lightPosition.y + j,
                            lightPosition.z + k);
                    Block block = map.getBlockAtPosition(newPosition);
                    doLightingCalculation(lightSource, property, direction, lightSourcePosition,
                            block, newPosition);
                }
            }
        }
    }

    private void doLightingCalculation(LightSource lightSource, RayTracerProperty property,
            Vector3f direction, Vector3f lightSourcePosition, Block block, IntVector blockPosition) {
        if (block.getType() != Blocks.get("Air")) {
            int lightLength = lightSource.getBlock().getLightDistance();
            BlockSide[] sides = getVisibleBlockSides(blockPosition);
            for (int h = 0; h < sides.length; ++h) {
                BlockSide side = sides[h];
                Vector3f[] vertices = side.getNode().getVertices();
                for (int g = 0; g < vertices.length; ++g) {
                    Vector3f.sub(vertices[g], lightSourcePosition, direction);
                    direction.normalise();
                    float distanceSquaredToVertex = VectorMathUtils.distanceSquareBetween(
                            vertices[g], lightSourcePosition);
                    float distanceToVertex = (float) Math.sqrt(distanceSquaredToVertex);
                    RayTracerResult result = rayTracer.traceRays(lightSourcePosition, direction,
                            (int) (distanceToVertex) + 1, property);
                    if (result == null
                            || result.hasFound()
                                    && result.getDistance() >= distanceSquaredToVertex
                                    && result.getDistance() >= distanceSquaredToVertex + 0.1
                                    && result.getSide() == RenderableBlock
                                            .getIdFromSide(sides[h].getSide())) {
                        float lightIntensity = lightSource.getBlock().getLightStrength()
                                * (distanceToVertex / lightSource.getBlock().getLightDistance());
                        // lightIntensity = Math.max(0.0f, 1.0f -
                        // (sides[h].getNode().getLight(g) + lightIntensity));
                        if (lightIntensity + sides[h].getNode().getLight(g) > 1.0f) {
                            lightIntensity = 1.0f - sides[h].getNode().getLight(g);
                        }
                        // System.out.println(lightIntensity);
                        // lightIntensity = 1.0f;
                        sides[h].getNode().addLight(g, lightIntensity);
                        lightSource.addLitVertex(sides[h].getNode(), g, lightIntensity);
                    }
                }
            }
        }
    }

    private BlockSide[] getVisibleBlockSides(IntVector blockPosition) {
        return map.getVisibleBlockSides(blockPosition);
    }

    private void removeLightSourceEffect(LightSource lightSource) {
        List<LitVertex> litVertices = lightSource.getLitVertices();
        for (int i = 0; i < litVertices.size(); ++i) {
            RenderableBlockNode node = litVertices.get(i).getBlockNode();
            int index = litVertices.get(i).getVertexIndex();
            float amount = litVertices.get(i).getLightAmount();
            node.addLight(index, -amount);
        }
        lightSource.clearLitVertices();
    }

    private void recalculateLightingAfterBlockChangeAtPosition(IntVector position, Block block) {
        if (block != null && block.isLightEnabled()) {
            LightSource lightSource = new LightSource(block, position);
            addLighsourceToAllChunkWhichItEffects(lightSource);
        }
        List<LightSource> effectingLightSources = getLightSourcesWhichEffectCurrentPosition(position);
        for (int i = 0; i < effectingLightSources.size(); ++i) {
            removeLightSourceEffect(effectingLightSources.get(i));
        }
        for (int i = 0; i < effectingLightSources.size(); ++i) {
            addLightingEffect(effectingLightSources.get(i));
        }
    }

    private List<LightSource> getLightSourcesWhichEffectCurrentPosition(IntVector position) {
        List<LightSource> result = new ArrayList<LightSource>();
        for (Map.Entry<String, Chunk> entry : map.getLoadedChunks().entrySet()) {
            Chunk chunk = entry.getValue();
            List<LightSource> lightSources = chunk.getLightSources();
            for (int i = 0; i < lightSources.size(); ++i) {
                float lightDistanceSquared = lightSources.get(i).getBlock().getLightDistance();
                lightDistanceSquared *= lightDistanceSquared;
                if (VectorMathUtils.distanceSquareBetween(lightSources.get(i).getPosition(),
                        position) < lightDistanceSquared) {
                    result.add(lightSources.get(i));
                }
            }
        }
        return result;
    }

    private void addLighsourceToAllChunkWhichItEffects(LightSource lightSource) {
        BoundingBox lightBoundingBox = lightSource.getBoundingBox();
        for (Map.Entry<String, Chunk> entry : map.getLoadedChunks().entrySet()) {
            Chunk chunk = entry.getValue();
            if (chunk.getBoundingBox().containsBox(lightBoundingBox) != CullingResult.FULLY_OUT) {
                chunk.addLightSource(lightSource);
            }
        }
    }

    private void removeBlockAtPosition(IntVector position, BoundingBox boundingBox,
            boolean shouldRemove) {
        Chunk chunk = map.getChunkForPosition(position.x, position.y, position.z);
        chunk.removeBlockFromVisible(position.x, position.y, position.z, boundingBox);
        RenderableBlock originalBlock = chunk.getRenderableBlockAt(position.x, position.y,
                position.z);
        chunk.setBlockAt(position, Blocks.get("Air"));
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                for (int k = -1; k <= 1; ++k) {
                    checkAllBlockSides(chunk, position.x + i, position.y + j, position.z + k);
                }
            }
        }
        if (shouldRemove) {
            chunk.setBlock(position.x, position.y, position.z, originalBlock);
        }
    }

    private void checkAllBlockSides(Chunk chunk, int x, int y, int z) {

        Block block = chunk.getBlock(x, y, z);
        if (block.getType() == Blocks.get("Air")) {
            return;
        }

        Vector3f lightPosition = new Vector3f(30, 13, 30);
        float lightDistance = 600f;
        float ambientLight = 0.6f;

        for (int h = 0; h < block.getNumberOfModels(); ++h) {
            if (isBlockSideVisible(chunk, block, x, y, z, h)) {
                BlockSide side = block.generateNewBlockSide(h);
                IntVector intPosition = chunk.getIntPosition();
                int newX = intPosition.x * Chunk.CHUNK_BLOCK_SIZE + x;
                int newY = intPosition.y * Chunk.CHUNK_BLOCK_SIZE + y;
                int newZ = intPosition.z * Chunk.CHUNK_BLOCK_SIZE + z;

                Vector3f position = new Vector3f(newX * Block.SIZE, newY * Block.SIZE, newZ
                        * Block.SIZE);
                side.getNode().setPosition(position);
                float u = (side.getTexturePositionU() * 16.0f) / (256.0f);
                float v = (side.getTexturePositionV() * 16.0f) / (256.0f);
                float dx = 16.0f / (256.0f);
                float dy = 16.0f / (256.0f);
                side.getNode().setTexturePosition(0, u, v);
                side.getNode().setTexturePosition(1, u + dx, v);
                side.getNode().setTexturePosition(2, u + dx, v + dy);
                side.getNode().setTexturePosition(3, u, v + dy);

                float light = ambientLight;
                float distanceSquare = VectorMathUtils.distanceSquareBetween(position,
                        lightPosition);
                if (distanceSquare < lightDistance) {
                    light = (1.0f - distanceSquare / lightDistance) / 2.0f + ambientLight;
                }
                side.getNode().setLight(light);
                side.setIntPosition(new IntVector(newX, newY, newZ));
                chunk.addVisibleObject(side);
            }
        }
    }

    private boolean isBlockSideVisible(Chunk chunk, Block block, int x, int y, int z, int side) {

        if (block.getType() == Blocks.get("Air")) {
            return false;
        }

        if (block.getNumberOfModels() != 6) {
            return true;
        }

        Block neighbour = getNeighbour(chunk, x, y, z, side);

        if (neighbour == null) {
            return false;
        }
        if (neighbour.getType() == Blocks.get("Air") || neighbour.getType() == Blocks.get("Water")) {
            return true;
        }
        if (neighbour.isTransparent()) {
            if (block.getType() == neighbour.getType()) {
                return false;
            }
            return recursivelyFindAirOrWater(x, y, z, side);
        }
        return false;
    }

    private boolean recursivelyFindAirOrWater(int x, int y, int z, int side) {
        return true;
    }

    private Block getNeighbour(Chunk originalChunk, int x, int y, int z, int side) {
        IntVector vector = RenderableBlock.intVectorFromSide(side);
        int neighbourBlockX = x - vector.getX();
        int neighbourBlockY = y + vector.getY();
        int neighbourBlockZ = z + vector.getZ();

        int chunkX = originalChunk.getIntPosition().x;
        int chunkY = originalChunk.getIntPosition().y;
        int chunkZ = originalChunk.getIntPosition().z;
        if (neighbourBlockX < 0) {
            --chunkX;
            neighbourBlockX = Chunk.CHUNK_BLOCK_SIZE - 1;
        }
        if (neighbourBlockX == Chunk.CHUNK_BLOCK_SIZE) {
            ++chunkX;
            neighbourBlockX = 0;
        }
        if (neighbourBlockY < 0) {
            --chunkX;
            neighbourBlockY = Chunk.CHUNK_BLOCK_SIZE - 1;
        }
        if (neighbourBlockY == Chunk.CHUNK_BLOCK_SIZE) {
            ++chunkX;
            neighbourBlockY = 0;
        }
        if (neighbourBlockZ < 0) {
            --chunkZ;
            neighbourBlockZ = Chunk.CHUNK_BLOCK_SIZE - 1;
        }
        if (neighbourBlockZ == Chunk.CHUNK_BLOCK_SIZE) {
            ++chunkZ;
            neighbourBlockZ = 0;
        }

        Chunk chunk = map.getChunk(chunkX, chunkY, chunkZ);
        if (chunk == null) {
            return null;
        }
        int neighbourBlockXX = getPositionInChunkRange(neighbourBlockX, chunk.getIntPosition().x);
        int neighbourBlockYY = getPositionInChunkRange(neighbourBlockY, chunk.getIntPosition().y);
        int neighbourBlockZZ = getPositionInChunkRange(neighbourBlockZ, chunk.getIntPosition().z);

        return chunk.getBlock(neighbourBlockXX, neighbourBlockYY, neighbourBlockZZ);
    }

    // TODO: some copied code to Chunk
    private int getPositionInChunkRange(int neighbourCoord, int chunkIntPos) {

        if (neighbourCoord >= 0 && neighbourCoord < Math.abs(chunkIntPos * Chunk.CHUNK_BLOCK_SIZE)) {
            return neighbourCoord;
        }

        int result = Math.abs(Math.abs(neighbourCoord)
                - Math.abs(chunkIntPos * Chunk.CHUNK_BLOCK_SIZE));

        return result;
    }
}
