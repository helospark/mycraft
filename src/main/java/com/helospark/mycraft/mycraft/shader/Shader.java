package com.helospark.mycraft.mycraft.shader;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.glu.GLU;

public class Shader {

	class ShaderEntity {
		Integer id;
		String source;

		public ShaderEntity(Integer id, String source) {
			this.id = id;
			this.source = source;
		}
	}

	private Map<Integer, ShaderEntity> shaders = new HashMap<>();
	private boolean complete = false;
	private boolean linked = false;
	private int programId;
	private Map<String, Integer> uniformLocations = new HashMap<>();
	private Map<String, Integer> attributeLocations = new HashMap<>();
	private Map<Integer, ShaderUniform> globalShaderUniforms = new HashMap<>();

	public void addShader(Integer type, Integer id, String source) {
		if (shaders.containsKey(type)) {
			throw new IllegalArgumentException("Shader is already set");
		}
		shaders.put(type, new ShaderEntity(id, source));

		if (shaders.containsKey(GL20.GL_VERTEX_SHADER)
				&& shaders.containsKey(GL20.GL_FRAGMENT_SHADER)) {
			complete = true;
		}
	}

	private void fillUpUniforms(Integer id) {
		int total = GL20.glGetProgram(id, GL20.GL_ACTIVE_UNIFORMS);
		for (int i = 0; i < total; ++i) {
			String name = GL20.glGetActiveUniform(id, i, 1000);
			int location = GL20.glGetUniformLocation(id, name);

			uniformLocations.put(name, location);
		}
	}

	private void fillUpAttributes(Integer id) {
		int total = GL20.glGetProgram(id, GL20.GL_ACTIVE_ATTRIBUTES);
		for (int i = 0; i < total; ++i) {
			String name = GL20.glGetActiveAttrib(id, i, 1000);
			int location = GL20.glGetAttribLocation(id, name);

			attributeLocations.put(name, location);
		}
	}

	public void linkShader() {
		if (complete) {
			programId = GL20.glCreateProgram();
			for (Map.Entry<Integer, ShaderEntity> entry : shaders.entrySet()) {
				GL20.glAttachShader(programId, entry.getValue().id);
			}
			GL20.glLinkProgram(programId);
			GL20.glUseProgram(programId);

			fillUpUniforms(programId);
			fillUpAttributes(programId);

			checkForErrors();
		} else {
			throw new RuntimeException("The shader doesn't contains vertex and fragment shaders.");
		}
	}

	public void checkForErrors() {
		int isLinked = GL20.glGetProgram(programId, GL20.GL_LINK_STATUS);
		if (isLinked == GL11.GL_FALSE) {
			String errorMessage = GL20.glGetProgramInfoLog(programId, 1000);
			GL20.glDeleteProgram(programId);
			throw new RuntimeException("Error while compiling shader " + errorMessage);
		}

		int glErrorCode = GL11.glGetError();
		if (glErrorCode != GL11.GL_NO_ERROR) {
			throw new RuntimeException("OpenGL error: " + GLU.gluErrorString(glErrorCode));
		}
	}

	public void deactivate() {
		GL20.glUseProgram(0);
	}

	public int getProgramId() {
		return programId;
	}

	public void useProgram() {
		GL20.glUseProgram(programId);
		uploadGlobalUniforms();
	}

	public void unBind() {
		GL20.glUseProgram(0);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + programId;
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
		Shader other = (Shader) obj;
		if (programId != other.programId)
			return false;
		return true;
	}

	public int getUniformLocation(String name) {
		Integer location = uniformLocations.get(name);
		if (location == null) {
			return -1;
		}
		return (int) location;
	}

	public int getAttribLocation(String name) {
		Integer location = attributeLocations.get(name);
		if (location == null) {
			return -1;
		}
		return (int) location;
	}

	public void addGlobalShaderUniform(String name, ShaderUniform uniform) {
		int location = getUniformLocation(name);
		if (location != -1) {
			globalShaderUniforms.put(location, uniform);
		}
	}

	private void uploadGlobalUniforms() {
		for (Map.Entry<Integer, ShaderUniform> entry : globalShaderUniforms.entrySet()) {
			entry.getValue().uploadToShader(this, entry.getKey());
		}
	}

}
