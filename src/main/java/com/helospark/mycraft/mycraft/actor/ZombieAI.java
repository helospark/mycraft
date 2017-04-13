package com.helospark.mycraft.mycraft.actor;

import java.util.List;
import java.util.Random;

import org.lwjgl.util.vector.Vector3f;
import org.springframework.context.ApplicationContext;
import org.w3c.dom.Element;

import com.helospark.mycraft.mycraft.game.Block;
import com.helospark.mycraft.mycraft.game.Blocks;
import com.helospark.mycraft.mycraft.game.GameMap;
import com.helospark.mycraft.mycraft.mathutils.IntVector;
import com.helospark.mycraft.mycraft.mathutils.VectorMathUtils;
import com.helospark.mycraft.mycraft.message.PathFindingResultMessage;
import com.helospark.mycraft.mycraft.messages.ActorCommandMessage;
import com.helospark.mycraft.mycraft.messages.HitActorMessage;
import com.helospark.mycraft.mycraft.messages.MoveInDirectionMessage;
import com.helospark.mycraft.mycraft.pathsearch.PathSearchService;
import com.helospark.mycraft.mycraft.raytracer.RayTracerResult;
import com.helospark.mycraft.mycraft.raytracer.RayTracerType;
import com.helospark.mycraft.mycraft.services.ActorSearchService;
import com.helospark.mycraft.mycraft.services.RayTracer;
import com.helospark.mycraft.mycraft.services.RayTracerProperty;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageHandler;
import com.helospark.mycraft.mycraft.window.MessageListener;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public class ZombieAI extends ActorComponent implements MessageListener {

    public static final String ZOMBIE_AI_COMPONENT_NAME = "ZombieAi";
    public static final float AI_UPDATE_TIME = 4.0f;
    public static final float RAY_TRACE_UPDATE_TIME = 0.5f;
    public static final float ATTACK_TIME = 0.5f;
    private static final float ZOMBIE_HIT_DISTANCE_SQUARED = 0.5f;
    private static final int strength = 3;
    private int searchDistance = 5;
    private final MessageHandler messageHandler;
    private final PathSearchService pathSearchService;
    private final RayTracer rayTracer;
    private float timeTillAiUpdate = 0.0f;
    private float timeTillRayTraceUpdate = 0.0f;
    private float lastAttack = 0.0f;
    private final ActorSearchService actorSearchService;
    private List<IntVector> nodes;
    private int nodeIndex = 0;
    private final Vector3f currentDestination = new Vector3f();
    TransformComponent transformComponent = null;
    Vector3f directionVector = new Vector3f();
    Random random = new Random();
    int pathTaskId = -1;
    float rotationSpeed = 5.0f;
    float currentRotation = 0.0f;
    float desiredRotation = 0.0f;
    private boolean hasStraightDirection = false;
    private final Vector3f straightDirection = new Vector3f();
    GameMap gameMap;

    public ZombieAI(int searchDistance) {
        super(ZOMBIE_AI_COMPONENT_NAME);
        this.searchDistance = searchDistance;
        ApplicationContext context = Singleton.getInstance().getContext();
        messageHandler = context.getBean(MessageHandler.class);
        pathSearchService = context.getBean(PathSearchService.class);
        rayTracer = context.getBean(RayTracer.class);
        actorSearchService = context.getBean(ActorSearchService.class);
        gameMap = context.getBean(GameMap.class);
        messageHandler.registerListener(this, MessageTypes.PATH_FINDING_FINISHED);
    }

    @Override
    public Object createFromXML(Element node) {
        return null;
    }

    @Override
    public void afterInit() {

    }

    @Override
    public void update(double deltaTime) {
        if (transformComponent == null) {
            transformComponent = (TransformComponent) owner
                    .getComponent(TransformComponent.TRANSFORM_COMPONENT_NAME);
        }
        timeTillAiUpdate -= deltaTime;
        timeTillRayTraceUpdate -= deltaTime;
        lastAttack -= deltaTime;
        Actor actor = actorSearchService.getLocalHumanPlayer();

        if (timeTillRayTraceUpdate <= 0.0f) {
            generateStraightDirectionToPlayer(actor);
            timeTillRayTraceUpdate = RAY_TRACE_UPDATE_TIME;
            System.out.println("Ray trace with result: " + hasStraightDirection);
        }

        if (timeTillAiUpdate <= 0.0f && !hasStraightDirection) {
            System.out.println("Generate path");
            generatePathToPlayer(actor);
            timeTillAiUpdate = AI_UPDATE_TIME;
            hasStraightDirection = false;
        }

        // System.out.println(desiredRotation + " " + currentRotation);

        if (Math.abs(Math.abs(desiredRotation) - Math.abs(currentRotation)) < rotationSpeed) {
            currentRotation = desiredRotation;
        } else {
            // TODO: write this
            if (Math.abs(desiredRotation) < Math.abs(currentRotation)) {
                if (desiredRotation < currentRotation) {
                    currentRotation -= rotationSpeed;
                } else {
                    currentRotation += rotationSpeed;
                }
            } else {
                if (currentRotation < desiredRotation) {
                    currentRotation += rotationSpeed;
                } else {
                    currentRotation -= rotationSpeed;
                }
            }
        }
        transformComponent.setRotation(new Vector3f((float) Math.toRadians(-90.0), (float) Math
                .toRadians(currentRotation), 0.0f));

        TransformComponent actorTransformComponent = (TransformComponent) actor
                .getComponent(TransformComponent.TRANSFORM_COMPONENT_NAME);
        if (hasStraightDirection) {
            Vector3f.sub(actorTransformComponent.getPosition(), transformComponent.getPosition(),
                    directionVector);
            directionVector.y = 0.0f;
            moveTowardDestination(directionVector);
        } else {
            // System.out.println(nodeIndex);
            if (nodes != null && nodeIndex < nodes.size()) {
                System.out.println("Path " + nodeIndex + " " + currentDestination + " "
                        + directionVector + " " + transformComponent.getPosition());
                Vector3f zombiePosition = new Vector3f(transformComponent.getPosition());
                zombiePosition.x -= 0.5;
                zombiePosition.z -= 0.5;
                Vector3f.sub(currentDestination, zombiePosition, directionVector);
                directionVector.y = 0.0f;
                moveTowardDestination(directionVector);
                if (VectorMathUtils.distanceSquareBetween(zombiePosition, currentDestination) < 0.1f) {

                    IntVector currentDestinationIntVector = nodes.get(nodeIndex);
                    GameMap.fillVector3fFromIntvector(currentDestination,
                            currentDestinationIntVector);
                    ++nodeIndex;
                    if (nodeIndex >= nodes.size()) {
                        generatePathToPlayer(actor);
                    }
                }
            }
        }
        if (VectorMathUtils.distanceSquareBetween(actorTransformComponent.getPosition(),
                transformComponent.getPosition()) < ZOMBIE_HIT_DISTANCE_SQUARED
                && lastAttack <= 0.0f) {
            Vector3f direction = new Vector3f();
            Vector3f.sub(actorTransformComponent.getPosition(), transformComponent.getPosition(),
                    direction);
            direction.y += 0.4f;
            if (VectorMathUtils.lengthSquared(direction) >= 0.01) {
                direction.normalise();
            } else {
                direction.x = 0.707f;
                direction.y = 0.707f;
                direction.z = 0.0f;
            }
            VectorMathUtils.mul(direction, 2.0f);
            Message message = new HitActorMessage(MessageTypes.HIT_ACTOR_MESSAGE, direction,
                    strength, actor.getId(), owner.getId());
            messageHandler.sendMessage(message);
            lastAttack = ATTACK_TIME;
        }
    }

    private boolean generateStraightDirectionToPlayer(Actor actor) {
        TransformComponent actorTransformComponent = (TransformComponent) actor
                .getComponent(TransformComponent.TRANSFORM_COMPONENT_NAME);

        if (actorTransformComponent != null) {
            if (VectorMathUtils.distanceSquareBetween(transformComponent.getPosition(),
                    actorTransformComponent.getPosition()) < searchDistance * searchDistance) {
                Vector3f startPosition = new Vector3f(transformComponent.getPosition());
                startPosition.y += 1.0f;
                Vector3f endPosition = new Vector3f(actorTransformComponent.getPosition());
                endPosition.y += 1.0f;
                float actorDistanceSquared = VectorMathUtils.distanceSquareBetween(endPosition,
                        startPosition);
                Vector3f.sub(endPosition, startPosition, straightDirection);
                straightDirection.normalise();
                RayTracerResult result = rayTracer.traceRays(startPosition, straightDirection,
                        searchDistance, new RayTracerProperty(
                                new RayTracerType[] { RayTracerType.BLOCK }, actor.getId()));
                if (result == null || !result.hasFound()
                        || result.getDistance() * result.getDistance() >= actorDistanceSquared) {
                    hasStraightDirection = true;
                } else {
                    hasStraightDirection = false;
                }
            }
        }
        return hasStraightDirection;
    }

    private void generatePathToPlayer(Actor actor) {
        TransformComponent actorTransformComponent = (TransformComponent) actor
                .getComponent(TransformComponent.TRANSFORM_COMPONENT_NAME);
        if (transformComponent != null) {
            IntVector destination = GameMap.getIntPositionFromPosition(actorTransformComponent
                    .getPosition());
            IntVector startPosition = GameMap.getIntPositionFromPosition(transformComponent
                    .getPosition());
            pathTaskId = pathSearchService.requestShortestPathFinding(startPosition, destination,
                    6, 0.5f);
            nodes = null;
        }
    }

    private void generateRandomPath() {
        IntVector startPosition = GameMap.getIntPositionFromPosition(transformComponent
                .getPosition());
        IntVector endPosition = new IntVector(startPosition);
        endPosition.x += random.nextInt(10) - 5;
        endPosition.z += random.nextInt(10) - 5;
        pathTaskId = pathSearchService.requestShortestPathFinding(startPosition, endPosition, 6,
                0.5f);
        timeTillAiUpdate = AI_UPDATE_TIME;
    }

    private void moveTowardDestination(Vector3f directionVector) {
        if (VectorMathUtils.lengthSquared(directionVector) > 0.0001) {
            directionVector.normalise();
            float rotation = (float) Math.toDegrees(Math
                    .atan2(directionVector.x, directionVector.z));
            rotation += -90.0f;
            desiredRotation = rotation;
            // System.out.println(directionVector + " " + Math.toDegrees(rads));
            // transformComponent
            // .setRotation(new Vector3f((float) Math.toRadians(-90.0),
            // rotation, 0));
        }
        messageHandler.sendImmediateMessage(new MoveInDirectionMessage(
                MessageTypes.MOVE_IN_DIRECTION, owner.id, directionVector));
        Vector3f position = new Vector3f(transformComponent.getPosition());
        Vector3f direction = new Vector3f(directionVector);
        VectorMathUtils.mul(direction, 1.1f);
        Vector3f.add(position, direction, position);
        boolean shouldJump = true;

        Block block = gameMap.getBlockAtPosition(GameMap.getIntPositionFromPosition(position));
        if (block != null && block.getType() != Blocks.get("Air")) {
            for (int i = 0; i < 2; ++i) {
                position.y += 1.0f;
                block = gameMap.getBlockAtPosition(GameMap.getIntPositionFromPosition(position));
                if (block != null && block.getType() != Blocks.get("Air")) {
                    shouldJump = false;
                }
            }
        } else {
            shouldJump = false;
        }

        if (shouldJump) {
            PhysicsComponent physicsComponent = (PhysicsComponent) owner
                    .getComponent(PhysicsComponent.PHYSICS_COMPONENT_PROPERTY_NAME);
            if (physicsComponent != null && physicsComponent.isTouchGround()) {
                ActorCommandMessage actorCommandMessage = new ActorCommandMessage(
                        MessageTypes.ACTOR_JUMP, Message.MESSAGE_TARGET_ANYONE, owner.id);
                messageHandler.sendMessage(actorCommandMessage);
            }
        }
    }

    @Override
    public void onRemove() {

    }

    @Override
    public ActorComponent createNew() {
        return null;
    }

    @Override
    public boolean receiveMessage(Message message) {
        if (message.getType() == MessageTypes.PATH_FINDING_FINISHED) {

            PathFindingResultMessage pathMessage = (PathFindingResultMessage) message;
            // if (pathMessage.getTaskId() == pathTaskId)
            {
                nodes = pathMessage.getResultList();
                nodeIndex = 0;
                if (nodes.size() > 0) {
                    IntVector currentDestinationIntVector = nodes.get(0);
                    GameMap.fillVector3fFromIntvector(currentDestination,
                            currentDestinationIntVector);
                }
                // if (nodes.size() > 1) {
                // IntVector currentDestinationIntVector = nodes.get(1);
                // GameMap.fillVector3fFromIntvector(currentDestination,
                // currentDestinationIntVector);
                // nodeIndex = 1;
                // }
            }
        }
        return false;
    }

}
