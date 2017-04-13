package com.helospark.mycraft.mycraft.message;

import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public class GenericTwoIntMessage extends Message {
	int param1;
	int param2;

	public GenericTwoIntMessage(MessageTypes type, int target, int param1, int param2) {
		super(type, target);
		this.param1 = param1;
		this.param2 = param2;
	}

	@Override
	public String serializeToString() {
		return param1 + ";" + param2;
	}

	public int getParam1() {
		return param1;
	}

	public int getParam2() {
		return param2;
	}

	@Override
	public Message deserializeFromString(String[] data, MessageTypes messageType) {
		int param1 = Integer.parseInt(data[0]);
		int param2 = Integer.parseInt(data[1]);
		return new GenericTwoIntMessage(messageType, MESSAGE_TARGET_ANYONE, param1, param2);
	}

}
