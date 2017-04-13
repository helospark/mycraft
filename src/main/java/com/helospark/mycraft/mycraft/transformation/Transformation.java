package com.helospark.mycraft.mycraft.transformation;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.springframework.stereotype.Service;

import com.helospark.mycraft.mycraft.mathutils.VectorMathUtils;
import com.helospark.mycraft.mycraft.shader.Shader;

@Service
public class Transformation {
	private static final String MODEL_MATRIX_SHADER_NAME = "inModelMatrix";
	private static final String VIEW_MATRIX_SHADER_NAME = "inViewMatrix";
	private static final String PROJECTION_MATRIX_SHADER_NAME = "inProjectionMatrix";
	private static final String MODEL_VIEW_MATRIX_SHADER_NAME = "inModelViewMatrix";
	private static final String MODEL_VIEW_PROJECTION_MATRIX_SHADER_NAME = "inModelViewProjectionMatrix";
	private static final int NUM_MATRICES = 3;
	public static final int MODEL_MATRIX = 0;
	public static final int VIEW_MATRIX = 1;
	public static final int PROJECTION_MATRIX = 2;

	private List<Matrix4f>[] matrixStack = new List[NUM_MATRICES];
	private int matrixMode = MODEL_MATRIX;
	private Matrix4f modelViewMatrix = new Matrix4f();
	private Matrix4f modelViewProjectionMatrix = new Matrix4f();
	private Matrix4f normalMatrix = new Matrix4f();

	// OPTIMIZE
	private FloatBuffer floatBuffer4 = BufferUtils.createFloatBuffer(16);
	private FloatBuffer floatBuffer3 = BufferUtils.createFloatBuffer(9);

	private boolean[] dirtyBits = new boolean[NUM_MATRICES];
	private int lastProgramId = -1;

	public Transformation() {
		for (int i = 0; i < NUM_MATRICES; ++i) {
			matrixStack[i] = new ArrayList<Matrix4f>();
			Matrix4f newElement = new Matrix4f();
			newElement.setIdentity();
			matrixStack[i].add(newElement);
			dirtyBits[i] = true;
		}
	}

	public void setMatrixMode(int newMatrixMode) {
		if (newMatrixMode == MODEL_MATRIX) {
			matrixMode = newMatrixMode;
		} else if (newMatrixMode == VIEW_MATRIX) {
			matrixMode = newMatrixMode;
		} else if (newMatrixMode == PROJECTION_MATRIX) {
			matrixMode = newMatrixMode;
		} else {
			throw new IllegalArgumentException("Unable to change matrix mode");
		}
	}

	public void pushMatrix() {
		dirtyBits[matrixMode] = true;
		List<Matrix4f> currentMatrixStack = matrixStack[matrixMode];
		Matrix4f topElement = currentMatrixStack.get(currentMatrixStack.size() - 1);
		currentMatrixStack.add(new Matrix4f(topElement));
	}

	public void popMatrix() {
		dirtyBits[matrixMode] = true;
		List<Matrix4f> currentMatrixStack = matrixStack[matrixMode];
		if (currentMatrixStack.size() <= 1) {
			throw new RuntimeException("Unable to pop element, stack underflow");
		}
		currentMatrixStack.remove(currentMatrixStack.size() - 1);
	}

	public void loadIdentity() {
		dirtyBits[matrixMode] = true;
		Matrix4f topElement = getTopElement();
		topElement.setIdentity();
	}

	public void translate(Vector3f amount) {
		dirtyBits[matrixMode] = true;
		Matrix4f topElement = getTopElement();
		Matrix4f translationMatrix = new Matrix4f().translate(amount);
		Matrix4f.mul(translationMatrix, topElement, topElement);
		// topElement.translate(amount);
	}

