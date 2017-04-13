package com.helospark.mycraft.mycraft.actor;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;
import org.springframework.context.ApplicationContext;
import org.w3c.dom.Element;

import com.helospark.mycraft.mycraft.mathutils.VectorMathUtils;
import com.helospark.mycraft.mycraft.message.GenericTwoIntMessage;
import com.helospark.mycraft.mycraft.messages.ActiveInventoryItemChangedMessage;
import com.helospark.mycraft.mycraft.messages.ActorCommandMessage;
import com.helospark.mycraft.mycraft.messages.MoveInDirectionMessage;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageHandler;
import com.helospark.mycraft.mycraft.window.MessageListener;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public class ActorMessageControllerComponent extends ActorComponent implements MessageListener {

	public static final String ACTOR_MESSAGE_COMPONENT_NAME = "ActorMessageControllerComponent";
	private MessageHandler messager;
	private GameItems gameItems;
	private float speed = 4.5f;
	private List<Message> handleLaterMessages = new ArrayList<>();

	public ActorMessageControllerComponent() {
		super(ACTOR_MESSAGE_COMPONENT_NAME);
		ApplicationContext context = Singleton.getInstance().getContext();
		gameItems = context.getBean(GameItems.class);
		messager = context.getBean(MessageHandler.class);
		messager.registerListener(this, MessageTypes.ACTOR_JUMP);
		messager.registerListener(this, MessageTypes.CHANGE_INVENTORY_ITEM);
		messager.registerListener(this, MessageTypes.MOVE_IN_DIRECTION);
	}

	@Override
	public Object createFromXML(Element node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void afterInit() {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(double deltaTime) {
		for (int i = 0; i < handleLaterMessages.size(); ++i) {
			Message message = handleLaterMessages.get(i);
			if (message.getType() == MessageTypes.MOVE_IN_DIRECTION) {
				handleActorMovement(deltaTime, message);
			}
		}
		handleLaterMessages.clear();
	}

	private void handleActorMovement(double deltaTime, Message message) {
		MoveInDirectionMessage directionMessage = (MoveInDirectionMessage) message;
		if (directionMessage.getActorId() == owner.id) {
			TransformComponent transform = (TransformComponent) owner
					.getComponent(TransformComponent.TRANSFORM_COMPONENT_NAME);
			if (transform != null) {
				Vector3f actorPosition = transform.getPosition();
				Vector3f amount = new Vector3f(directionMessage.getAmount());
				VectorMathUtils.mul(amount, speed * (float) deltaTime);
				Vector3f.add(actorPosition, amount, actorPosition);
				transform.setPosition(actorPosition);
			}

		}
	}

	@Override
	public void onRemove() {
		// TODO Auto-generated method stub

	}

	@Override
	public ActorComponent createNew() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean receiveMessage(Message message) {
		if (message.getType() == MessageTypes.ACTOR_JUMP) {
			ActorCommandMessage actorMessage = (ActorCommandMessage) message;
			if (actorMessage.getOwnerId() == owner.id) {
				PhysicsComponent physicsComponent = (PhysicsComponent) owner
						.getComponent(PhysicsComponent.PHYSICS_COMPONENT_PROPERTY_NAME);
				if (physicsComponent != null) {
					if (physicsComponent.isTouchGround()) {
						physicsComponent.jump();
					}
				}
			}
		} else if (message.getType() == MessageTypes.CHANGE_INVENTORY_ITEM) {
			handleInventoryElementChange(message);
		} else {
			handleLaterMessages.add(message);
		}
		return false;
	}

	private void handleInventoryElementChange(Message message) {
		GenericTwoIntMessage intMessage = (GenericTwoIntMessage) message;
		if (owner.id == intMessage.getParam2()) {
			InventoryComponent inventory = (InventoryComponent) owner
					.getComponent(InventoryComponent.INVENTORY_COMPONENT_NAME);
			inventory.changeActiveComponentWith(intMessage.getParam1());
			InventoryItem currentInventoryItem = inventory.getCurrentInventoryItem();
			GameItem currentGameItem = gameItems.getById(currentInventoryItem.getId());
			Message resultMessage = new ActiveInventoryItemChangedMessage(
					MessageTypes.INVENTORY_ACTIVE_ITEM_CHANGED, Message.MESSAGE_TARGET_ANYONE,
					currentGameItem, owner.id);
			messager.sendMessage(resultMessage);
		}
	}

}
