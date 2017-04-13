package com.helospark.mycraft.mycraft.messages;

import org.lwjgl.util.vector.Vector3f;

import com.helospark.mycraft.mycraft.mathutils.VectorMathUtils;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public class HitActorMessage extends Message {

	private Vector3f direction;
	private int hitActor;
	private int effecter;
	private float strength;

	public HitActorMessage(MessageTypes messageType, Vector3f direction, float strength,
			int hitActor, int effecter) {
		super(messageType, MESSAGE_TARGET_ANYONE);
		this.direction = direction;
		this.hitActor = hitActor;
		this.effecter = effecter;
		this.strength = strength;
	}

	@Override
	public String serializeToString() {
		return VectorMathUtils.serialize(direction) + ";" + hitActor + ";" + effecter + ";"
				+ strength;
	}

	@Override
	public Message deserializeFromString(String[] data, MessageTypes messageType) {
		Vector3f direction = VectorMathUtils.deserialize(data[0]);
		int hitActor = Integer.parseInt(data[1]);
		int effecter = Integer.parseInt(data[2]);
		float strength = Float.parseFloat(data[3]);
		return new HitActorMessage(messageType, direction, strength, hitActor, effecter);
	}

	public Vector3f getDirection() {
		return direction;
	}

	public void setDirection(Vector3f direction) {
		this.direction = direction;
	}

	public int getHitActor() {
		return hitActor;
	}

	public void setHitActor(int hitActor) {
		this.hitActor = hitActor;
	}

	public int getEffecter() {
		return effecter;
	}

	public void setEffecter(int effecter) {
		this.effecter = effecter;
	}

	public float getStrength() {
		return strength;
	}

	public void setStrength(float strength) {
		this.strength = strength;
	}

}
