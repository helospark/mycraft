package com.helospark.mycraft.mycraft.messages;

import org.lwjgl.util.vector.Vector3f;

import com.helospark.mycraft.mycraft.mathutils.VectorMathUtils;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public class MoveInDirectionMessage extends Message {

	private int actorId;
	private Vector3f amount;

	public MoveInDirectionMessage(MessageTypes type, int actorId, Vector3f amount) {
		super(type, Message.MESSAGE_TARGET_ANYONE);
		this.actorId = actorId;
		this.amount = amount;
	}

	@Override
	public String serializeToString() {
		return actorId + ";" + VectorMathUtils.serialize(amount);
	}

	@Override
	public Message deserializeFromString(String[] data, MessageTypes messageType) {
		int actorId = Integer.parseInt(data[0]);
		Vector3f amount = VectorMathUtils.deserialize(data[1]);
		return new MoveInDirectionMessage(messageType, actorId, amount);
	}

	public int getActorId() {
		return actorId;
	}

	public Vector3f getAmount() {
		return amount;
	}

}
