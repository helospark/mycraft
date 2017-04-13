package com.helospark.mycraft.mycraft.game;

import java.util.Map;
import java.util.Random;

import org.lwjgl.util.vector.Vector3f;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.helospark.mycraft.mycraft.mathutils.IntVector;
import com.helospark.mycraft.mycraft.mathutils.VectorMathUtils;
import com.helospark.mycraft.mycraft.maze.MazeCell;
import com.helospark.mycraft.mycraft.maze.MazePanel;
import com.helospark.mycraft.mycraft.messages.ActorPositionChangedMessage;
import com.helospark.mycraft.mycraft.messages.GenericIntegerVectorMessage;
import com.helospark.mycraft.mycraft.messages.NewBlockMessage;
import com.helospark.mycraft.mycraft.services.ChunkFileHandlerService;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageHandler;
import com.helospark.mycraft.mycraft.window.MessageTypes;

@Service
public class ChunkGenerator {
    public static final int CHUNK_MAX_DISTANCE = 1;
    private final MessageHandler messager;
    private final GameMap map;
    private long lastRun = 0;
    private final MazePanel maze;
    private int seed;
    private boolean mazeGenerated = false;
    private boolean runOnce = false;
    @Autowired
    ChunkFileHandlerService chunkFileHandlerService;

    // private static final int MAX_DISTANCE_IN_UNITS = CHUNK_MAX_DISTANCE *
    // Chunk.CHUNK_UNIT_SIZE + 40.0f;

    public ChunkGenerator() {
        ApplicationContext context = Singleton.getInstance().getContext();
        messager = context.getBean(MessageHandler.class);
        map = context.getBean(GameMap.class);
        // messager.registerListener(this, MessageTypes.ACTOR_POSITION_CHANGED);
        maze = new MazePanel();
    }

    public boolean receiveMessage(Message message) {

        if (message.getType() == MessageTypes.ACTOR_POSITION_CHANGED) {
            if (!mazeGenerated) {
                mazeGenerated = true;
                maze.generateMaze();
            }
            if (/* System.nanoTime() - lastRun > 1000000000000L || */!runOnce) {
                ActorPositionChangedMessage actorMessage = (ActorPositionChangedMessage) message;
                Vector3f actorNewPosition = actorMessage.getNewPosition();

                int currentChunkX = (int) Math.floor(actorNewPosition.x / Chunk.CHUNK_UNIT_SIZE);
                int currentChunkY = (int) Math.floor(actorNewPosition.y / Chunk.CHUNK_UNIT_SIZE);
                int currentChunkZ = (int) Math.floor(actorNewPosition.z / Chunk.CHUNK_UNIT_SIZE);

                // handleFileLoadingTheChunks(actorNewPosition);

                for (int i = currentChunkX - CHUNK_MAX_DISTANCE; i <= currentChunkX
                        + CHUNK_MAX_DISTANCE; ++i) {
                    for (int j = currentChunkY - CHUNK_MAX_DISTANCE; j <= currentChunkY
                            + CHUNK_MAX_DISTANCE; ++j) {
                        for (int k = currentChunkZ - CHUNK_MAX_DISTANCE; k <= currentChunkZ
                                + CHUNK_MAX_DISTANCE; ++k) {
                            if (i == 0 && j == 0 && k == 0) { // TODO: home
                                                              // debug
                                generateChunkIfNeeded(i, j, k, actorNewPosition);
                            }
                        }
                    }
                }
                lastRun = System.nanoTime();
                runOnce = true;
            }
        } else if (message.getType() == MessageTypes.GROW_TREE) {
            GenericIntegerVectorMessage intvectorMessage = (GenericIntegerVectorMessage) message;
            IntVector position = new IntVector(intvectorMessage.getX(), intvectorMessage.getY(),
                    intvectorMessage.getZ());
            Chunk chunk = map.getChunkForPosition(position.x, position.y, position.z);
            Random random = new Random();
            createTree(chunk, random, position, 9);
        }

        return false;
    }

    private void handleFileLoadingTheChunks(Vector3f playerPosition) {
        Map<String, Chunk> loadedChunks = map.getLoadedChunks();
        for (Map.Entry<String, Chunk> entry : loadedChunks.entrySet()) {
            Chunk chunk = entry.getValue();
            float distanceSquared = VectorMathUtils.distanceSquareBetween(chunk.getPosition(),
                    playerPosition);

            if (distanceSquared > 120 * 120) {

                messager.sendMessage(new GenericIntegerVectorMessage(
                        MessageTypes.UNLOAD_CHUNK_MESSAGE, Message.MESSAGE_TARGET_ANYONE, chunk
                                .getIntPosition()));
            }
        }
    }

