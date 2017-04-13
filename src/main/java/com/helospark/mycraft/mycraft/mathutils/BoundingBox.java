package com.helospark.mycraft.mycraft.mathutils;

import org.lwjgl.util.vector.Vector3f;

public class BoundingBox {
	public static final int NUMBER_OF_VERTICES = 8;
	private float left, right, top, bottom, near, far;
	private Vector3f[] vertices = new Vector3f[NUMBER_OF_VERTICES];

	public BoundingBox() {
		left = right = top = bottom = near = far = 0.0f;
		calculateVertices();
	}

	public BoundingBox(Vector3f position, float size) {

		calculateVertices();
	}

	public BoundingBox(Vector3f position, float size, float size2, float size3) {

		calculateVertices();
	}

	public BoundingBox(BoundingBox boundingBox) {
		left = boundingBox.left;
		right = boundingBox.right;
		top = boundingBox.top;
		bottom = boundingBox.bottom;
		near = boundingBox.near;
		far = boundingBox.far;

		for (int i = 0; i < NUMBER_OF_VERTICES; ++i) {
			vertices[i] = new Vector3f(boundingBox.vertices[i]);
		}
	}

	private void calculateVertices() {

	}

	public CullingResult containsBox(BoundingBox boundingBox) {

		if (this.left > boundingBox.right)
			return CullingResult.FULLY_OUT;
		if (this.right < boundingBox.left)
			return CullingResult.FULLY_OUT;
		if (this.top < boundingBox.bottom)
			return CullingResult.FULLY_OUT;
		if (this.bottom > boundingBox.top)
			return CullingResult.FULLY_OUT;
		if (this.near > boundingBox.far)
			return CullingResult.FULLY_OUT;
		if (this.far < boundingBox.near)
			return CullingResult.FULLY_OUT;

		// TODO: partial check here

		return CullingResult.FULLY_IN;
	}

	public Vector3f getVertex(int j) {
		return vertices[j];
	}

	public static BoundingBox fromTwoPoints(Vector3f position, Vector3f bottomRightPosition) {
		BoundingBox boundingBox = new BoundingBox();
		boundingBox.setVertex(0, position);
		boundingBox.setVertex(1, new Vector3f(position.x, position.y, bottomRightPosition.z));
		boundingBox.setVertex(2, new Vector3f(position.x, bottomRightPosition.y, position.z));
		boundingBox.setVertex(3, new Vector3f(position.x, bottomRightPosition.y,
				bottomRightPosition.z));
		boundingBox.setVertex(4, new Vector3f(bottomRightPosition.x, position.y, position.z));
		boundingBox.setVertex(5, new Vector3f(bottomRightPosition.x, position.y,
				bottomRightPosition.z));
		boundingBox.setVertex(6, new Vector3f(bottomRightPosition.x, bottomRightPosition.y,
				position.z));
		boundingBox.setVertex(7, bottomRightPosition);

		boundingBox.left = position.x;
		boundingBox.bottom = position.y;
		boundingBox.near = position.z;
		boundingBox.right = bottomRightPosition.x;
		boundingBox.top = bottomRightPosition.y;
		boundingBox.far = bottomRightPosition.z;

		return boundingBox;
	}

	private void setVertex(int i, Vector3f position) {
		vertices[i] = position;
	}

	public void setPosition(float x, float y, float z) {
		for (Vector3f vertex : vertices) {
			vertex.x = vertex.x + x;
			vertex.y = vertex.y + y;
			vertex.z = vertex.z + z;
		}
		left += x;
		right += x;
		top += y;
		bottom += y;
		near += z;
		far += z;
	}

	public BoundingBox getBoundingBoxWithPosition(float f, float g, float h) {
		return BoundingBox.fromTwoPoints(new Vector3f(f, g, h), new Vector3f(right + f, top + g,
				far + h));
	}

	public float getLeft() {
		return left;
	}

	public float getRight() {
		return right;
	}

	public float getTop() {
		return top;
	}

	public float getBottom() {
		return bottom;
	}

	public float getNear() {
		return near;
	}

	public float getFar() {
		return far;
	}

