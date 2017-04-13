package com.helospark.mycraft.mycraft.messages;

import org.lwjgl.util.vector.Vector3f;

import com.helospark.mycraft.mycraft.mathutils.VectorMathUtils;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public class ActorPositionChangedMessage extends Message {
	int actorId;
	private Vector3f position;
	private Vector3f oldPosition;

	public ActorPositionChangedMessage(MessageTypes type, int targetId,
			Vector3f position, Vector3f oldPosition, int actorId) {
		super(type, targetId);
		this.position = position;
		this.oldPosition = oldPosition;
		this.actorId = actorId;
	}

	public ActorPositionChangedMessage(MessageTypes actorPositionChanged) {

	}

	public Vector3f getNewPosition() {
		return position;
	}

	public int getActorId() {
		return actorId;
	}

	public Vector3f getOldPosition() {
		return oldPosition;
	}

	@Override
	public String serializeToString() {
		String result = actorId + ";" + VectorMathUtils.serialize(position)
				+ ";" + VectorMathUtils.serialize(oldPosition);
		return result;
	}

	@Override
	public Message deserializeFromString(String[] data, MessageTypes messageType) {
		if (data.length != 3) {
			throw new RuntimeException("Unable to deserialize");
		}
		int newActorId = Integer.parseInt(data[0]);
		Vector3f newPosition = VectorMathUtils.deserialize(data[1]);
		Vector3f newOldPosition = VectorMathUtils.deserialize(data[2]);
		ActorPositionChangedMessage result = new ActorPositionChangedMessage(
				messageType, Message.MESSAGE_TARGET_ANYONE, newPosition,
				newOldPosition, newActorId);
		return result;
	}
}
