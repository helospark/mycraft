package com.helospark.mycraft.mycraft.messages;

import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public class ActorCommandMessage extends Message {

	private int ownerId;

	public ActorCommandMessage(MessageTypes type, int targetId, int id) {
		super(type, targetId);
		this.ownerId = id;
	}

	public int getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(int ownerId) {
		this.ownerId = ownerId;
	}

	@Override
	public String serializeToString() {
		String string = String.valueOf(ownerId);
		return string;
	}

	@Override
	public Message deserializeFromString(String[] data, MessageTypes messageType) {
		if (data.length != 1) {
			throw new RuntimeException("Unknown message");
		}
		int newOwnerId = Integer.parseInt(data[0]);
		ActorCommandMessage result = new ActorCommandMessage(messageType,
				Message.MESSAGE_TARGET_ANYONE, newOwnerId);
		return result;
	}

}