	public void rotateX(float angle) {
		dirtyBits[matrixMode] = true;
		Matrix4f topElement = getTopElement();
		Matrix4f rotate = new Matrix4f().rotate(angle, VectorMathUtils.X_UNIT_VECTOR);
		Matrix4f.mul(rotate, topElement, topElement);
		// topElement.rotate(angle, VectorMathUtils.X_UNIT_VECTOR);
	}

	public void rotateY(float angle) {
		dirtyBits[matrixMode] = true;
		Matrix4f topElement = getTopElement();
		Matrix4f rotate = new Matrix4f().rotate(angle, VectorMathUtils.Y_UNIT_VECTOR);
		Matrix4f.mul(rotate, topElement, topElement);
		// topElement.rotate(angle, VectorMathUtils.Y_UNIT_VECTOR);
	}

	public void rotateZ(float angle) {
		dirtyBits[matrixMode] = true;
		Matrix4f topElement = getTopElement();
		topElement.rotate(angle, VectorMathUtils.Z_UNIT_VECTOR);
	}

	public void rotateX(float angle, Vector3f axis) {
		dirtyBits[matrixMode] = true;
		Matrix4f topElement = getTopElement();
		topElement.rotate(angle, axis);
	}

	public void scale(float amount) {
		dirtyBits[matrixMode] = true;
		Matrix4f topElement = getTopElement();
		topElement.scale(new Vector3f(amount, amount, amount));
	}

	public void scale(Vector3f amount) {
		dirtyBits[matrixMode] = true;
		Matrix4f topElement = getTopElement();
		topElement.scale(amount);
	}

	public void perspective(float fieldOfView, float aspectRatio, float near, float far) {
		dirtyBits[matrixMode] = true;
		Matrix4f topElement = getTopElement();

		Matrix4f projectionMatrix = new Matrix4f();

		float y_scale = (float) (1.0f / Math.tan((Math.toRadians(fieldOfView / 2f))));
		float x_scale = y_scale / aspectRatio;
		float frustum_length = far - near;

		projectionMatrix.m00 = x_scale;
		projectionMatrix.m11 = y_scale;
		projectionMatrix.m22 = -((far + near) / frustum_length);
		projectionMatrix.m23 = -1;
		projectionMatrix.m32 = -((2 * near * far) / frustum_length);
		projectionMatrix.m33 = 0;

		Matrix4f.mul(topElement, projectionMatrix, topElement);
	}

	public void ortho(float left, float right, float top, float bottom, float near, float far) {
		dirtyBits[matrixMode] = true;
		Matrix4f topElement = getTopElement();

		Matrix4f orthoMatrix = new Matrix4f();
		orthoMatrix.m00 = 2.0f / (right - left);
		orthoMatrix.m01 = 0.0f;
		orthoMatrix.m02 = 0.0f;
		orthoMatrix.m03 = 0.0f;

		orthoMatrix.m10 = 0.0f;
		orthoMatrix.m11 = 2.0f / (top - bottom);
		orthoMatrix.m12 = 0.0f;
		orthoMatrix.m13 = 0.0f;

		orthoMatrix.m20 = 0.0f;
		orthoMatrix.m21 = 0.0f;
		orthoMatrix.m22 = -2.0f / (far - near);
		orthoMatrix.m23 = 0.0f;

		orthoMatrix.m30 = -(right + left) / (right - left);
		orthoMatrix.m31 = -(top + bottom) / (top - bottom);
		orthoMatrix.m32 = -(far + near) / (far - near);
		orthoMatrix.m33 = 1.0f;

		Matrix4f.mul(topElement, orthoMatrix, topElement);
	}

	public void uploadToOpenglShaders(Shader shader) {
		if (lastProgramId != shader.getProgramId()) {
			dirtyBits[PROJECTION_MATRIX] = true;
			dirtyBits[VIEW_MATRIX] = true;
			dirtyBits[MODEL_MATRIX] = true;
			lastProgramId = shader.getProgramId();
		}
		uploadModelMatrix(shader);
		uploadViewMatrix(shader);
		uploadProjectionMatrix(shader);
		uploadModelViewMatrix(shader);
		uploadModelViewProjectionMatrix(shader);
		uploadNormalMatrix(shader);
		dirtyBits[PROJECTION_MATRIX] = false;
		dirtyBits[VIEW_MATRIX] = false;
		dirtyBits[MODEL_MATRIX] = false;
	}

