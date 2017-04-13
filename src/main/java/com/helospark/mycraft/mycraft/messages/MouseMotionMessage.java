package com.helospark.mycraft.mycraft.messages;

import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public class MouseMotionMessage extends Message {
	int oldX, oldY;
	int newX, newY;
	boolean wasDragged;

	public MouseMotionMessage(MessageTypes type, int targetId, int oldX,
			int oldY, int newX, int newY, boolean wasDragged) {
		super(type, targetId);
		this.oldX = oldX;
		this.oldY = oldY;
		this.newX = newX;
		this.newY = newY;
		this.wasDragged = wasDragged;
	}

	public void setDragged(boolean wasDragged) {
		this.wasDragged = wasDragged;
	}

	public int getNewX() {
		return newX;
	}

	public int getNewY() {
		return newY;
	}

	@Override
	public String serializeToString() {
		String result = oldX + ";" + oldY + ";" + newX + ";" + newY + ";"
				+ (wasDragged ? '1' : '0');
		return result;
	}

	@Override
	public Message deserializeFromString(String[] data, MessageTypes messageType) {
		int oldX = Integer.parseInt(data[0]);
		int oldY = Integer.parseInt(data[1]);
		int newX = Integer.parseInt(data[2]);
		int newY = Integer.parseInt(data[3]);
		boolean wasDragged = (Integer.parseInt(data[4]) == 1);
		MouseMotionMessage result = new MouseMotionMessage(messageType,
				MESSAGE_TARGET_ANYONE, oldX, oldY, newX, newY, wasDragged);
		return result;
	}

}
