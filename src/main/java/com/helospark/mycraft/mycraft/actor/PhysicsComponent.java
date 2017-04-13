package com.helospark.mycraft.mycraft.actor;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;
import org.springframework.context.ApplicationContext;
import org.w3c.dom.Element;

import com.helospark.mycraft.mycraft.mathutils.BoundingBox;
import com.helospark.mycraft.mycraft.mathutils.VectorMathUtils;
import com.helospark.mycraft.mycraft.services.PhysicsResolverService;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageListener;

public class PhysicsComponent extends ActorComponent implements MessageListener {
	public static final String PHYSICS_COMPONENT_PROPERTY_NAME = "PhysicsComponent";
	private static final Vector3f MAXIMUM_VELOCITY = new Vector3f(1.00f, 1.00f, 1.00f);
	private static final Vector3f JUMP_IMPULSE_COMPONENT = new Vector3f(0, 0.25f, 0);

	PhysicsResolverService physics;
	BoundingBox boundingBox;
	Vector3f size;
	boolean shouldApplyGravity;
	boolean isMovable;
	Vector3f acceleration = new Vector3f();
	Vector3f velocity = new Vector3f();
	float mass = 1.0f;
	boolean isTouchGround = false;
	private Vector3f gravity = null;
	List<Vector3f> impulses = new ArrayList<>();
	Vector3f offset = new Vector3f();

	public PhysicsComponent(Vector3f size) {
		super(PHYSICS_COMPONENT_PROPERTY_NAME);
		ApplicationContext context = Singleton.getInstance().getContext();
		physics = context.getBean(PhysicsResolverService.class);
		this.size = size;
		boundingBox = BoundingBox.fromTwoPoints(new Vector3f(0, 0, 0), size);
	}

	@Override
	public Object createFromXML(Element node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean receiveMessage(Message message) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void afterInit() {
		int id = owner.getId();
		physics.registerActor(id);
	}

	@Override
	public void update(double deltaTime) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRemove() {
		physics.removeActor(owner.getId());
	}

	@Override
	public ActorComponent createNew() {
		return new PhysicsComponent(size);
	}

	public BoundingBox getBoundingBox() {
		TransformComponent transform = (TransformComponent) owner
				.getComponent(TransformComponent.TRANSFORM_COMPONENT_NAME);
		Vector3f actorPosition = transform.getPosition();
		return boundingBox.getBoundingBoxWithPosition(actorPosition.x + offset.x, actorPosition.y
				+ offset.y, actorPosition.z + offset.z);
	}

	public BoundingBox getBoundingBox(Vector3f position) {
		return boundingBox.getBoundingBoxWithPosition(position.x + offset.x, position.y + offset.y,
				position.z + offset.z);
	}

	public boolean isShouldApplyGravity() {
		return shouldApplyGravity;
	}

	public boolean isMovable() {
		return isMovable;
	}

	public void setShouldApplyGravity(boolean b) {
		shouldApplyGravity = b;
	}

	public void addImpulse(Vector3f force) {
		acceleration.x += (force.x) / mass;
		acceleration.y += (force.y) / mass;
		acceleration.z += (force.z) / mass;
	}

	public void addVelocity(Vector3f vel) {
		velocity.x += vel.x;
		velocity.y += vel.y;
		velocity.z += vel.z;
	}

	public void clearAccelerationOnVector(Vector3f vector) {
		if (Math.abs(vector.x) > VectorMathUtils.DELTA) {
			acceleration.x = 0.0f;
			velocity.x = 0.0f;
		}
		if (Math.abs(vector.y) > VectorMathUtils.DELTA) {
			acceleration.y = 0.0f;
			velocity.y = 0.0f;
		}
		if (Math.abs(vector.z) > VectorMathUtils.DELTA) {
			acceleration.z = 0.0f;
			velocity.z = 0.0f;
		}
	}

	public Vector3f getAcceleration() {
		return acceleration;
	}

	public Vector3f getVelocity() {
		return velocity;
	}

	public Vector3f calculateVelocity(float deltaTime) {
		velocity.x += acceleration.x * deltaTime;
		velocity.y += acceleration.y * deltaTime;
		velocity.z += acceleration.z * deltaTime;
		clampVelocityToMaximum();
		return velocity;
	}

	private void clampVelocityToMaximum() {
		if (Math.abs(velocity.x) > MAXIMUM_VELOCITY.x) {
			velocity.x = Math.signum(velocity.x) * MAXIMUM_VELOCITY.x;
		}
		if (Math.abs(velocity.y) > MAXIMUM_VELOCITY.y) {
			velocity.y = Math.signum(velocity.y) * MAXIMUM_VELOCITY.y;
		}
		if (Math.abs(velocity.z) > MAXIMUM_VELOCITY.z) {
			velocity.z = Math.signum(velocity.z) * MAXIMUM_VELOCITY.z;
		}
	}

	public boolean isTouchGround() {
		return isTouchGround;
	}

	public void setTouchGround(boolean touchGround) {
		this.isTouchGround = touchGround;
	}

	public void jump() {
		addVelocity(JUMP_IMPULSE_COMPONENT);
	}

	public void setGravity(Vector3f gravity) {
		this.gravity = gravity;
	}

	public Vector3f getGravity() {
		return gravity;
	}

	public void decreaseVelocity() {
		velocity.x /= 1.1f;
		velocity.y /= 1.1f;
		velocity.z /= 1.1f;
	}

	public void addOffset(Vector3f offset) {
		this.offset = offset;
		boundingBox = BoundingBox.fromTwoPoints(offset, size);
		// boundingBox.addOffset(offset);
	}
}
