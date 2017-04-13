package com.helospark.mycraft.mycraft.render;

public class MyTexture {
	public int type;
	public int textureId;

	public int width, height;

	public MyTexture(int type, int textureId, int width, int height) {
		this.type = type;
		this.textureId = textureId;
		this.width = width;
		this.height = height;
	}

	public int getTextureId() {
		return textureId;
	}

	public int getType() {
		return type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + textureId;
		result = prime * result + type;
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
		MyTexture other = (MyTexture) obj;
		if (textureId != other.textureId)
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
}
