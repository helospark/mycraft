package com.helospark.mycraft.mycraft.messages;

import org.lwjgl.util.vector.Vector3f;

import com.helospark.mycraft.mycraft.mathutils.VectorMathUtils;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public class ActorPositionChangeMessage extends Message {

	int actorId;
	Vector3f newPosition;

	public ActorPositionChangeMessage(MessageTypes type, int targetId,
			Vector3f newPosition, int actorId) {
		super(type, targetId);
		this.actorId = actorId;
		this.newPosition = newPosition;
	}

	public Vector3f getNewPosition() {
		return newPosition;
	}

	public int getActorId() {
		return actorId;
	}

	@Override
	public String serializeToString() {
		String result = actorId + ";" + VectorMathUtils.serialize(newPosition);
		return result;
	}

	@Override
	public Message deserializeFromString(String[] data, MessageTypes messageType) {

		int newActorId = Integer.parseInt(data[0]);
		Vector3f newNewPosition = VectorMathUtils.deserialize(data[1]);
		ActorPositionChangeMessage result = new ActorPositionChangeMessage(
				messageType, Message.MESSAGE_TARGET_ANYONE, newNewPosition,
				newActorId);

		return result;
	}
}
