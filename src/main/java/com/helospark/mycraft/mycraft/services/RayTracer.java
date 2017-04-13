package com.helospark.mycraft.mycraft.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.helospark.mycraft.mycraft.actor.Actor;
import com.helospark.mycraft.mycraft.actor.PhysicsComponent;
import com.helospark.mycraft.mycraft.game.Block;
import com.helospark.mycraft.mycraft.game.Blocks;
import com.helospark.mycraft.mycraft.game.Chunk;
import com.helospark.mycraft.mycraft.game.GameMap;
import com.helospark.mycraft.mycraft.mathutils.BoundingBox;
import com.helospark.mycraft.mycraft.mathutils.IntVector;
import com.helospark.mycraft.mycraft.raytracer.RayTracableBox;
import com.helospark.mycraft.mycraft.raytracer.RayTraceResultComparator;
import com.helospark.mycraft.mycraft.raytracer.RayTracerResult;
import com.helospark.mycraft.mycraft.raytracer.RayTracerType;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.window.MessageHandler;

@Service
public class RayTracer {
    public static final int INITIAL_COLLISION_RESULT_AMOUNT = 10;
    public static final int RESULT_INCREMENT_MULTIPLIER = 2;
    private final double minDistance = 0.0001;
    private RayTracerResult lastRayTracerResult = null;

    // cache
    private final List<RayTracerResult> collidedWithObjects = new ArrayList<RayTracerResult>();
    private final RayTraceResultComparator comparator = new RayTraceResultComparator();
    private final List<RayTracerResult> rayTraceResults = new ArrayList<RayTracerResult>();

    RayTracableBox lastObject;
    private final GameMap gameMap;
    private final MessageHandler messager;
    @Autowired
    private ActorSearchService actorSearchService;

    public RayTracer() {
        ApplicationContext context = Singleton.getInstance().getContext();
        messager = context.getBean(MessageHandler.class);
        gameMap = context.getBean(GameMap.class);
    }

    public RayTracerResult traceRays(Vector3f position, Vector3f direction, int maxBlockDistance,
            RayTracerProperty property) {

        boolean result = false;
        int rayTraceResultIndex = 0;
        collidedWithObjects.clear();
        List<RayTracableBox> possibleRenderableObjects = calculatePossibleBoxes(position,
                maxBlockDistance);
        lastRayTracerResult = null;
        if (Arrays.asList(property.getTypes()).contains(RayTracerType.BLOCK)) {
            for (int i = 0; i < possibleRenderableObjects.size(); ++i) {
                RayTracerResult rayTraceResult = new RayTracerResult();
                possibleRenderableObjects.get(i).rayTracing(position, direction, rayTraceResult);
                if (shouldAddToResultList(property, rayTraceResult)) {
                    rayTraceResult.setBlockId(possibleRenderableObjects.get(i).getType());
                    collidedWithObjects.add(rayTraceResult);
                }
            }
        }

        if (Arrays.asList(property.getTypes()).contains(RayTracerType.ACTOR)) {
            List<Actor> actors = actorSearchService.getActorsCloseToPosition(position,
                    maxBlockDistance * Block.SIZE);
            for (int i = 0; i < actors.size(); ++i) {
                if (actors.get(i).getId() != property.getOwnerIdToIgnore()) {
                    PhysicsComponent physicsComponent = (PhysicsComponent) actors.get(i).getComponent(
                            PhysicsComponent.PHYSICS_COMPONENT_PROPERTY_NAME);
                    RayTracableBox rayTracableBox = new RayTracableBox(
                            physicsComponent.getBoundingBox());
                    if (physicsComponent != null) {
                        RayTracerResult rayTraceResult = new RayTracerResult();
                        rayTracableBox.rayTracing(position, direction, rayTraceResult);
                        if (shouldAddToResultList(property, rayTraceResult)) {
                            rayTraceResult.setActor(actors.get(i));
                            collidedWithObjects.add(rayTraceResult);
                        }
                    }
                }
            }
        }

        if (collidedWithObjects.size() != 0) {
            int index = collidedWithObjects.size();
            double currentMinimum = collidedWithObjects.get(0).getDistance();
            index = 0;
            for (int i = 1; i < collidedWithObjects.size(); ++i) {
                if (collidedWithObjects.get(i).getDistance() <= currentMinimum) {
                    currentMinimum = collidedWithObjects.get(i).getDistance();
                    index = i;
                }
            }

            if (index < collidedWithObjects.size()) {
                lastObject = collidedWithObjects.get(index).getObject();
                lastRayTracerResult = collidedWithObjects.get(index);
                result = true;
            } else {
                lastObject = null;
                lastRayTracerResult = null;
                result = false;
            }
        }
        if (lastRayTracerResult == null) {
            return null;
        } else {
            RayTracerResult copiedResult = new RayTracerResult();
            copiedResult.initializeFrom(lastRayTracerResult);
            return copiedResult;
        }
    }

