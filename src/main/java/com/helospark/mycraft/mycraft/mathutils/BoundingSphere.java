package com.helospark.mycraft.mycraft.mathutils;

import org.lwjgl.util.vector.Vector3f;

public class BoundingSphere {
	Vector3f position;
	float radius;

	public BoundingSphere(Vector3f position, float radius) {
		this.position = position;
		this.radius = radius;
	}

	public Vector3f getPosition() {
		return position;
	}

	public float getRadius() {
		return radius;
	}

}
