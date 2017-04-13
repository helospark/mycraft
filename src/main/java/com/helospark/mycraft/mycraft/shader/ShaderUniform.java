package com.helospark.mycraft.mycraft.shader;

public abstract class ShaderUniform {
	protected Object value;

	public ShaderUniform(Object value) {
		this.value = value;
	}

	public abstract void uploadToShader(Shader shader, String key);

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public int getLocation(Shader shader, String key) {
		return shader.getUniformLocation(key);
	}

	public abstract void uploadToShader(Shader shader, Integer key);

}
