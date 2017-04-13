package com.helospark.mycraft.mycraft.messages;

import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public class RenderComponentMessage extends Message {

	private String modelId;
	private int ownerId;

	public RenderComponentMessage(MessageTypes type, int targetId) {
		super(type, targetId);
	}

	public RenderComponentMessage(MessageTypes type, int targetId,
			String modelId, int id) {
		super(type, targetId);
		this.modelId = modelId;
		ownerId = id;
	}

	public RenderComponentMessage(MessageTypes newRenderComponent) {
		// TODO Auto-generated constructor stub
	}

	public int getOwnerId() {
		return ownerId;
	}

	public String getRenderableNodes() {
		return modelId;
	}

	@Override
	public String serializeToString() {
		String result = ownerId + ";" + modelId;
		return result;
	}

	@Override
	public Message deserializeFromString(String[] data, MessageTypes messageType) {
		int ownerId = Integer.parseInt(data[0]);
		String modelId = data[1];
		return new RenderComponentMessage(messageType, MESSAGE_TARGET_ANYONE,
				modelId, ownerId);
	}
}
