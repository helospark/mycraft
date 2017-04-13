package com.helospark.mycraft.mycraft.mathutils;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.springframework.context.ApplicationContext;

import com.helospark.mycraft.mycraft.game.Camera;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.transformation.Transformation;

public class Frustum {
	private static final int NUMBER_OF_PLANES_ON_BOX = 6;
	Plane[] planes = new Plane[NUMBER_OF_PLANES_ON_BOX];
	Transformation transformation;
	int callCount = 0;
	Vector3f position = new Vector3f();

	public Frustum() {
		ApplicationContext context = Singleton.getInstance().getContext();
		transformation = context.getBean(Transformation.class);

		for (int i = 0; i < planes.length; ++i) {
			planes[i] = new Plane();
		}
	}

	public void extractPlanes(Camera activeCamera) {
		transformation.setMatrixMode(Transformation.VIEW_MATRIX);
		Matrix4f viewMatrix = transformation.getCurrentMatrix();

		transformation.setMatrixMode(Transformation.PROJECTION_MATRIX);
		Matrix4f projectionMatrix = transformation.getCurrentMatrix();

		Matrix4f res = new Matrix4f();
		Matrix4f.mul(projectionMatrix, viewMatrix, res);
		planes[0]
				.change(res.m03 - res.m00, res.m13 - res.m10, res.m23 - res.m20, res.m33 - res.m30);
		planes[1]
				.change(res.m03 + res.m00, res.m13 + res.m10, res.m23 + res.m20, res.m33 + res.m30);

		planes[2]
				.change(res.m03 - res.m01, res.m13 - res.m11, res.m23 - res.m21, res.m33 - res.m31);
		planes[3]
				.change(res.m03 + res.m01, res.m13 + res.m11, res.m23 + res.m21, res.m33 + res.m31);

		planes[4]
				.change(res.m03 - res.m02, res.m13 - res.m12, res.m23 - res.m22, res.m33 - res.m32);
		planes[5]
				.change(res.m03 + res.m02, res.m13 + res.m12, res.m23 + res.m22, res.m33 + res.m32);
		for (int i = 0; i < planes.length; i++) {
			planes[i].normalize();
		}
		callCount = 0;
		position = activeCamera.getPosition();
	}

	public boolean containsPoint(Vector3f point) {
		for (int i = 0; i < planes.length; ++i) {
			if (planes[i].testPoint(point) <= 0.0f) {
				return false;
			}
		}
		return true;
	}

	public boolean containsSphere(BoundingSphere sphere) {
		for (int i = 0; i < planes.length; ++i) {
			if (planes[i].testPoint(sphere.getPosition()) - sphere.getRadius() <= 0.0f) {
				return false;
			}
		}
		return true;
	}

	public CullingResult containsBox(BoundingBox box) {
		++callCount;
		int allPointIn = 0;
		int boxNumVertices = 8;
		for (int i = 0; i < NUMBER_OF_PLANES_ON_BOX; i++) {
			int pointOut = 0;
			int pointIn = 1;
			for (int j = 0; j < BoundingBox.NUMBER_OF_VERTICES; j++) {
				if (planes[i].testPoint(box.getVertex(j)) < -6.0f) {
					pointIn = 0;
					pointOut++;
				}
			}

			if (pointOut >= boxNumVertices) {
				return CullingResult.FULLY_OUT;
			}
			allPointIn += pointIn;
		}
		if (allPointIn == planes.length) {
			return CullingResult.FULLY_IN;
		}
		return CullingResult.PARTIALLY_IN;
	}

	public int getCallCount() {
		return callCount;
	}

	public Vector3f getPosition() {
		return position;
	}
}