	private void uploadNormalMatrix(Shader shader) {
		if (dirtyBits[MODEL_MATRIX] || dirtyBits[VIEW_MATRIX]) {

		}
	}

	private void uploadModelViewProjectionMatrix(Shader shader) {
		if (dirtyBits[MODEL_MATRIX] || dirtyBits[VIEW_MATRIX] || dirtyBits[PROJECTION_MATRIX]) {
			int location = shader.getUniformLocation(MODEL_VIEW_PROJECTION_MATRIX_SHADER_NAME);
			if (location != -1) {
				Matrix4f.mul(getTopElement(PROJECTION_MATRIX), getTopElement(VIEW_MATRIX),
						modelViewMatrix);
				Matrix4f.mul(modelViewMatrix, getTopElement(MODEL_MATRIX),
						modelViewProjectionMatrix);

				uploadMatrix(modelViewProjectionMatrix, shader, location);
			}
		}
	}

	private void uploadModelViewMatrix(Shader shader) {
		if (dirtyBits[VIEW_MATRIX] || dirtyBits[MODEL_MATRIX]) {
			int location = shader.getUniformLocation(MODEL_VIEW_MATRIX_SHADER_NAME);
			if (location != -1) {
				Matrix4f.mul(getTopElement(VIEW_MATRIX), getTopElement(MODEL_MATRIX),
						modelViewMatrix);
				uploadMatrix(modelViewMatrix, shader, location);
			}
		}
	}

	private void uploadProjectionMatrix(Shader shader) {
		if (dirtyBits[PROJECTION_MATRIX]) {
			int location = shader.getUniformLocation(PROJECTION_MATRIX_SHADER_NAME);
			if (location != -1) {
				uploadMatrix(getTopElement(PROJECTION_MATRIX), shader, location);
			}
		}
	}

	private void uploadViewMatrix(Shader shader) {
		if (dirtyBits[VIEW_MATRIX]) {
			int location = shader.getUniformLocation(VIEW_MATRIX_SHADER_NAME);
			if (location != -1) {
				uploadMatrix(getTopElement(VIEW_MATRIX), shader, location);
			}
		}
	}

	private void uploadModelMatrix(Shader shader) {
		if (dirtyBits[MODEL_MATRIX]) {
			int location = shader.getUniformLocation(MODEL_MATRIX_SHADER_NAME);
			if (location != -1) {
				uploadMatrix(getTopElement(MODEL_MATRIX), shader, location);
			}
		}
	}

	private void uploadMatrix(Matrix4f matrixToUpload, Shader shader, int location) {
		matrixToUpload.store(floatBuffer4);
		floatBuffer4.flip();
		GL20.glUniformMatrix4(location, false, floatBuffer4);
	}

	private void uploadMatrix(Matrix3f matrixToUpload, Shader shader, int location) {
		matrixToUpload.store(floatBuffer3);
		floatBuffer3.flip();
		GL20.glUniformMatrix3(location, false, floatBuffer4);
	}

	private Matrix4f getTopElement(int matrixModeParam) {
		List<Matrix4f> currentMatrixStack = matrixStack[matrixModeParam];
		return currentMatrixStack.get(currentMatrixStack.size() - 1);
	}

	private Matrix4f getTopElement() {
		List<Matrix4f> currentMatrixStack = matrixStack[matrixMode];
		return currentMatrixStack.get(currentMatrixStack.size() - 1);
	}

	public int getCurrentMatrixMode() {
		return matrixMode;
	}

	public Matrix4f getCurrentMatrix() {
		return getTopElement();
	}
}
