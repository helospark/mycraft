package com.helospark.mycraft.mycraft.mathutils;

import org.lwjgl.util.vector.Vector3f;

import com.helospark.mycraft.mycraft.raytracer.RayTracerResult;

public class Plane {
	private static final double FLOAT_DELTA = 0.00001;

	float d;

	Vector3f p1, p2, p3;
	Vector3f normal, negativeNormal;
	float distance, negativeDistance;

	public Plane() {

	}

	public Plane(Vector3f planePoint, Vector3f normal) {
		this.normal = normal;
		this.p1 = planePoint;
	}

	public Plane(float distance, int x, int y, int z) {
		this.normal = new Vector3f(x, y, z);
		this.p1 = new Vector3f(normal);
		VectorMathUtils.abs(p1);
		VectorMathUtils.mul(p1, distance);
	}

	public void change(float x, float y, float z, float d) {
		this.normal = new Vector3f(x, y, z);
		this.d = d;
	}

	public void normalize() {
		float length = normal.length();
		normal.x /= length;
		normal.y /= length;
		normal.z /= length;
		d /= length;
	}

	public float testPoint(Vector3f point) {
		float result = Vector3f.dot(normal, point) + d;
		return result;
	}

	public void change(Vector3f vector, float d) {
		vector.normalise(normal);
		this.d = d;
		p1 = new Vector3f(vector);
		p1.x *= d;
		p1.y *= d;
		p1.z *= d;
		Vector3f[] vectors = new Vector3f[2];
		int vectorIndex = 0;
		if (Math.abs(vector.x) <= FLOAT_DELTA) {
			vectors[vectorIndex++] = new Vector3f(1.0f, 0.0f, 0.0f);
		}
		if (Math.abs(vector.y) <= FLOAT_DELTA) {
			vectors[vectorIndex++] = new Vector3f(0.0f, 1.0f, 0.0f);
		}
		if (Math.abs(vector.z) <= FLOAT_DELTA) {
			vectors[vectorIndex++] = new Vector3f(0.0f, 0.0f, 1.0f);
		}
		p2 = new Vector3f();
		p3 = new Vector3f();

		Vector3f.add(p1, vectors[0], p2);
		Vector3f.add(p1, vectors[1], p3);

		normal = vector;

		init();
	}

	private void init() {
		Vector3f d1 = new Vector3f();
		Vector3f.sub(p1, p2, d1);

		Vector3f d2 = new Vector3f();
		Vector3f.sub(p1, p3, d2);

		Vector3f d3 = new Vector3f();
		Vector3f.sub(p2, p3, d3);

		Vector3f.cross(d1, d2, normal);
		normal.normalise();

		negativeNormal = new Vector3f(normal);
		negativeNormal.x *= -1.0f;
		negativeNormal.y *= -1.0f;
		negativeNormal.z *= -1.0f;

		calculateDistance();
	}

	private void calculateDistance() {
		distance = Vector3f.dot(normal, p1);
		negativeDistance = Vector3f.dot(negativeNormal, p1);
	}

	public void traceRays(Vector3f rayPosition, Vector3f rayDirection,
			RayTracerResult rayTraceResult, float left, float right, float top,
			float bottom) {
		doCalculation(rayPosition, normal, distance, rayDirection,
				rayTraceResult);
		if (rayTraceResult.hasFound()) {
			if (!checkOrthogonalPlaneCollision(rayTraceResult, left, right,
					top, bottom)) {
				rayTraceResult.setHasCollided(false);
			}
		}
		if (!rayTraceResult.hasFound()) {
			doCalculation(rayPosition, negativeNormal, negativeDistance,
					rayDirection, rayTraceResult);
			if (rayTraceResult.hasFound()) {
				if (!checkOrthogonalPlaneCollision(rayTraceResult, left, right,
						top, bottom)) {
					rayTraceResult.setHasCollided(false);
				}
			}
		}
	}

	private boolean checkOrthogonalPlaneCollision(
			RayTracerResult rayTraceResult, float left, float right, float top,
			float bottom) {
		Vector3f collisionPoint = rayTraceResult.getCollisionPoint();

		float[] values = new float[4];
		int index = 0;
		values[0] = left;
		values[1] = right;
		values[2] = top;
		values[3] = bottom;

		if (Math.abs(normal.x) <= FLOAT_DELTA) {
			if (collisionPoint.x <= values[index++]
					|| collisionPoint.x >= values[index++]) {
				return false;
			}
		}
		if (Math.abs(normal.y) <= FLOAT_DELTA) {
			if (collisionPoint.y <= values[index++]
					|| collisionPoint.y >= values[index++]) {
				return false;
			}
		}
		if (Math.abs(normal.z) <= FLOAT_DELTA) {
			if (collisionPoint.z <= values[index++]
					|| collisionPoint.z >= values[index++]) {
				return false;
			}
		}
		return true;
	}

	public void doCalculation(Vector3f rayPosition, Vector3f pNormal,
			double pDistance, Vector3f rayDirection,
			RayTracerResult rayTraceResult) {
		double nominator = pDistance - Vector3f.dot(pNormal, rayPosition);
		double denominator = Vector3f.dot(rayDirection, pNormal);
		if (denominator <= 0.000001) {
			rayTraceResult.setHasCollided(false);
		} else {
			double k = nominator / denominator;
			if (k > 0.0) {
				Vector3f collisionPosition = new Vector3f();
				collisionPosition.x = (float) (rayPosition.x + k
						* rayDirection.x);
				collisionPosition.y = (float) (rayPosition.y + k
						* rayDirection.y);
				collisionPosition.z = (float) (rayPosition.z + k
						* rayDirection.z);
				rayTraceResult.setHasCollided(true);
				rayTraceResult.setDistance((float) k);
				rayTraceResult.setPosition(collisionPosition);
				rayTraceResult.setCollisionPoint(collisionPosition);
			}
		}
	}

	public Vector3f getNormal() {
		return normal;
	}

	public float getCollisionDistance(Plane plane) {
		Vector3f pointOnPlane = new Vector3f(plane.getPointOnPlane());
		// VectorMathUtils.mulInPlace(pointOnPlane, plane.getNormal());

		if (Vector3f.dot(normal, plane.normal) > VectorMathUtils.DELTA) {
			return Float.MAX_VALUE;
		}

		if (Math.abs(plane.getNormal().x) > VectorMathUtils.DELTA) {
			float point = pointOnPlane.x - p1.x;
			return Math.abs(point);
		}
		if (Math.abs(plane.getNormal().y) > VectorMathUtils.DELTA) {
			float point = pointOnPlane.y - p1.y;
			return Math.abs(point);
		}
		if (Math.abs(plane.getNormal().z) > VectorMathUtils.DELTA) {
			float point = pointOnPlane.z - p1.z;
			return Math.abs(point);
		}
		return Float.MAX_VALUE;
	}

	public Vector3f getPointOnPlane() {
		return p1;
	}
}