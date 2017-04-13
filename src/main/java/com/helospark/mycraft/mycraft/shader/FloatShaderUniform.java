package com.helospark.mycraft.mycraft.shader;

import org.lwjgl.opengl.GL20;

public class FloatShaderUniform extends ShaderUniform {

	public FloatShaderUniform(Float value) {
		super(value);
	}

	public FloatShaderUniform(Double value) {
		super(null);
		Float f = new Float((float) value.doubleValue());
		this.value = f;
	}

	@Override
	public void uploadToShader(Shader shader, String key) {
		int location = super.getLocation(shader, key);
		if (location != -1) {
			GL20.glUniform1f(location, (float) value);
		}
	}

	@Override
	public void uploadToShader(Shader shader, Integer location) {
		GL20.glUniform1f(location, (Float) value);
	}
}
