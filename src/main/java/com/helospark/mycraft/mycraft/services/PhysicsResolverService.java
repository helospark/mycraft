package com.helospark.mycraft.mycraft.services;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.helospark.mycraft.mycraft.actor.Actor;
import com.helospark.mycraft.mycraft.actor.PhysicsComponent;
import com.helospark.mycraft.mycraft.actor.TransformComponent;
import com.helospark.mycraft.mycraft.game.Block;
import com.helospark.mycraft.mycraft.game.Blocks;
import com.helospark.mycraft.mycraft.game.Chunk;
import com.helospark.mycraft.mycraft.game.GameMap;
import com.helospark.mycraft.mycraft.game.RenderableBlock;
import com.helospark.mycraft.mycraft.mathutils.BoundingBox;
import com.helospark.mycraft.mycraft.mathutils.CullingResult;
import com.helospark.mycraft.mycraft.mathutils.IntVector;
import com.helospark.mycraft.mycraft.mathutils.Plane;
import com.helospark.mycraft.mycraft.mathutils.VectorMathUtils;
import com.helospark.mycraft.mycraft.messages.ActorLifeDecreaseMessage;
import com.helospark.mycraft.mycraft.messages.ActorPositionChangeMessage;
import com.helospark.mycraft.mycraft.messages.ActorPositionChangedMessage;
import com.helospark.mycraft.mycraft.messages.CollisionMessage;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageHandler;
import com.helospark.mycraft.mycraft.window.MessageListener;
import com.helospark.mycraft.mycraft.window.MessageTypes;

@Service
public class PhysicsResolverService implements MessageListener {
    private static final Vector3f GRAVITY = new Vector3f(0.0f, -0.07f, 0.0f);
    List<Integer> registeredActors = new ArrayList<Integer>();
    MessageHandler messager;
    GameMap gameMap;
    ActorSearchService actorSearchService;

    class PlaneResult {

        float area;
        Plane blockPlane;
        Plane actorPlane;

        public PlaneResult() {
            this.area = 0;
            this.blockPlane = null;
            this.actorPlane = null;
        }

        public PlaneResult(Plane plane, Plane actorPlane, float area) {
            this.area = area;
            this.blockPlane = plane;
            this.actorPlane = actorPlane;
        }

        public float getArea() {
            return area;
        }

        public void setArea(float area) {
            this.area = area;
        }

        public Plane getBlockPlane() {
            return blockPlane;
        }

        public void setBlockPlane(Plane blockPlane) {
            this.blockPlane = blockPlane;
        }

        public Plane getActorPlane() {
            return actorPlane;
        }

        public void setActorPlane(Plane actorPlane) {
            this.actorPlane = actorPlane;
        }

        public boolean isFound() {
            return blockPlane != null;
        }
    }

    public PhysicsResolverService() {
        ApplicationContext context = Singleton.getInstance().getContext();
        messager = context.getBean(MessageHandler.class);
        messager.registerListener(this, MessageTypes.ACTOR_POSITION_CHANGED);
        gameMap = context.getBean(GameMap.class);
        actorSearchService = context.getBean(ActorSearchService.class);
    }

    public void update(double deltaTime) {

        for (int i = 0; i < registeredActors.size(); ++i) {
            Actor movedActor = actorSearchService.findActorById(registeredActors.get(i));
            if (movedActor != null) {
                TransformComponent transform = (TransformComponent) movedActor
                        .getComponent(TransformComponent.TRANSFORM_COMPONENT_NAME);
                PhysicsComponent physics = (PhysicsComponent) movedActor
                        .getComponent(PhysicsComponent.PHYSICS_COMPONENT_PROPERTY_NAME);
                physics.setTouchGround(false);
                if (transform != null && physics != null) {
                    applyForces(movedActor, transform, physics, deltaTime);
                    Vector3f velocity = physics.calculateVelocity((float) deltaTime);
                    Vector3f position = transform.getPositionReference();
                    physics.decreaseVelocity();
                    VectorMathUtils.addInPlace(position, velocity);
                    updateActorPosition(transform.getPosition(), movedActor);
                }
            }
        }

        for (int i = 0; i < registeredActors.size(); ++i) {
            for (int j = i + 1; j < registeredActors.size(); ++j) {
                checkCollisionBetweenActors(i, j);
            }
        }

    }