	public boolean isPlaneInside(BoundingBox actorBox, Plane actorPlane) {

		boolean a = false, b = false, c = false;

		if (Math.abs(actorPlane.getNormal().x) > VectorMathUtils.DELTA) {
			if (isBetween(actorPlane.getPointOnPlane().x, getLeft(), getRight())) {
				a = true;
				b = checkY(actorBox);
				c = checkZ(actorBox);
			}
		} else if (Math.abs(actorPlane.getNormal().y) > VectorMathUtils.DELTA) {
			if (isBetween(actorPlane.getPointOnPlane().y, getBottom(), getTop())) {
				a = checkX(actorBox);
				b = true;
				c = checkZ(actorBox);
			}
		} else if (Math.abs(actorPlane.getNormal().z) > VectorMathUtils.DELTA) {
			if (isBetween(actorPlane.getPointOnPlane().z, getNear(), getFar())) {
				a = checkX(actorBox);
				b = checkY(actorBox);
				c = true;
			}
		}

		return a && b && c;
	}

	private boolean isBetween(float point, float near, float far) {
		return point >= near && point <= far;
	}

	private boolean checkX(BoundingBox actorPlane) {
		return isIn(actorPlane.getLeft(), actorPlane.getRight(), getLeft(), getRight());
	}

	private boolean checkZ(BoundingBox actorPlane) {
		return isIn(actorPlane.getNear(), actorPlane.getFar(), getNear(), getFar());

	}

	private boolean checkY(BoundingBox actorPlane) {
		return isIn(actorPlane.getBottom(), actorPlane.getTop(), getBottom(), getTop());
	}

	private boolean isIn(float pointBottom, float pointTop, float bottom, float top) {
		return (pointBottom >= bottom && pointBottom <= top)
				|| (pointTop <= top && pointTop >= bottom);
	}

	public float calculateArea(BoundingBox box, Plane actorPlane, Plane boxPlane) {

		if (Vector3f.dot(actorPlane.getNormal(), boxPlane.getNormal()) > -VectorMathUtils.DELTA) {
			return -1.0f;
		}

		if (Math.abs(actorPlane.getNormal().x) > VectorMathUtils.DELTA) {
			float height = calculateHeight(box);
			float depth = calculateDepth(box);
			return height * depth;
		} else if (Math.abs(actorPlane.getNormal().y) > VectorMathUtils.DELTA) {
			float width = calculateWidth(box);
			float depth = calculateDepth(box);
			return width * depth;
		} else if (Math.abs(actorPlane.getNormal().z) > VectorMathUtils.DELTA) {
			float height = calculateHeight(box);
			float width = calculateWidth(box);
			return height * width;
		}
		return -1.0f;
	}

	private float calculateDepth(BoundingBox box) {
		float near = Math.max(getNear(), box.getNear());
		float far = Math.min(getFar(), box.getFar());
		return far - near;
	}

	private float calculateHeight(BoundingBox box) {
		float bottom = Math.max(getBottom(), box.getBottom());
		float top = Math.min(getTop(), box.getTop());
		return top - bottom;
	}

	private float calculateWidth(BoundingBox box) {
		float left = Math.max(getLeft(), box.getLeft());
		float right = Math.min(getRight(), box.getRight());
		return right - left;
	}

	public String serialize() {
		String result = left + " " + right + " " + top + " " + bottom + " " + near + " " + far;
		return result;
	}

	public static BoundingBox deserialize(String string) {
		String[] splittedString = string.split(" ");
		float left = Float.parseFloat(splittedString[0]);
		float right = Float.parseFloat(splittedString[1]);
		float top = Float.parseFloat(splittedString[2]);
		float bottom = Float.parseFloat(splittedString[3]);
		float near = Float.parseFloat(splittedString[4]);
		float far = Float.parseFloat(splittedString[5]);

		Vector3f position = new Vector3f(left, top, near);
		Vector3f bottomRightPosition = new Vector3f(right, bottom, far);

		return BoundingBox.fromTwoPoints(position, bottomRightPosition);
	}

	public void addOffset(Vector3f offset) {
		left += offset.x;
		right += offset.x;
		top += offset.y;
		bottom += offset.y;
		near += offset.z;
		far += offset.z;

		for (int i = 0; i < vertices.length; ++i) {
			Vector3f.add(vertices[i], offset, vertices[i]);
		}

	}
}