    private void generateChunkIfNeeded(int i, int j, int k, Vector3f playerPosition) {
        if (!map.isChunkGenerated(i, j, k)) {
            float distanceSquared = VectorMathUtils.distanceSquareBetween(new Vector3f(i
                    * Chunk.CHUNK_UNIT_SIZE, j * Chunk.CHUNK_UNIT_SIZE, k * Chunk.CHUNK_UNIT_SIZE),
                    playerPosition);
            if (distanceSquared < 100 * 100) {
                if (chunkFileHandlerService.hasChunk(i, j, k)) {
                    // messager.sendImmediateMessage(new
                    // GenericIntegerVectorMessage(
                    // MessageTypes.LOAD_CHUNK_MESSAGE,
                    // Message.MESSAGE_TARGET_ANYONE,
                    // new IntVector(i, j, k)));

                    Chunk newChunk = generateChunk(i, j, k);
                    map.setChunk(i, j, k, newChunk);
                    sendChunkGeneratedMessage(i, j, k);
                } else {
                    Chunk newChunk = generateChunk(i, j, k);
                    map.setChunk(i, j, k, newChunk);
                    sendChunkGeneratedMessage(i, j, k);
                }
            }
        }

    }

    private Chunk generateChunk(int chunkX, int chunkY, int chunkZ) {
        Random random = new Random();
        Chunk result = new Chunk(chunkX, chunkY, chunkZ);
        for (int x = 0; x < Chunk.CHUNK_BLOCK_SIZE; x++) {
            for (int y = 0; y < Chunk.CHUNK_BLOCK_SIZE; y++) {
                for (int z = 0; z < Chunk.CHUNK_BLOCK_SIZE; ++z) {
                    int absoluteX = chunkX * Chunk.CHUNK_BLOCK_SIZE + x;
                    int absoluteY = chunkY * Chunk.CHUNK_BLOCK_SIZE + y;
                    int absoluteZ = chunkZ * Chunk.CHUNK_BLOCK_SIZE + z;
                    RenderableBlock block = null;
                    if (chunkX == 0 && chunkY == 0 && chunkZ == 0) {
                        if (y == 10) {
                            block = new RenderableBlock(Blocks.get("Dirt"));
                        } else {
                            block = new RenderableBlock(Blocks.get("Air"));
                        }
                        if (y <= 10) {
                            block = new RenderableBlock(Blocks.get("Dirt"));
                        } else {
                            if (y == 11 || y == 12 || y == 13) {
                                MazeCell mazeCell = maze.getCurrentCell(x / 4, z /
                                        4);
                                if (mazeCell.isLeftWall() && x % 4 == 0) {
                                    if (y == 13) {
                                        block = new RenderableBlock(Blocks.get("Gold"));
                                    } else {
                                        block = new RenderableBlock(Blocks.get("Stone"));
                                    }
                                } else if (mazeCell.isTopWall() && z % 4 == 2) {
                                    if (y == 13) {
                                        block = new RenderableBlock(Blocks.get("Gold"));
                                    } else {
                                        block = new RenderableBlock(Blocks.get("Stone"));
                                    }
                                } else {
                                    block = new RenderableBlock(Blocks.get("Air"));
                                }
                            } else {
                                block = new RenderableBlock(Blocks.get("Air"));
                            }
                        }

                    } else if (chunkY == 0) {
                        if (y == 10) {
                            block = new RenderableBlock(Blocks.get("Stone"));
                        } else {
                            block = new RenderableBlock(Blocks.get("Air"));
                        }
                    } else {
                        block = new RenderableBlock(Blocks.get("Air"));
                    }

                    // if (chunkX == 0 && chunkY == 0 && chunkZ == 0 && x == 10
                    // && y == 10 && z == 10) {
                    // block = new RenderableBlock(Blocks.get("Dirt"));
                    // } else {
                    // block = new RenderableBlock(Blocks.get("Air"));
                    // }
                    result.setBlock(x, y, z, block);

                }
            }
        }
        if (chunkX == 0 && chunkY == 0 && chunkZ == 0) {
            // createTree(result, random, new IntVector(33, 10, 33), 9);
            result.setBlock(20, 11, 20, new RenderableBlock(Blocks.get("Dirt")));
            result.setBlock(21, 11, 20, new RenderableBlock(Blocks.get("Dirt")));
            result.setBlock(22, 11, 20, new RenderableBlock(Blocks.get("Dirt")));
            result.setBlock(23, 11, 20, new RenderableBlock(Blocks.get("Dirt")));
            result.setBlock(20, 11, 21, new RenderableBlock(Blocks.get("Dirt")));
            result.setBlock(20, 11, 22, new RenderableBlock(Blocks.get("Dirt")));
            result.setBlock(20, 11, 23, new RenderableBlock(Blocks.get("Dirt")));
            result.setBlock(21, 11, 23, new RenderableBlock(Blocks.get("Dirt")));
            result.setBlock(22, 11, 23, new RenderableBlock(Blocks.get("Dirt")));
            result.setBlock(23, 11, 23, new RenderableBlock(Blocks.get("Dirt")));

            result.setBlock(20, 12, 20, new RenderableBlock(Blocks.get("Dirt")));
            result.setBlock(21, 12, 20, new RenderableBlock(Blocks.get("Dirt")));
            result.setBlock(22, 12, 20, new RenderableBlock(Blocks.get("Dirt")));
            result.setBlock(23, 12, 20, new RenderableBlock(Blocks.get("Dirt")));
            result.setBlock(20, 12, 21, new RenderableBlock(Blocks.get("Dirt")));
            result.setBlock(20, 12, 22, new RenderableBlock(Blocks.get("Dirt")));
            result.setBlock(20, 12, 23, new RenderableBlock(Blocks.get("Dirt")));
            result.setBlock(21, 12, 23, new RenderableBlock(Blocks.get("Dirt")));
            result.setBlock(22, 12, 23, new RenderableBlock(Blocks.get("Dirt")));
            result.setBlock(23, 12, 23, new RenderableBlock(Blocks.get("Dirt")));
        }
        return result;
    }

