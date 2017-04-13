package com.helospark.mycraft.mycraft.messages;

import com.helospark.mycraft.mycraft.mathutils.IntVector;
import com.helospark.mycraft.mycraft.mathutils.VectorMathUtils;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public class BlockChangeMessage extends Message {

	private IntVector position;
	private int newBlockId;

	public BlockChangeMessage(MessageTypes type, int targetId,
			IntVector position, int newBlockId) {
		super(type, targetId);
		this.position = position;
		this.newBlockId = newBlockId;
	}

	public IntVector getPosition() {
		return position;
	}

	public void setPosition(IntVector position) {
		this.position = position;
	}

	public int getNewBlockId() {
		return newBlockId;
	}

	public void setNewBlockId(int newBlockId) {
		this.newBlockId = newBlockId;
	}

	@Override
	public String serializeToString() {
		String result = VectorMathUtils.serialize(position) + ";" + newBlockId;
		return result;
	}

	@Override
	public Message deserializeFromString(String[] data, MessageTypes messageType) {
		if (data.length != 2) {
			throw new RuntimeException("Unable to deserialize");
		}
		IntVector newPosition = VectorMathUtils.deserializeIntVector(data[0]);
		int newBlockId = Integer.parseInt(data[1]);
		BlockChangeMessage result = new BlockChangeMessage(messageType,
				Message.MESSAGE_TARGET_ANYONE, newPosition, newBlockId);
		return result;
	}
}
