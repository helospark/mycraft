package com.helospark.mycraft.mycraft.messages;

import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public class GenericIntMessage extends Message {

	int parameter;

	public GenericIntMessage(MessageTypes type, int targetId, int parameter) {
		super(type, targetId);
		this.parameter = parameter;
	}

	public int getParameter() {
		return parameter;
	}

	@Override
	public String serializeToString() {
		String result = String.valueOf(parameter);
		return result;
	}

	@Override
	public Message deserializeFromString(String[] data, MessageTypes messageType) {
		if (data.length != 1) {
			throw new RuntimeException("Unable to deserialize");
		}
		int parameter = Integer.parseInt(data[0]);
		GenericIntMessage result = new GenericIntMessage(messageType,
				Message.MESSAGE_TARGET_ANYONE, parameter);
		return result;
	}
}
