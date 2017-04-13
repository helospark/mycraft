package com.helospark.mycraft.mycraft.mathutils;

import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;

public class VectorMathUtils {
	public static final Vector3f X_UNIT_VECTOR = new Vector3f(1.0f, 0.0f, 0.0f);
	public static final Vector3f Y_UNIT_VECTOR = new Vector3f(0.0f, 1.0f, 0.0f);
	public static final Vector3f Z_UNIT_VECTOR = new Vector3f(0.0f, 0.0f, 1.0f);
	public static final float DELTA = 0.00001f;

	public static Vector3f getLookVectorFromAngles(float pitch, float yaw) {
		Vector3f result = new Vector3f((float) (-Math.cos(pitch * Math.PI / 180.0) * Math.sin(yaw
				* Math.PI / 180.0)), (float) (Math.sin(pitch * Math.PI / 180.0)),
				(float) (-Math.cos(pitch * Math.PI / 180.0) * Math.cos(yaw * Math.PI / 180.0)));
		result.normalise();
		return result;
	}

	public static Vector3f getLookVectorFromAngles(Vector3f vector) {
		return getLookVectorFromAngles(vector.x, vector.y);
	}

	public static void mul(Vector3f result, float amount) {
		result.x *= amount;
		result.y *= amount;
		result.z *= amount;
	}

	public static void mulInPlace(Vector3f lhs, Vector3f rhs) {
		lhs.x *= rhs.x;
		lhs.y *= rhs.y;
		lhs.z *= rhs.z;
	}

	public static float getNotNullComponent(Vector3f vector) {
		if (Math.abs(vector.x) > DELTA) {
			return vector.x;
		}
		if (Math.abs(vector.y) > DELTA) {
			return vector.y;
		}
		if (Math.abs(vector.z) > DELTA) {
			return vector.z;
		}
		// throw new RuntimeException("Null vector");
		return 0.0f;
	}

	public static void addInPlace(Vector3f lhs, Vector3f rhs) {
		lhs.x += rhs.x;
		lhs.y += rhs.y;
		lhs.z += rhs.z;
	}

	public static void abs(Vector3f p1) {
		p1.x = Math.abs(p1.x);
		p1.y = Math.abs(p1.y);
		p1.z = Math.abs(p1.z);
	}

	public static Vector3f lerp(Vector3f oldPosition, Vector3f newPosition, float lertAmound) {
		Vector3f result = new Vector3f();
		result.x = oldPosition.x * (1.0f - lertAmound) + newPosition.x * (lertAmound);
		result.y = oldPosition.y * (1.0f - lertAmound) + newPosition.y * (lertAmound);
		result.z = oldPosition.z * (1.0f - lertAmound) + newPosition.z * (lertAmound);
		return result;
	}

	public static void subInPlace(Vector3f lhs, Vector3f rhs) {
		lhs.x -= rhs.x;
		lhs.y -= rhs.y;
		lhs.z -= rhs.z;
	}

	public static void addInPlaceAndMultiply(Vector3f lhs, Vector3f rhs, float factor) {
		lhs.x += rhs.x * factor;
		lhs.y += rhs.y * factor;
		lhs.z += rhs.z * factor;
	}

	public static String serialize(Vector3f position) {
		return position.x + " " + position.y + " " + position.z;
	}

	public static Vector3f deserialize(String string) {
		Vector3f vector = new Vector3f();
		String[] splittedString = string.split(" ");
		vector.x = Float.parseFloat(splittedString[0]);
		vector.y = Float.parseFloat(splittedString[1]);
		vector.z = Float.parseFloat(splittedString[2]);
		return vector;
	}

	public static String serialize(IntVector position) {
		return position.x + " " + position.y + " " + position.z;
	}

	public static IntVector deserializeIntVector(String string) {
		IntVector vector = new IntVector();
		String[] splittedString = string.split(" ");
		vector.x = Integer.parseInt(splittedString[0]);
		vector.y = Integer.parseInt(splittedString[1]);
		vector.z = Integer.parseInt(splittedString[2]);
		return vector;
	}

	public static float distanceSquareBetween(Vector3f position1, Vector3f position2) {
		float dx = position1.x - position2.x;
		float dy = position1.y - position2.y;
		float dz = position1.z - position2.z;
		return dx * dx + dy * dy + dz * dz;
	}

	public static int distanceSquareBetween(IntVector position1, IntVector position2) {
		int dx = position1.x - position2.x;
		int dy = position1.y - position2.y;
		int dz = position1.z - position2.z;
		return dx * dx + dy * dy + dz * dz;
	}

	public static Vector3f multiply(Quaternion quaternion, Vector3f v) {
		float k0 = quaternion.w * quaternion.w - 0.5f;
		float k1;
		float rx, ry, rz;

		// k1 = Q.V
		k1 = v.x * quaternion.x;
		k1 += v.y * quaternion.y;
		k1 += v.z * quaternion.z;

		// (qq-1/2)V+(Q.V)Q
		rx = v.x * k0 + quaternion.x * k1;
		ry = v.y * k0 + quaternion.y * k1;
		rz = v.z * k0 + quaternion.z * k1;

		// (Q.V)Q+(qq-1/2)V+q(QxV)
		rx += quaternion.w * (quaternion.y * v.z - quaternion.z * v.y);
		ry += quaternion.w * (quaternion.z * v.x - quaternion.x * v.z);
		rz += quaternion.w * (quaternion.x * v.y - quaternion.y * v.x);

		// 2((Q.V)Q+(qq-1/2)V+q(QxV))
		rx += rx;
		ry += ry;
		rz += rz;

		return new Vector3f(rx, ry, rz);
	}

	public static Vector3f multiply(Vector3f currentNormal, Quaternion orientation) {
		// TODO Auto-generated method stub
		return null;
	}

	public static com.badlogic.gdx.math.Quaternion toOtherQuaternion(Quaternion other) {
		com.badlogic.gdx.math.Quaternion result = new com.badlogic.gdx.math.Quaternion();
		result.x = other.x;
		result.y = other.y;
		result.z = other.z;
		result.w = other.w;
		return result;
	}

	public static void setQuaternion(Quaternion first, com.badlogic.gdx.math.Quaternion other) {
		first.x = other.x;
		first.y = other.y;
		first.z = other.z;
		first.w = other.w;
	}

	public static Vector3f mix(Vector3f position1, Vector3f position2, float interpolationValue) {
		Vector3f result = new Vector3f();
		result.x = position1.x * (1.0f - interpolationValue) + position2.x * (interpolationValue);
		result.y = position1.y * (1.0f - interpolationValue) + position2.y * (interpolationValue);
		result.z = position1.z * (1.0f - interpolationValue) + position2.z * (interpolationValue);
		return result;
	}

	public static double lengthSquared(Vector3f directionVector) {
		return directionVector.x * directionVector.x + directionVector.y * directionVector.y
				+ directionVector.z * directionVector.z;
	}
}
