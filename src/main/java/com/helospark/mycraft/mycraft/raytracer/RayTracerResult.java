package com.helospark.mycraft.mycraft.raytracer;

import org.lwjgl.util.vector.Vector3f;

import com.helospark.mycraft.mycraft.actor.Actor;

public class RayTracerResult {

	private boolean hasFound = false;
	private int blockId;
	private float distance;
	private int side;
	RayTracableBox object;
	Vector3f collisionPoint;
	Vector3f position;
	boolean isBlock = true;
	Actor foundActor;

	public boolean hasFound() {
		return hasFound;
	}

	public void setBlockId(int id) {
		this.blockId = id;
	}

	public int getBlockId() {
		return blockId;
	}

	public double getDistance() {
		return distance;
	}

	public RayTracableBox getObject() {
		return object;
	}

	public void setSide(int side) {
		this.side = side;
	}

	public void setHasCollided(boolean b) {
		this.hasFound = b;
	}

	public void setDistance(float k) {
		this.distance = k;
	}

	public void setCollisionPoint(Vector3f collisionPosition) {
		this.collisionPoint = collisionPosition;
	}

	public Vector3f getCollisionPoint() {
		return collisionPoint;
	}

	public void initializeFrom(RayTracerResult rayTracerResult) {
		hasFound = rayTracerResult.hasFound;
		blockId = rayTracerResult.blockId;
		distance = rayTracerResult.distance;
		side = rayTracerResult.side;
		object = rayTracerResult.object;
		collisionPoint = rayTracerResult.collisionPoint;
		position = rayTracerResult.position;
		blockId = rayTracerResult.blockId;
		foundActor = rayTracerResult.foundActor;
		isBlock = rayTracerResult.isBlock;
	}

	public void setPosition(Vector3f position) {
		this.position = position;

	}

	public Vector3f getPosition() {
		return position;
	}

	public int getSide() {
		return side;
	}

	public boolean hasFoundBlock() {
		return isBlock;
	}

	public Actor getFoundActor() {
		return foundActor;
	}

	public void setActor(Actor actor) {
		this.foundActor = actor;
		this.isBlock = false;
	}

}