    private boolean shouldAddToResultList(RayTracerProperty property, RayTracerResult rayTraceResult) {
        boolean result = rayTraceResult.hasFound() && rayTraceResult.getDistance() > minDistance
                && !ignoreListContains(property, rayTraceResult);

        RayTracerType[] types = property.getTypes();
        return result;
    }

    private boolean ignoreListContains(RayTracerProperty property, RayTracerResult rayTraceResult) {
        List<IntVector> ignoreList = property.getIgnoreAtPositions();
        IntVector intPosition = new IntVector();
        intPosition.x = (int) Math.floor(rayTraceResult.getPosition().x / Block.SIZE);
        intPosition.y = (int) Math.floor(rayTraceResult.getPosition().y / Block.SIZE);
        intPosition.z = (int) Math.floor(rayTraceResult.getPosition().z / Block.SIZE);
        return ignoreList.contains(intPosition);
    }

    private List<RayTracableBox> calculatePossibleBoxes(Vector3f position, int maxBlockDistance) {
        IntVector intPosition = GameMap.getIntPositionFromPosition(position);
        List<RayTracableBox> possibleBoxes = new ArrayList<RayTracableBox>();
        for (int x = intPosition.x - maxBlockDistance; x <= intPosition.x + maxBlockDistance; ++x) {
            for (int y = intPosition.y - maxBlockDistance; y <= intPosition.y + maxBlockDistance; ++y) {
                for (int z = intPosition.z - maxBlockDistance; z <= intPosition.z
                        + maxBlockDistance; ++z) {
                    addBlockIfShouldBeRayTraced(possibleBoxes, x, y, z);
                }
            }
        }
        return possibleBoxes;
    }

    private void addBlockIfShouldBeRayTraced(List<RayTracableBox> possibleBoxes, int x, int y, int z) {
        Chunk chunk = gameMap.getChunkForPosition(x, y, z);
        if (chunk != null) {
            IntVector blockPosition = new IntVector(x, y, z);
            IntVector chunkBlockPosition = chunk.getIntPositionInRange(blockPosition);
            Block block = chunk.getBlock(chunkBlockPosition.x, chunkBlockPosition.y,
                    chunkBlockPosition.z);
            if (block != null && block.getType() != Blocks.get("Air")) {
                BoundingBox boundingBox = block.calculateBoundingBox(x, y, z);
                RayTracableBox rayTracableBox = new RayTracableBox(boundingBox);
                rayTracableBox.setType(block.getType());
                possibleBoxes.add(rayTracableBox);
            }
        }
    }

    private void incrementRayTraceResultSizeIfNeeded(int rayTraceResultIndex) {
        if (rayTraceResultIndex >= rayTraceResults.size()) {
            int newSize = rayTraceResults.size() * RESULT_INCREMENT_MULTIPLIER;
            for (int j = rayTraceResults.size(); j < newSize; ++j) {
                rayTraceResults.add(new RayTracerResult());
            }
        }
    }
}