    private void applyForces(Actor movedActor, TransformComponent transform,
            PhysicsComponent physics, double deltaTime) {
        if (physics.isShouldApplyGravity()) {
            if (physics.getGravity() != null) {
                physics.addImpulse(physics.getGravity());
            } else {
                physics.addImpulse(GRAVITY);
            }
        }
        // TODO: rest of the forces
    }

    private void checkCollisionBetweenActors(int i, int j) {
        Actor actor1 = actorSearchService.findActorById(registeredActors.get(i));
        if (actor1 == null) {
            return;
        }
        PhysicsComponent component1 = (PhysicsComponent) actor1
                .getComponent(PhysicsComponent.PHYSICS_COMPONENT_PROPERTY_NAME);

        Actor actor2 = actorSearchService.findActorById(registeredActors.get(j));
        if (actor2 == null) {
            return;
        }
        PhysicsComponent component2 = (PhysicsComponent) actor2
                .getComponent(PhysicsComponent.PHYSICS_COMPONENT_PROPERTY_NAME);

        if (isActorsCollide(component1, component2)) {
            sendCollisionMessage(i, j);
        }
    }

    private boolean isActorsCollide(PhysicsComponent component1, PhysicsComponent component2) {
        return component1.getBoundingBox().containsBox(component2.getBoundingBox()) != CullingResult.FULLY_OUT;
    }

    private void sendCollisionMessage(int i, int j) {
        CollisionMessage collisionMessage = new CollisionMessage(MessageTypes.COLLISION_MESSAGE,
                Message.MESSAGE_TARGET_ANYONE, registeredActors.get(i), registeredActors.get(j));
        messager.sendMessage(collisionMessage);
    }

    @Override
    public boolean receiveMessage(Message message) {

        if (message.getType() == MessageTypes.ACTOR_POSITION_CHANGED) {
            ActorPositionChangedMessage actorMessage = (ActorPositionChangedMessage) message;
            int actorId = actorMessage.getActorId();
            if (registeredActors.contains(actorId)) {
                Vector3f newPosition = actorMessage.getNewPosition();
                Actor movedActor = actorSearchService.findActorById(actorId);
                if (movedActor != null) {
                    // TODO: optimize this
                    for (int i = 0; i < 2; ++i) {
                        updateActorPosition(newPosition, movedActor);
                    }
                }
            }
        }

        return false;
    }

    private void updateActorPosition(Vector3f newPosition, Actor movedActor) {
        Vector3f position = new Vector3f(newPosition);
        for (int i = 0; i < 2; ++i) {
            boolean positionChanged = checkBlockPositions(position, movedActor);
        }
        sendNewPositionMessageToActor(movedActor.getId(), new Vector3f(position));
    }

