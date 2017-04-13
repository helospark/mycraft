package com.helospark.mycraft.mycraft.shader;

import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Vector3f;

public class Vector3fShaderUniform extends ShaderUniform {
	public Vector3fShaderUniform(Vector3f value) {
		super(value);
	}

	@Override
	public void uploadToShader(Shader shader, String key) {
		int location = super.getLocation(shader, key);
		if (location != -1) {
			Vector3f vec = (Vector3f) value;
			GL20.glUniform3f(location, vec.x, vec.y, vec.z);
		}
	}

	@Override
	public void uploadToShader(Shader shader, Integer location) {
		Vector3f vec = (Vector3f) value;
		GL20.glUniform3f(location, vec.x, vec.y, vec.z);
	}
}
