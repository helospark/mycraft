package com.helospark.mycraft.mycraft.shader;

import org.lwjgl.opengl.GL20;

public class IntegerShaderUniform extends ShaderUniform {
	public IntegerShaderUniform(Integer value) {
		super(value);
	}

	@Override
	public void uploadToShader(Shader shader, String key) {
		int location = super.getLocation(shader, key);
		if (location != -1) {
			GL20.glUniform1i(location, (Integer) value);
		}
	}

	@Override
	public void uploadToShader(Shader shader, Integer location) {
		GL20.glUniform1i(location, (Integer) value);
	}
}
