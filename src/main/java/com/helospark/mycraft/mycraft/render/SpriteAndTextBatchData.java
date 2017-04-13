package com.helospark.mycraft.mycraft.render;

import java.util.ArrayList;
import java.util.List;

import com.helospark.mycraft.mycraft.shader.Shader;

public class SpriteAndTextBatchData {
	public static final int NUMBER_OF_BATCHES = 2;
	public static final int BATCH_TYPE_SPRITE = 0;
	public static final int BATCH_TYPE_TEXT = 1;

	int[] texture = new int[NUMBER_OF_BATCHES];
	int[] programIds = new int[NUMBER_OF_BATCHES];
	Shader[] shaders = new Shader[NUMBER_OF_BATCHES];
	List<Float>[] vertices = new List[NUMBER_OF_BATCHES];
	List<Integer>[] indices = new List[NUMBER_OF_BATCHES];

	public SpriteAndTextBatchData() {
		for (int i = 0; i < NUMBER_OF_BATCHES; ++i) {
			vertices[i] = new ArrayList<Float>();
			indices[i] = new ArrayList<Integer>();
		}
	}

	public void clear() {
		for (int i = 0; i < NUMBER_OF_BATCHES; ++i) {
			vertices[i].clear();
			indices[i].clear();
		}
	}

	public List<Float> getVertexListFor(int batchType) {
		return vertices[batchType];
	}

	public List<Integer> getIndexListFor(int batchType) {
		return indices[batchType];
	}

	public void setShader(int batchType, Shader shader) {
		programIds[batchType] = shader.getProgramId();
		shaders[batchType] = shader;
	}

	public void setTextureId(int batchType, int textureId) {
		texture[batchType] = textureId;
	}

	public int getProgramIdFor(int batchType) {
		return programIds[batchType];
	}

	public int getTextureId(int i) {
		return texture[i];
	}

	public Shader getShaderForId(int i) {
		return shaders[i];
	}
}
