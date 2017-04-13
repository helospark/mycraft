package com.helospark.mycraft.mycraft.messages;

import java.net.InetAddress;

import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public class NetworkMessage extends Message {
	Message message;
	InetAddress address;
	int port;

	public NetworkMessage(Message message, InetAddress address, int port) {
		super(MessageTypes.NETWORK_MESSAGE_RECEIVED,
				Message.MESSAGE_TARGET_ANYONE);
		this.message = message;
		this.address = address;
		this.port = port;
	}

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

	public InetAddress getAddress() {
		return address;
	}

	public void setAddress(InetAddress address) {
		this.address = address;
	}

	public int getPort() {
		return port;
	}

	@Override
	public String serializeToString() {
		// SHOULD NOT SEND THROUGH NETWORK
		return null;
	}

	@Override
	public Message deserializeFromString(String[] data, MessageTypes messageType) {
		// SHOULD NOT SEND THROUGH NETWORK
		return null;
	}

}
