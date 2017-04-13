package com.helospark.mycraft.mycraft.messages;

import com.helospark.mycraft.mycraft.mathutils.IntVector;
import com.helospark.mycraft.mycraft.mathutils.VectorMathUtils;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public class ExplosionMessage extends Message {
	private int radius;
	private IntVector position;

	public ExplosionMessage(MessageTypes message) {
		super(message, MESSAGE_TARGET_ANYONE);
	}

	public ExplosionMessage(MessageTypes message, int target, int radius, IntVector position) {
		super(message, target);
		this.radius = radius;
		this.position = position;
	}

	@Override
	public String serializeToString() {
		return String.valueOf(radius) + ";" + VectorMathUtils.serialize(position);
	}

	@Override
	public Message deserializeFromString(String[] data, MessageTypes messageType) {
		int radius = Integer.parseInt(data[0]);
		IntVector position = VectorMathUtils.deserializeIntVector(data[1]);
		return new ExplosionMessage(messageType, MESSAGE_TARGET_ANYONE, radius, position);
	}

	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}

	public IntVector getPosition() {
		return position;
	}

	public void setPosition(IntVector position) {
		this.position = position;
	}
}
