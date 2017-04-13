package com.helospark.mycraft.mycraft.messages;

import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public class MouseStateChangeMessage extends Message {

	public static final int MOUSE_BUTTON_RELEASED = 0;
	public static final int MOUSE_BUTTON_PRESSED = 1;
	public static final int MOUSE_BUTTON_CLICKED = 2;

	public static final int MOUSE_BUTTON_LEFT = 0;
	public static final int MOUSE_BUTTON_MIDDLE = 1;
	public static final int MOUSE_BUTTON_RIGHT = 2;

	private int positionX;
	private int positionY;
	private int button;
	private int eventType;

	public MouseStateChangeMessage(MessageTypes type, int targetId,
			int positionX, int positionY, int eventType, int button) {
		super(type, targetId);
		this.positionX = positionX;
		this.positionY = positionY;
		this.button = button;
		this.eventType = eventType;
	}

	public int getX() {
		return positionX;
	}

	public int getY() {
		return positionY;
	}

	public int getMouseMessageType() {
		return eventType;
	}

	public int getMouseEventType() {
		return eventType;
	}

	public int getButton() {
		return button;
	}

	@Override
	public String serializeToString() {
		String result = positionX + ";" + positionY + ";" + button + ";"
				+ eventType;
		return result;
	}

	@Override
	public Message deserializeFromString(String[] data, MessageTypes messageType) {
		int positionX = Integer.parseInt(data[0]);
		int positionY = Integer.parseInt(data[1]);
		int button = Integer.parseInt(data[2]);
		int eventType = Integer.parseInt(data[3]);
		return new MouseStateChangeMessage(messageType, MESSAGE_TARGET_ANYONE,
				positionX, positionY, eventType, button);
	}
}
