package com.helospark.mycraft.mycraft.messages;

import com.helospark.mycraft.mycraft.mathutils.IntVector;
import com.helospark.mycraft.mycraft.mathutils.VectorMathUtils;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public class BlockDestroyingMessage extends Message {

	int blockId;
	IntVector position;
	int id;

	public BlockDestroyingMessage(MessageTypes blockDestroyingStarted,
			int messageTargetAnyone, int blockId,
			IntVector positionOfActiveBlock, int idWithSomeName) {
		super(blockDestroyingStarted, messageTargetAnyone);
		this.blockId = blockId;
		this.position = positionOfActiveBlock;
		this.id = idWithSomeName;
	}

	public BlockDestroyingMessage(MessageTypes blockDestroyingEnded) {
		// TODO Auto-generated constructor stub
	}

	public int getBlockId() {
		return blockId;
	}

	public IntVector getPosition() {
		return position;
	}

	public int getId() {
		return id;
	}

	@Override
	public String serializeToString() {
		String result = blockId + ";" + VectorMathUtils.serialize(position)
				+ ";" + id;
		return result;
	}

	@Override
	public Message deserializeFromString(String[] data, MessageTypes messageType) {
		if (data.length != 3) {
			throw new RuntimeException("Unable to deserialize");
		}
		int blockId = Integer.parseInt(data[0]);
		IntVector newPosition = VectorMathUtils.deserializeIntVector(data[1]);
		int id = Integer.parseInt(data[2]);
		BlockDestroyingMessage result = new BlockDestroyingMessage(messageType,
				Message.MESSAGE_TARGET_ANYONE, blockId, newPosition, id);
		return result;
	}

}
