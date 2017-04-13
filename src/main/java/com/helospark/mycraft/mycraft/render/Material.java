package com.helospark.mycraft.mycraft.render;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.helospark.mycraft.mycraft.shader.Shader;

public class Material {
	public Shader shader;
	public boolean isTransparent = false;
	public Map<String, MyTexture> textures = new HashMap<String, MyTexture>();

	public Material(Shader shader) {
		super();
		this.shader = shader;
	}

	public void bind() {
		shader.useProgram();
		bindTextures();
	}

	public void bindTextures() {
		int currentActiveTexture = GL13.GL_TEXTURE0;
		int programId = shader.getProgramId();
		int textureIndex = 0;
		for (Map.Entry<String, MyTexture> texture : textures.entrySet()) {
			GL13.glActiveTexture(currentActiveTexture);
			GL11.glBindTexture(texture.getValue().getType(), texture.getValue()
					.getTextureId());
			int location = shader.getUniformLocation(texture.getKey());
			GL20.glUniform1i(location, textureIndex);
			++textureIndex;
			++currentActiveTexture;
		}
	}

	public void unBind() {
		shader.unBind();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GL30.glBindVertexArray(0);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isTransparent ? 1231 : 1237);
		result = prime * result + ((shader == null) ? 0 : shader.hashCode());
		result = prime * result
				+ ((textures == null) ? 0 : textures.hashCode());
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
		Material other = (Material) obj;
		if (isTransparent != other.isTransparent)
			return false;
		if (shader == null) {
			if (other.shader != null)
				return false;
		} else if (!shader.equals(other.shader))
			return false;
		if (textures == null) {
			if (other.textures != null)
				return false;
		} else if (!textures.equals(other.textures))
			return false;
		return true;
	}

	public void addTexture(String name, MyTexture texture) {
		textures.put(name, texture);

	}

	public MyTexture getTexture() {
		return textures.entrySet().iterator().next().getValue();
	}

	public Map<String, MyTexture> getTextures() {
		return textures;
	}

	public Shader getShader() {
		return shader;
	}

	public void setShader(Shader shader) {
		this.shader = shader;
	}

	public boolean isTransparent() {
		return isTransparent;
	}

	public void setTransparent(boolean isTransparent) {
		this.isTransparent = isTransparent;
	}

}
