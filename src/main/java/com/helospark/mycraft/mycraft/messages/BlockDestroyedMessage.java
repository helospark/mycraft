package com.helospark.mycraft.mycraft.messages;

import com.helospark.mycraft.mycraft.mathutils.BoundingBox;
import com.helospark.mycraft.mycraft.mathutils.IntVector;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public class BlockDestroyedMessage extends GenericIntegerVectorMessage {

	private int destroyer;
	private int blockId = -1;
	private BoundingBox boundingBox;
	private boolean willDrop = true;

	public BlockDestroyedMessage(MessageTypes type, int targetId, int x, int y, int z,
			BoundingBox boundingBox, boolean willDrop) {
		super(type, targetId, x, y, z);
		this.boundingBox = boundingBox;
		this.willDrop = willDrop;
	}

	public BlockDestroyedMessage(MessageTypes type, int targetId, IntVector originalPosition,
			int blockId, int ownerId, BoundingBox boundingBox, boolean willDrop) {
		super(type, targetId, originalPosition);
		this.destroyer = ownerId;
		this.blockId = blockId;
		this.boundingBox = boundingBox;
		this.willDrop = willDrop;
	}

	public BlockDestroyedMessage(MessageTypes blockDestroyedMessage) {
		super(blockDestroyedMessage);
	}

	public int getBlockId() {
		return blockId;
	}

	public BoundingBox getBoundingBox() {
		return boundingBox;
	}

	@Override
	public String serializeToString() {
		String result = super.serializeToString();
		result += ";" + destroyer + ";" + blockId + ";" + boundingBox.serialize() + ";"
				+ (willDrop ? 1 : 0);
		return result;
	}

	@Override
	public Message deserializeFromString(String[] data, MessageTypes messageType) {
		IntVector intVector = super.getIntVectorDeserialized(data);
		int newDestroyer = Integer.parseInt(data[3]);
		int newBlockId = Integer.parseInt(data[4]);
		BoundingBox newBoundingBox = BoundingBox.deserialize(data[5]);
		boolean willDrop = (Integer.parseInt(data[6]) == 1 ? true : false);

		BlockDestroyedMessage result = new BlockDestroyedMessage(messageType,
				Message.MESSAGE_TARGET_ANYONE, intVector, newBlockId, newDestroyer, newBoundingBox,
				willDrop);
		return result;
	}

	public boolean willDrop() {
		return willDrop;
	}

	public void setBlockId(int blockId) {
		this.blockId = blockId;
	}

}
