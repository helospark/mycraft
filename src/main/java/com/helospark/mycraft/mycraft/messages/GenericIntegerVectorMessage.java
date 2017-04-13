package com.helospark.mycraft.mycraft.messages;

import com.helospark.mycraft.mycraft.mathutils.IntVector;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public class GenericIntegerVectorMessage extends Message {
	int x, y, z;

	public GenericIntegerVectorMessage(MessageTypes type, int targetId, int x,
			int y, int z) {
		super(type, targetId);
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public GenericIntegerVectorMessage(MessageTypes type, int targetId,
			IntVector originalPosition) {
		this(type, targetId, originalPosition.x, originalPosition.y,
				originalPosition.z);
	}

	public GenericIntegerVectorMessage(MessageTypes blockDestructionHandled) {
		// TODO Auto-generated constructor stub
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}

	@Override
	public String serializeToString() {
		String result = x + ";" + y + ";" + z;
		return result;
	}

	@Override
	public Message deserializeFromString(String[] data, MessageTypes messageType) {
		if (data.length != 3) {
			throw new RuntimeException("Unable to deserialize");
		}
		int newX = Integer.parseInt(data[0]);
		int newY = Integer.parseInt(data[1]);
		int newZ = Integer.parseInt(data[2]);
		GenericIntegerVectorMessage result = new GenericIntegerVectorMessage(
				messageType, Message.MESSAGE_TARGET_ANYONE, new IntVector(newX,
						newY, newZ));
		return result;
	}

	public IntVector getIntVectorDeserialized(String[] data) {
		IntVector result = new IntVector();
		result.x = Integer.parseInt(data[0]);
		result.y = Integer.parseInt(data[1]);
		result.z = Integer.parseInt(data[2]);
		return result;
	}

}