    private void createTree(Chunk result, Random random, IntVector position, int size) {
        for (int i = 0; i < size; ++i) {
            NewBlockMessage newBlockMessage = new NewBlockMessage(MessageTypes.NEW_BLOCK_MESSAGE,
                    Message.MESSAGE_TARGET_ANYONE, new IntVector(position.x, position.y + i,
                            position.z),
                    Blocks.get("RegularWood"));
            messager.sendImmediateMessage(newBlockMessage);

        }

        int leavesSize = size / 2;
        IntVector leavesCenter = new IntVector(position.x, position.y + size, position.z);
        IntVector newPosition = new IntVector();

        for (int i = -leavesSize; i <= leavesSize; ++i) {
            for (int j = -leavesSize + 1; j <= leavesSize; ++j) {
                for (int k = -leavesSize; k <= leavesSize; ++k) {
                    newPosition.x = leavesCenter.x + k;
                    newPosition.y = leavesCenter.y + j;
                    newPosition.z = leavesCenter.z + i;
                    float distanceSquared = VectorMathUtils.distanceSquareBetween(
                            newPosition.toVector3f(), leavesCenter.toVector3f());

                    if (distanceSquared <= leavesSize * leavesSize + 2.0f
                            && random.nextFloat() < 0.7f) {
                        RenderableBlock renderableBlock = result.getRenderableBlockAt(
                                newPosition.x, newPosition.y, newPosition.z);
                        if (renderableBlock == null || renderableBlock.getId() == Blocks.get("Air")) {
                            NewBlockMessage newBlockMessage = new NewBlockMessage(
                                    MessageTypes.NEW_BLOCK_MESSAGE, Message.MESSAGE_TARGET_ANYONE,
                                    newPosition, Blocks.get("Leaves"));
                            messager.sendImmediateMessage(newBlockMessage);
                        }
                    }
                }
            }
        }
    }

    private void sendChunkGeneratedMessage(int i, int j, int k) {
        messager.sendImmediateMessage(new GenericIntegerVectorMessage(
                MessageTypes.CHUNK_GENERATED_MESSAGE, Message.MESSAGE_TARGET_ANYONE, i, j, k));
    }

    public void setSeed(int seed) {
        this.seed = seed;
        maze.generateMaze();
        mazeGenerated = true;
    }
}
