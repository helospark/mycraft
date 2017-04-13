package com.helospark.mycraft.mycraft.messages;

import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public class ConnectToServerMessage extends Message {
	String userName;
	String ip;
	int port;

	public ConnectToServerMessage(MessageTypes messageType) {
		super(messageType, Message.MESSAGE_TARGET_ANYONE);
	}

	public ConnectToServerMessage(MessageTypes messageType, String userName,
			String ip, int port) {
		super(messageType, Message.MESSAGE_TARGET_ANYONE);
		this.ip = ip;
		this.port = port;
		this.userName = userName;
	}

	@Override
	public String serializeToString() {
		String result = userName + ";" + ip + ";" + port;
		return result;
	}

	@Override
	public Message deserializeFromString(String[] data, MessageTypes messageType) {
		String userName = data[0];
		String ip = data[1];
		int port = Integer.parseInt(data[2]);
		return new ConnectToServerMessage(messageType, userName, ip, port);
	}

}
