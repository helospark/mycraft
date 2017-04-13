package com.helospark.mycraft.mycraft.messages;

import com.helospark.mycraft.mycraft.mathutils.IntVector;
import com.helospark.mycraft.mycraft.mathutils.VectorMathUtils;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public class NewBlockMessage extends Message {
	private IntVector position;
	private int blockId;

	public NewBlockMessage(MessageTypes type, int targetId, IntVector position,
			int blockId) {
		super(type, targetId);
		this.position = position;
		this.blockId = blockId;
	}

	public NewBlockMessage(MessageTypes blockCreatedMessage) {
		// TODO Auto-generated constructor stub
	}

	public IntVector getPosition() {
		return position;
	}

	public int getBlockId() {
		return blockId;
	}

	@Override
	public String serializeToString() {
		String result = VectorMathUtils.serialize(position) + ";" + blockId;
		return result;
	}

	@Override
	public Message deserializeFromString(String[] data, MessageTypes messageType) {
		IntVector position = VectorMathUtils.deserializeIntVector(data[0]);
		int blockId = Integer.parseInt(data[1]);
		return new NewBlockMessage(messageType, MESSAGE_TARGET_ANYONE,
				position, blockId);
	}

}
