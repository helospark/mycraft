package com.helospark.mycraft.mycraft.actor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.lwjgl.input.Keyboard;
import org.springframework.context.ApplicationContext;
import org.w3c.dom.Element;

import com.helospark.mycraft.mycraft.message.GenericTwoIntMessage;
import com.helospark.mycraft.mycraft.messages.ActorCommandMessage;
import com.helospark.mycraft.mycraft.messages.GenericIntMessage;
import com.helospark.mycraft.mycraft.messages.KeyStateChangedMessage;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageHandler;
import com.helospark.mycraft.mycraft.window.MessageListener;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public class ActorKeyboardControllerComponent extends ActorComponent implements MessageListener {

	public static final String ACTOR_KEYBOARD_COMPONENT_NAME = "ActorKeyboardComponent";
	private Map<Integer, MessageTypes> keyToMessageCodes = new HashMap<>();
	private Set<Integer> pressedKeys = new TreeSet<>();
	MessageHandler messager;

	public ActorKeyboardControllerComponent() {
		super(ACTOR_KEYBOARD_COMPONENT_NAME);
		keyToMessageCodes.put(Keyboard.KEY_W, MessageTypes.ACTOR_POSITION_FORWARD);
		keyToMessageCodes.put(Keyboard.KEY_S, MessageTypes.ACTOR_POSITION_BACKWARD);
		keyToMessageCodes.put(Keyboard.KEY_S, MessageTypes.ACTOR_POSITION_LEFT);
		keyToMessageCodes.put(Keyboard.KEY_D, MessageTypes.ACTOR_POSITION_RIGHT);
		keyToMessageCodes.put(Keyboard.KEY_SPACE, MessageTypes.ACTOR_JUMP);

		ApplicationContext context = Singleton.getInstance().getContext();
		messager = context.getBean(MessageHandler.class);
		messager.registerListener(this, MessageTypes.KEY_STATE_CHANGE_MESSAGE);
		messager.registerListener(this, MessageTypes.MOUSE_SCROLLED_MESSAGE);
	}

	@Override
	public Object createFromXML(Element node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void afterInit() {

	}

	@Override
	public void update(double deltaTime) {
		for (Integer pressedKey : pressedKeys) {
			MessageTypes messageType = keyToMessageCodes.get(pressedKey);
			if (messageType != null) {
				messager.sendImmediateMessage(new ActorCommandMessage(messageType,
						Message.MESSAGE_TARGET_ANYONE, owner.id));
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
		if (message.getType() == MessageTypes.KEY_STATE_CHANGE_MESSAGE) {
			KeyStateChangedMessage keyMessage = (KeyStateChangedMessage) message;
			if (keyMessage.getKeyStateChangeType() == KeyStateChangedMessage.KEY_PRESSED) {
				pressedKeys.add(keyMessage.getKeyCode());
			} else {
				pressedKeys.remove(keyMessage.getKeyCode());
			}
		} else if (message.getType() == MessageTypes.MOUSE_SCROLLED_MESSAGE) {
			GenericIntMessage mouseScrolledMessage = (GenericIntMessage) message;
			Message inventoryChangeMessage = new GenericTwoIntMessage(
					MessageTypes.CHANGE_INVENTORY_ITEM, Message.MESSAGE_TARGET_ANYONE,
					mouseScrolledMessage.getParameter(), owner.id);
			messager.sendMessage(inventoryChangeMessage);
		}
		return false;
	}
}
