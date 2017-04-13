package com.helospark.mycraft.mycraft.window;

public abstract class Message {
	public static final int MESSAGE_TARGET_ANYONE = -1;
	private final MessageTypes type;
	int targetId;
	int sourceId = -1;

	public MessageTypes getType() {
		return type;
	}

	public Message() {
		type = MessageTypes.NOTHING;
	}

	public Message(MessageTypes type, int targetId) {
		this.type = type;
	}

	public int getTargetId() {
		return targetId;
	}

	public void setSource(int id) {
		this.sourceId = id;
	}

	public int getSource() {
		return sourceId;
	}

	public abstract String serializeToString();

	public abstract Message deserializeFromString(String[] data, MessageTypes messageType);
}
