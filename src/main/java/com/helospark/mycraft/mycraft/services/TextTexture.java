package com.helospark.mycraft.mycraft.services;

import java.util.Map;

import com.helospark.mycraft.mycraft.mathutils.IntVector;
import com.helospark.mycraft.mycraft.render.MyTexture;

public class TextTexture {
	private MyTexture texture;
	Map<Character, IntVector> characters;
	boolean isSequential = false;
	int start, end;
	float dx;
	float dy;
	int charWidth, charHeight;
	int charPerRow;

	public TextTexture(MyTexture openglTexture, int charWidth, int charHeight) {
		this.texture = openglTexture;
		this.charWidth = charWidth;
		this.charHeight = charHeight;
		dx = (float) charWidth / openglTexture.getWidth();
		dy = (float) charHeight / openglTexture.getHeight();
		charPerRow = openglTexture.getWidth() / charWidth;
	}

	public int getTextureId() {
		return texture.getTextureId();
	}

	public void setCharacterBounds(int start, int end) {
		isSequential = true;
		this.start = start;
		this.end = end;
	}

	public float getU(char c) {
		if (isSequential) {
			int result = ((c - start) % charPerRow);
			return result * dx;
		}
		throw new IllegalStateException(
				"Not sequential character texture is not supported yet");
	}

	public float getV(char c) {
		if (isSequential) {
			int result = ((c - start) / charPerRow);
			return result * dx;
		}
		throw new IllegalStateException(
				"Not sequential character texture is not supported yet");
	}

	public float getDx() {
		return dx;
	}

	public float getDy() {
		return dy;
	}

	public boolean contains(int c) {
		if (isSequential) {
			return (c >= start && c <= end);
		}
		throw new IllegalStateException(
				"Not sequential character texture is not supported yet");
	}

	public int getRecignisedCharacterCount(String text) {
		int count = 0;
		for (int i = 0; i < text.length(); ++i) {
			if (contains(text.charAt(i))) {
				++count;
			}
		}
		return count;
	}
}
