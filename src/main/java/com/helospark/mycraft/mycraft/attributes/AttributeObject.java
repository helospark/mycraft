package com.helospark.mycraft.mycraft.attributes;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.lwjgl.opengl.GL11;

public class AttributeObject {

	List<float[]> dataList = new ArrayList<float[]>();
	int length;
	int type;

	public AttributeObject(int type, int length) {
		this.length = length;
		this.type = type;
		if (type != GL11.GL_FLOAT) {
			throw new IllegalArgumentException("Only supported type is float for attributes");
		}
	}

	public void setData(float[] data) {
		if (data.length != length) {
			throw new IllegalArgumentException("Data size doesn't match");
		}
		float[] tmpArray = Arrays.copyOf(data, data.length);
		dataList.add(tmpArray);
	}

	public FloatBuffer writeToFloatBuffer(FloatBuffer floatBuffer, int index) {
		if (index < dataList.size()) {
			for (int i = 0; i < length; ++i) {
				floatBuffer.put(dataList.get(index)[i]);
			}
		}
		return floatBuffer;
	}

	public int getLength() {
		return length;
	}

	public int getNumberOfDatas() {
		return dataList.size();
	}

	public void empty() {
		dataList.clear();
	}

	public float get(int vertexIndex, int componentIndex) {
		if (componentIndex < 0 || componentIndex >= length) {
			throw new IllegalArgumentException(
					"Unable to get the given index, because boundaries are out of bounds");
		}
		return dataList.get(vertexIndex)[componentIndex];
	}

	public void set(int index, int i, float data) {
		dataList.get(index)[i] = data;
	}

	public int writeToBuffer(float[] buffer, int bufferIndex, int elementIndex) {
		int writtenData = 0;
		for (int i = 0; i < dataList.get(elementIndex).length; ++i) {
			buffer[bufferIndex++] = dataList.get(elementIndex)[i];
			++writtenData;
		}
		return writtenData;
	}

	public AttributeObject copy() {
		AttributeObject result = new AttributeObject(type, length);
		result.dataList = new ArrayList<float[]>();
		for (float[] data : dataList) {
			float[] floatData = new float[data.length];
			for (int j = 0; j < floatData.length; ++j) {
				floatData[j] = data[j];
			}
			result.dataList.add(floatData);
		}
		return result;
	}

	public int getNumberOfVertices() {
		return dataList.size();
	}

	public void add(int index, int vertex, float amount) {
		dataList.get(index)[vertex] += amount;
	}

	public void clear() {
		dataList.clear();
	}

}
