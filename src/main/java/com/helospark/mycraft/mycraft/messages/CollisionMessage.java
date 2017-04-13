package com.helospark.mycraft.mycraft.messages;

import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public class CollisionMessage extends Message {

	private int[] actorIds = new int[2];

	public CollisionMessage(MessageTypes type, int targetId, int actorA,
			int actorB) {
		super(type, targetId);
		actorIds[0] = actorA;
		actorIds[1] = actorB;
	}

	public int getCollidedObjectId(int i) {
		return actorIds[i];
	}

	@Override
	public String serializeToString() {
		String result = actorIds[0] + ";" + actorIds[1];
		return result;
	}

	@Override
	public Message deserializeFromString(String[] data, MessageTypes messageType) {
		int a = Integer.parseInt(data[0]);
		int b = Integer.parseInt(data[1]);
		CollisionMessage result = new CollisionMessage(messageType,
				Message.MESSAGE_TARGET_ANYONE, a, b);

		return result;
	}

}
