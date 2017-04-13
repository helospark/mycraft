package com.helospark.mycraft.mycraft.messages;

import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public class GenericMessage extends Message {

	public GenericMessage(MessageTypes type, int targetId) {
		super(type, targetId);
	}

	@Override
	public String serializeToString() {
		return "";
	}

	@Override
	public Message deserializeFromString(String[] data, MessageTypes messageType) {
		return new GenericMessage(messageType, MESSAGE_TARGET_ANYONE);
	}

}
