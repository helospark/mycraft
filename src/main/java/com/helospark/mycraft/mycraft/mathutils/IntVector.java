package com.helospark.mycraft.mycraft.mathutils;

import org.lwjgl.util.vector.Vector3f;

public class IntVector {
	public int x;
	public int y;
	public int z;

	public IntVector(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public IntVector(IntVector other) {
		this.x = other.x;
		this.y = other.y;
		this.z = other.z;
	}

	public IntVector() {
		// TODO Auto-generated constructor stub
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getZ() {
		return z;
	}

	public void setZ(int z) {
		this.z = z;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		result = prime * result + z;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IntVector other = (IntVector) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		if (z != other.z)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "IntVector [x=" + x + ", y=" + y + ", z=" + z + "]";
	}

	public IntVector add(IntVector sideVector) {
		this.x += sideVector.x;
		this.y += sideVector.y;
		this.z += sideVector.z;
		return this;
	}

	public Vector3f toVector3f() {
		return new Vector3f(x, y, z);
	}

	public static IntVector valueOf(String dataString) {
		String[] splittedString = dataString.split("\\s");
		if (splittedString.length > 3) {
			throw new RuntimeException("Unable to parse IntVector from more than 3 data");
		}
		if (splittedString.length < 2) {
			throw new RuntimeException("Unable to parse IntVector from less than 2 data");
		}
		IntVector result = new IntVector();
		result.x = Integer.parseInt(splittedString[0]);
		result.y = Integer.parseInt(splittedString[1]);
		if (splittedString.length == 3) {
			result.z = Integer.parseInt(splittedString[2]);
		} else {
			result.z = 0;
		}
		return result;
	}

	public void set(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public int lengthSquared() {
		return x * x + y * y + z * z;
	}
}
