package com.helospark.mycraft.mycraft.messages;

import org.lwjgl.util.vector.Vector3f;

import com.helospark.mycraft.mycraft.mathutils.VectorMathUtils;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public class UserConnectionResultMessage extends Message {
	Vector3f initialPosition;
	int idLowerRange, idUpperRange;
	int seed;

	public UserConnectionResultMessage(MessageTypes messageType) {
		super(messageType, Message.MESSAGE_TARGET_ANYONE);
	}

	public UserConnectionResultMessage(MessageTypes messageType,
			Vector3f initialPosition, int idLowerRange, int idUpperRange,
			int seed) {
		super(messageType, Message.MESSAGE_TARGET_ANYONE);
		this.initialPosition = initialPosition;
		this.seed = seed;
		this.idLowerRange = idLowerRange;
		this.idUpperRange = idUpperRange;
	}

	@Override
	public String serializeToString() {
		String result = VectorMathUtils.serialize(initialPosition) + ";"
				+ idLowerRange + ";" + idUpperRange + ";" + seed;
		return result;
	}

	@Override
	public Message deserializeFromString(String[] data, MessageTypes messageType) {
		Vector3f initialialPosition = VectorMathUtils.deserialize(data[0]);
		int idLowerRange = Integer.parseInt(data[1]);
		int idUpperRange = Integer.parseInt(data[2]);
		int seed = Integer.parseInt(data[3]);
		return new UserConnectionResultMessage(messageType, initialialPosition,
				idLowerRange, idUpperRange, seed);
	}

	public Vector3f getInitialPosition() {
		return initialPosition;
	}

	public void setInitialPosition(Vector3f initialPosition) {
		this.initialPosition = initialPosition;
	}

	public int getIdLowerRange() {
		return idLowerRange;
	}

	public void setIdLowerRange(int idLowerRange) {
		this.idLowerRange = idLowerRange;
	}

	public int getIdUpperRange() {
		return idUpperRange;
	}

	public void setIdUpperRange(int idUpperRange) {
		this.idUpperRange = idUpperRange;
	}

	public int getSeed() {
		return seed;
	}

	public void setSeed(int seed) {
		this.seed = seed;
	}
}