    private boolean checkBlockPositions(Vector3f newPosition, Actor movedActor) {
        PhysicsComponent physicsComponent = (PhysicsComponent) movedActor
                .getComponent(PhysicsComponent.PHYSICS_COMPONENT_PROPERTY_NAME);

        boolean positionChanged = false;
        int actorId = movedActor.getId();
        TransformComponent transformComponent = (TransformComponent) movedActor
                .getComponent(TransformComponent.TRANSFORM_COMPONENT_NAME);
        Vector3f actorPosition = transformComponent.getPosition();
        BoundingBox actorBoundingBox = physicsComponent.getBoundingBox(newPosition);
        float left = actorBoundingBox.getLeft();
        float right = actorBoundingBox.getRight();
        float top = actorBoundingBox.getTop();
        float bottom = actorBoundingBox.getBottom();
        float near = actorBoundingBox.getNear();
        float far = actorBoundingBox.getFar();
        Vector3f tmp = new Vector3f();
        PlaneResult winnerPlane = new PlaneResult();
        for (float i = near; i <= far; i += Block.SIZE) {
            for (float j = bottom; j <= top; j += Block.SIZE) {
                for (float k = left; k <= right; k += Block.SIZE) {
                    tmp.x = k;
                    tmp.y = j;
                    tmp.z = i;

                    IntVector intPosition = GameMap.getIntPositionFromPosition(tmp);
                    Chunk chunk = gameMap.getChunkForPosition(intPosition.x, intPosition.y,
                            intPosition.z);

                    if (chunk != null) {
                        IntVector bottomPosition = chunk.getIntPositionInRange(intPosition);

                        Block block = chunk.getBlock(bottomPosition.x, bottomPosition.y,
                                bottomPosition.z);

                        if (block != null && block.getType() != Blocks.get("Air")
                                && block.canCollide()) {
                            BoundingBox originalBoundingBox = block.getBoundingBox();
                            BoundingBox blockBoundingBox = originalBoundingBox
                                    .getBoundingBoxWithPosition(bottomPosition.x * Block.SIZE,
                                            bottomPosition.y * Block.SIZE, bottomPosition.z
                                                    * Block.SIZE);
                            if (blockBoundingBox.containsBox(actorBoundingBox) != CullingResult.FULLY_OUT) {
                                PlaneResult newResult = getClosestWinnerPlane(newPosition,
                                        blockBoundingBox, actorBoundingBox);

                                if (newResult.getArea() > winnerPlane.getArea()) {
                                    winnerPlane = newResult;
                                }
                            }
                        }
                    }
                }
            }
        }
        if (winnerPlane.isFound()) {
            resolveCollision(newPosition, winnerPlane, physicsComponent, actorId);
            positionChanged = true;
        }
        return positionChanged;

    }

    private void resolveCollision(Vector3f newPosition, PlaneResult winnerPlane,
            PhysicsComponent physicsComponent, int actorId) {

        Plane collidedPlane = winnerPlane.getBlockPlane();
        Plane actorPlane = winnerPlane.getActorPlane();

        // if we are on the edge of a block, fall down, instead of being able to
        // move on it fixed a bug, where one could move on a wall's blocks
        if (Math.abs(collidedPlane.getNormal().y) > VectorMathUtils.DELTA
                && winnerPlane.getArea() < 0.2f
                && actorId == actorSearchService.getLocalHumanPlayer().getId()) {
            return;
        }

        Vector3f firstPoint = collidedPlane.getPointOnPlane();
        Vector3f secondPoint = actorPlane.getPointOnPlane();
        VectorMathUtils.mulInPlace(firstPoint, collidedPlane.getNormal());
        VectorMathUtils.mulInPlace(secondPoint, collidedPlane.getNormal());

        float index1 = VectorMathUtils.getNotNullComponent(firstPoint);
        float index2 = VectorMathUtils.getNotNullComponent(secondPoint);

        float distanceToPush = Math.abs(Math.abs(index1) - Math.abs(index2));

        Vector3f directionToPush = new Vector3f(collidedPlane.getNormal());
        VectorMathUtils.mul(directionToPush, distanceToPush);

        VectorMathUtils.addInPlace(newPosition, directionToPush);
        Vector3f velocity = physicsComponent.getVelocity();

        // TODO: don't put this here
        if (velocity.y < -0.40) {
            int lifeToDecrease = (int) Math.abs((velocity.y + 0.40f - 0.03) / 0.03f);
            messager.sendMessage(new ActorLifeDecreaseMessage(MessageTypes.ACTOR_LIFE_DECREASE,
                    actorId, lifeToDecrease));
        }

        physicsComponent.clearAccelerationOnVector(collidedPlane.getNormal());

        if (directionToPush.y > VectorMathUtils.DELTA) {
            physicsComponent.setTouchGround(true);
        }
    }

