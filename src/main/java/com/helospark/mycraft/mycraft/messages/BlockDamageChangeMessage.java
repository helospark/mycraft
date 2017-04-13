package com.helospark.mycraft.mycraft.messages;

import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public class BlockDamageChangeMessage extends Message {
	int damage, blockId, actorId;

	public BlockDamageChangeMessage(MessageTypes type, int targetId,
			int damage, int blockId, int actorId) {
		super(type, targetId);
		this.damage = damage;
		this.blockId = blockId;
		this.actorId = actorId;
	}

	public BlockDamageChangeMessage(MessageTypes blockDamageChangedMessage) {
		// TODO Auto-generated constructor stub
	}

	public int getDamage() {
		return damage;
	}

	public int getBlockId() {
		return blockId;
	}

	@Override
	public String serializeToString() {
		String result = damage + ";" + blockId + ";" + actorId;
		return result;
	}

	@Override
	public Message deserializeFromString(String[] data, MessageTypes messageType) {
		if (data.length != 3) {
			throw new RuntimeException("Unable to deserialize");
		}
		int newDamage = Integer.parseInt(data[0]);
		int newBlockId = Integer.parseInt(data[1]);
		int actorId = Integer.parseInt(data[2]);
		BlockDamageChangeMessage result = new BlockDamageChangeMessage(
				messageType, Message.MESSAGE_TARGET_ANYONE, newDamage,
				newBlockId, actorId);
		return result;
	}

	public int getOwnerId() {
		return actorId;
	}

}
