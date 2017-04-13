package com.helospark.mycraft.mycraft.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector3f;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.helospark.mycraft.mycraft.attributes.Model;
import com.helospark.mycraft.mycraft.mathutils.Frustum;
import com.helospark.mycraft.mycraft.mathutils.IntVector;
import com.helospark.mycraft.mycraft.messages.BlockDestroyedMessage;
import com.helospark.mycraft.mycraft.render.BatchModel;
import com.helospark.mycraft.mycraft.render.Material;
import com.helospark.mycraft.mycraft.render.MyTexture;
import com.helospark.mycraft.mycraft.render.MyTextureLoader;
import com.helospark.mycraft.mycraft.services.GlobalParameters;
import com.helospark.mycraft.mycraft.services.MyExecutorService;
import com.helospark.mycraft.mycraft.shader.Shader;
import com.helospark.mycraft.mycraft.shader.ShaderLoader;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.transformation.Transformation;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageHandler;
import com.helospark.mycraft.mycraft.window.MessageListener;
import com.helospark.mycraft.mycraft.window.MessageTypes;

@Service
public class GameMapRenderer implements MessageListener {
    @Autowired
    MyExecutorService batchExecutorService;
    List<OctreeBatchTask> batchTasks = new ArrayList<>();
    List<Future<Map<Material, List<Model>>>> results = new ArrayList<>();
    List<Future<Map<Material, List<Model>>>> laterHandledList = new ArrayList<>();
    Map<Material, List<Model>> renderableSides = new HashMap<>();
    Map<Material, List<Model>> mergedList = new HashMap<>();
    Map<Material, List<Model>> transparentObjectList = new HashMap<>();

    @Autowired
    Transformation transformation;

    int batchRenderVbo = -1;
    private BatchModel model;
    Shader shader;
    Material material;
    MyTexture texture;
    @Autowired
    GameMap gameMap;
    @Autowired
    MyTextureLoader textureLoader;
    @Autowired
    ShaderLoader shaderLoader;
    @Autowired
    DynamicObjectRenderer dynamicObjectRenderer;

    MessageHandler messager;
    GlobalParameters globalParameters;

    public GameMapRenderer() {
        ApplicationContext context = Singleton.getInstance().getContext();
        globalParameters = context.getBean(GlobalParameters.class);
        messager = context.getBean(MessageHandler.class);
        messager.registerListener(this, MessageTypes.BLOCK_DESTROYED_MESSAGE);
        messager.registerListener(this, MessageTypes.ESCAPE_REQUESTED);
    }

    public void initialize() {
        model = new BatchModel(50000);
        model.setShader(shader);
        material = new Material(shader);
        texture = textureLoader.load2DTexture("resources/blocks.png");
        material.addTexture("sampler", texture);
        model.createPositionAttribute();
        model.createUvAttribute();
        model.createLight();
        shader = shaderLoader.loadStandardShaderFromFile("src/main/resources/shaders/vertex.vs",
                "src/main/resources/shaders/fragment.fs");
        material = new Material(shader);
        material.addTexture("sampler", texture);
    }

    public void startAsynchBatchPrepare(Camera camera) {
        long startTime = System.nanoTime();
        Map<String, Chunk> loadedChunks = gameMap.getLoadedChunks();
        int batchTaskIndex = 0;
        results.clear();
        for (Map.Entry<String, Chunk> entry : loadedChunks.entrySet()) {
            Chunk chunk = entry.getValue();
            for (int i = 0; i < OctreeChunk.OCTREE_CHILDREN_NUMBER; ++i) {
                OctreeBatchTask batchTask;
                if (batchTaskIndex < batchTasks.size()) {
                    batchTask = batchTasks.get(batchTaskIndex);
                } else {
                    batchTask = new OctreeBatchTask();
                    batchTasks.add(batchTask);
                }
                batchTask.setFrustum(camera.getFrustum());
                batchTask.setOctreeToCheck(chunk, i);
                batchTask.clearResultList();
                batchTask.fillWithRootElements(camera.getFrustum());

                Future<Map<Material, List<Model>>> batchFuture = batchExecutorService
                        .getExecutorService().submit(batchTask);
                results.add(batchFuture);

                batchTaskIndex++;

            }
        }
        // System.out.println("Prepare took: " + (System.nanoTime() - startTime)
        // / (1000000.0));
    }

