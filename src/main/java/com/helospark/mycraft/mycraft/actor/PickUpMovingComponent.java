package com.helospark.mycraft.mycraft.actor;

import org.lwjgl.util.vector.Vector3f;
import org.springframework.context.ApplicationContext;
import org.w3c.dom.Element;

import com.helospark.mycraft.mycraft.messages.ActorLifeMessage;
import com.helospark.mycraft.mycraft.messages.CollisionMessage;
import com.helospark.mycraft.mycraft.services.ActorSearchService;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageHandler;
import com.helospark.mycraft.mycraft.window.MessageListener;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public class PickUpMovingComponent extends ActorComponent implements
		MessageListener {

	public static final String PICKUP_MOVING_COMPONENT_NAME = "PickupComponent";
	private static final Vector3f impulseVector = new Vector3f(0, 0.25f, 0);
	public static final float SPEED = 0.33f;
	public static final float MAX_DISTANCE = 0.3f;
	public static final float ROTATION_SPEED = 3f;
	private boolean isGoingUp = true;
	private float distance = 0;
	MessageHandler messager;
	ActorSearchService actorSearchService;
	InventoryItem inventoryItem;
	TransformComponent transformComponent;

	public PickUpMovingComponent(InventoryItem inventoryItem) {
		super(PICKUP_MOVING_COMPONENT_NAME);
		ApplicationContext applicationContext = Singleton.getInstance()
				.getContext();
		messager = applicationContext.getBean(MessageHandler.class);
		messager.registerListener(this, MessageTypes.COLLISION_MESSAGE);
		actorSearchService = applicationContext
				.getBean(ActorSearchService.class);
		this.inventoryItem = inventoryItem;
	}

	@Override
	public Object createFromXML(Element node) {
		return null;
	}

	@Override
	public void afterInit() {
		transformComponent = (TransformComponent) owner
				.getComponent(TransformComponent.TRANSFORM_COMPONENT_NAME);
	}

	@Override
	public void update(double deltaTime) {

		if (transformComponent == null) {
			throw new IllegalStateException(
					"Unable to use pickup without transform component");
		}
		PhysicsComponent physics = (PhysicsComponent) owner
				.getComponent(PhysicsComponent.PHYSICS_COMPONENT_PROPERTY_NAME);
		physics.setGravity(new Vector3f(0.0f, -0.015f, 0.0f));

		if (physics.isTouchGround) {
			physics.addImpulse(impulseVector);
		}

		transformComponent.getDirection().y += ROTATION_SPEED
				* (float) deltaTime;

	}

	@Override
	public void onRemove() {

	}

	@Override
	public ActorComponent createNew() {
		return new PickUpMovingComponent(inventoryItem);
	}

	@Override
	public boolean receiveMessage(Message message) {
		if (message.getType() == MessageTypes.COLLISION_MESSAGE) {
			handleCollisionMessage(message);
		}
		return false;
	}

	private void handleCollisionMessage(Message message) {
		CollisionMessage collisionMessage = (CollisionMessage) message;
		int id1 = collisionMessage.getCollidedObjectId(0);
		int id2 = collisionMessage.getCollidedObjectId(1);

		int thisId = (id1 == owner.getId() ? id1 : id2);
		int otherId = (id1 == owner.getId() ? id2 : id1);
		Actor otherActor = actorSearchService.findActorById(otherId);

		if (thisId == owner.getId()) {

			if (otherActor != null) {
				InventoryComponent inventory = (InventoryComponent) otherActor
						.getComponent(InventoryComponent.INVENTORY_COMPONENT_NAME);
				if (inventory != null) {
					inventory.addToInventory(inventoryItem);
					messager.sendImmediateMessage(new ActorLifeMessage(
							MessageTypes.DELETED_ACTOR_MESSAGE,
							Message.MESSAGE_TARGET_ANYONE, owner));
				}
			}
		}
	}
}