    private PlaneResult getClosestWinnerPlane(Vector3f newPosition, BoundingBox blockBoundingBox,
            BoundingBox actorBoundingBox) {
        PlaneResult result = new PlaneResult();
        Plane[] actorPlanes = setupActorPlanes(actorBoundingBox);

        for (int i = 0; i < Block.NUM_SIDES; i += 1) {
            Vector3f normal = new Vector3f(RenderableBlock.intVectorFromSide(i).toVector3f());
            Vector3f planePoint = getPointOnPlane(blockBoundingBox, normal);

            Plane currentBlockPlane = new Plane(planePoint, normal);

            for (int j = 0; j < actorPlanes.length; ++j) {
                Plane actorPlane = actorPlanes[j];
                if (blockBoundingBox.isPlaneInside(actorBoundingBox, actorPlane)) {
                    float area = blockBoundingBox.calculateArea(actorBoundingBox, actorPlane,
                            currentBlockPlane);
                    if (area > result.getArea()) {
                        result.setActorPlane(actorPlane);
                        result.setBlockPlane(currentBlockPlane);
                        result.setArea(area);
                    }
                }
            }
        }

        return result;
    }

    private Plane[] setupActorPlanes(BoundingBox actorBoundingBox) {
        Plane actorPlaneX = new Plane((actorBoundingBox.getRight()), 1, 0, 0);
        Plane actorPlaneY = new Plane((actorBoundingBox.getTop()), 0, 1, 0);
        Plane actorPlaneZ = new Plane((actorBoundingBox.getFar()), 0, 0, 1);
        Plane actorPlaneNegativeX = new Plane((actorBoundingBox.getLeft()), -1, 0, 0);
        Plane actorPlaneNegativeY = new Plane((actorBoundingBox.getBottom()), 0, -1, 0);
        Plane actorPlaneNegativeZ = new Plane((actorBoundingBox.getNear()), 0, 0, -1);
        Plane[] actorPlanes = new Plane[] { actorPlaneX, actorPlaneY, actorPlaneZ,
                actorPlaneNegativeX, actorPlaneNegativeY, actorPlaneNegativeZ };
        return actorPlanes;
    }

    private Vector3f getPointOnPlane(BoundingBox actorBoundingBox, Vector3f normal) {
        float distance = Float.MAX_VALUE;
        if (Math.abs(normal.x - 1.0f) < VectorMathUtils.DELTA) {
            distance = actorBoundingBox.getRight();
        }
        if (Math.abs(normal.x + 1.0f) < VectorMathUtils.DELTA) {
            distance = actorBoundingBox.getLeft();
        }
        if (Math.abs(normal.y - 1.0f) < VectorMathUtils.DELTA) {
            distance = actorBoundingBox.getTop();
        }
        if (Math.abs(normal.y + 1.0f) < VectorMathUtils.DELTA) {
            distance = actorBoundingBox.getBottom();
        }
        if (Math.abs(normal.z - 1.0f) < VectorMathUtils.DELTA) {
            distance = actorBoundingBox.getFar();
        }
        if (Math.abs(normal.z + 1.0f) < VectorMathUtils.DELTA) {
            distance = actorBoundingBox.getNear();
        }
        Vector3f resultVector = new Vector3f(normal);
        VectorMathUtils.abs(resultVector);
        VectorMathUtils.mul(resultVector, distance);
        return resultVector;
    }

    private void sendNewPositionMessageToActor(int actorId, Vector3f newPosition) {
        messager.sendImmediateMessage(new ActorPositionChangeMessage(
                MessageTypes.CHANGE_ACTOR_POSITION, Message.MESSAGE_TARGET_ANYONE, newPosition,
                actorId));

    }

    public void registerActor(int id) {
        registeredActors.add(id);
    }

    public void removeActor(int id) {
        for (int i = 0; i < registeredActors.size(); ++i) {
            if (registeredActors.get(i) == id) {
                registeredActors.remove(i);
            }
        }
    }
}