    public Map<Material, List<Model>> getConcatenatedRenderableSideMap() {
        renderableSides.clear();
        laterHandledList.clear();
        renderableSides.clear();
        transparentObjectList.clear();

        long startTime = System.nanoTime();

        for (int i = 0; i < results.size(); ++i) {
            Future<Map<Material, List<Model>>> result = results.get(i);
            if (result.isDone()) {
                Map<Material, List<Model>> resultMap = null;
                try {
                    resultMap = getResultMapFromFuture(result);
                } catch (Exception e) {
                    // solved
                }
                if (resultMap != null) {
                    mergeListIntoFinalResult(resultMap);
                }
            } else {
                // give these thread a little time, while we merge stuff into
                // the final map
                laterHandledList.add(result);
            }
        }

        // System.out.println("First took: " + (System.nanoTime() - startTime) /
        // (1000000.0));

        startTime = System.nanoTime();

        // now we make sure everything is finished
        for (int i = 0; i < laterHandledList.size(); ++i) {
            Future<Map<Material, List<Model>>> result = laterHandledList.get(i);
            Map<Material, List<Model>> resultMap = getResultMapFromFuture(result);
            mergeListIntoFinalResult(resultMap);
        }
        // System.out.println("Second took: " + (System.nanoTime() - startTime)
        // / (1000000.0));

        return renderableSides;
    }

    private Map<Material, List<Model>> getResultMapFromFuture(
            Future<Map<Material, List<Model>>> result) {
        Map<Material, List<Model>> resultMap;
        try {
            resultMap = result.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Exeption while getting the result" + e);
        }
        return resultMap;
    }

    private void mergeListIntoFinalResult(Map<Material, List<Model>> resultMap) {
        for (Map.Entry<Material, List<Model>> entry : resultMap.entrySet()) {
            if (entry.getKey().isTransparent()) {
                addEntryToMap(entry, transparentObjectList);
            } else {
                addEntryToMap(entry, renderableSides);
            }
        }
    }

    private void addEntryToMap(Map.Entry<Material, List<Model>> entry,
            Map<Material, List<Model>> modelList) {
        List<Model> models = modelList.get(entry.getKey());
        if (models != null) {
            models.addAll(entry.getValue());
        } else {
            modelList.put(entry.getKey(), entry.getValue());
        }
    }

    public void render(Frustum frustum) {
        getConcatenatedRenderableSideMap();
        long startTime = System.nanoTime();
        renderFaces(renderableSides);
        sortTransparentFaces(frustum.getPosition()); // TODO: put into separate
                                                     // thread
        GL11.glEnable(GL11.GL_BLEND);
        renderFaces(transparentObjectList);
        GL11.glDisable(GL11.GL_BLEND);
        // System.out.println("Render took: " + (System.nanoTime() - startTime)
        // / (1000000.0));
        // System.out.println("Rendered: " + renderedFaces);
        dynamicObjectRenderer.render();
    }

    private void sortTransparentFaces(Vector3f cameraPosition) {
        for (Map.Entry<Material, List<Model>> entry : transparentObjectList.entrySet()) {
            List<Model> models = entry.getValue();

            for (int i = 0; i < models.size(); ++i) {
                for (int j = i + 1; j < models.size(); ++j) {
                    if (!models.get(j).isCloserToCamera(models.get(i), cameraPosition)) {
                        Model tmpModel = models.get(i);
                        models.set(i, models.get(j));
                        models.set(j, tmpModel);
                    }
                }
            }
        }
    }

    private void renderFaces(Map<Material, List<Model>> faces) {
        int renderedFaces = 0;
        for (Map.Entry<Material, List<Model>> block : faces.entrySet()) {
            Material material = block.getKey();
            material.bind();
            List<Model> blocks = block.getValue();
            Shader shader = block.getKey().shader;
            shader.useProgram();

            transformation.uploadToOpenglShaders(shader);
            if (batchRenderVbo == -1) {
                batchRenderVbo = model.getVaoForProgram(shader);
            }
            GL30.glBindVertexArray(batchRenderVbo);
            model.updateBatch(shader, blocks);
            GL30.glBindVertexArray(0);
            material.unBind();
            renderedFaces += blocks.size();
        }
    }

    @Override
    public boolean receiveMessage(Message message) {
        if (message.getType() == MessageTypes.ESCAPE_REQUESTED) {
            batchExecutorService.getExecutorService().shutdownNow();
            results.clear();
        } else if (message.getType() == MessageTypes.BLOCK_DESTROYED_MESSAGE) {
            handleDrop(message);
        }
        return false;
    }

    private void handleDrop(Message message) {
        BlockDestroyedMessage blockDestroyedMessage = (BlockDestroyedMessage) message;

        int x = blockDestroyedMessage.getX();
        int y = blockDestroyedMessage.getY();
        int z = blockDestroyedMessage.getZ();
        IntVector position = new IntVector(x, y, z);

        int blockId = blockDestroyedMessage.getBlockId();
        Block block = Blocks.getBlockForId(blockId);
        if (block != null && blockDestroyedMessage.willDrop()) {
            dynamicObjectRenderer.dropItems(block, new Vector3f((position.x + 0.5f) * Block.SIZE,
                    (position.y + 0.5f) * Block.SIZE, (position.z + 0.5f) * Block.SIZE));
        }
    }

}
