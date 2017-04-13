package com.helospark.mycraft.mycraft.render;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.springframework.context.ApplicationContext;

import com.helospark.mycraft.mycraft.attributes.Model;
import com.helospark.mycraft.mycraft.shader.Shader;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.transformation.Transformation;

public class BatchModel extends Model {
	private int maxVertexBufferSize;
	private int bufferOffset = 0;
	private int indexBufferOffset = 0;
	private int maxIndexBufferSize;
	private int modelPerBatch;
	private Transformation transformation;

	// cache
	float[] bufferArray;
	int[] indexBufferArray;

	public BatchModel(int modelPerBatch) {
		this.modelPerBatch = modelPerBatch;
		ApplicationContext context = Singleton.getInstance().getContext();
		transformation = context.getBean(Transformation.class);
	}

	protected void createIbo(int fullSize) {
		int size = 8 * modelPerBatch;
		maxIndexBufferSize = size * 50;
		indexBuffer = BufferUtils.createIntBuffer(maxIndexBufferSize);
	}

	protected void createVao(int fullSize) {
		int size = getVertexSize() * 8 * modelPerBatch;
		maxVertexBufferSize = size * 50;
		buffer = BufferUtils.createFloatBuffer(maxVertexBufferSize);
	}

	public void updateBatch(Shader shader, List<Model> models) {
		int index = 0;
		bufferOffset = 0;
		indexBufferOffset = 0;
		if (bufferArray == null) {
			bufferArray = new float[models.get(0).getVertexSize() * models.get(0).getVertexCount()
					* modelPerBatch];
		}
		if (indexBufferArray == null) {
			indexBufferArray = new int[models.get(0).getIndexCount() * modelPerBatch];
		}
		int bufferIndex = 0;
		int indexBufferIndex = 0;
		enableVertexAttribArrays();
		while (index < models.size()) {
			int vertexCount = 0;
			for (int i = 0; i < modelPerBatch && index < models.size(); ++i, ++index) {
				bufferIndex += models.get(index).addVertexDataToArray(bufferArray, bufferIndex);
				indexBufferIndex += models.get(index).addIndexDataToArray(indexBufferArray,
						indexBufferIndex, vertexCount);
				vertexCount += models.get(index).getVertexCount();
			}
			streamBuffer(shader, bufferArray, bufferIndex, indexBufferArray, indexBufferIndex);
			bufferIndex = 0;
			indexBufferIndex = 0;
		}
		disableVertexAttribArrays();
	}

	public void disableVertexAttribArrays() {
		for (Integer vertexAttribIndex : vertexAttribArrays) {
			GL20.glDisableVertexAttribArray(vertexAttribIndex);
		}
	}

	public void enableVertexAttribArrays() {
		for (Integer vertexAttribIndex : vertexAttribArrays) {
			GL20.glEnableVertexAttribArray(vertexAttribIndex);
		}
	}

	public void streamBuffer(Shader shader, float[] data, int size, int[] indices, int indexSize) {
		// int batchSize = size * Float.BYTES;
		// int indicesSize = indexSize * Integer.BYTES;
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, bufferId);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboIndexBuffer);

		buffer.clear();
		indexBuffer.clear();

		writeFloatsToBuffer(buffer, data, size);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STREAM_DRAW);
		writeIntsToBuffer(indexBuffer, indices, indexSize);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL15.GL_STREAM_DRAW);

		transformation.setMatrixMode(Transformation.MODEL_MATRIX);
		transformation.loadIdentity();
		transformation.uploadToOpenglShaders(shader);

		GL11.glDrawElements(GL11.GL_TRIANGLES, indexSize, GL11.GL_UNSIGNED_INT, 0);
		//
		// if (bufferOffset + batchSize >= maxVertexBufferSize * Float.BYTES) {
		// // FloatBuffer buffer = BufferUtils
		// // .createFloatBuffer(maxVertexBufferSize);
		// // writeFloatsToBuffer(buffer, data, size);
		// GL15.glBufferData(GL15.GL_ARRAY_BUFFER, maxVertexBufferSize
		// * Float.BYTES, GL15.GL_STREAM_DRAW);
		// // GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer,
		// // GL15.GL_STREAM_DRAW);
		// bufferOffset = 0;
		// }
		//
		// if (indexBufferOffset + indicesSize >= maxIndexBufferSize
		// * Integer.BYTES) {
		// // IntBuffer indexBuffer = BufferUtils
		// // .createIntBuffer(maxIndexBufferSize);
		// // writeIntsToBuffer(indexBuffer, indices, indexSize);
		// // GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer,
		// // GL15.GL_STREAM_DRAW);
		// GL15.glBufferData(GL15.GL_ARRAY_BUFFER, maxIndexBufferSize
		// * Float.BYTES, GL15.GL_STREAM_DRAW);
		//
		// indexBufferOffset = 0;
		// }
		//
		// int access = (GL30.GL_MAP_WRITE_BIT | GL30.GL_MAP_UNSYNCHRONIZED_BIT
		// | GL30.GL_MAP_INVALIDATE_RANGE_BIT);
		// ByteBuffer mappedData = null;
		// int nBufferSize = 0;
		//
		// nBufferSize = GL15.glGetBufferParameter(GL15.GL_ARRAY_BUFFER,
		// GL15.GL_BUFFER_SIZE);
		//
		// System.out.println(nBufferSize);
		//
		// mappedData = GL30.glMapBufferRange(GL15.GL_ARRAY_BUFFER,
		// bufferOffset,
		// bufferOffset + batchSize, access, null);
		// int errorCode = GL11.glGetError();
		// System.out.println(GLU.gluErrorString(errorCode));
		//
		// ByteBuffer mappedIndices = null;
		// mappedIndices = GL30.glMapBufferRange(GL15.GL_ELEMENT_ARRAY_BUFFER,
		// indexBufferOffset, indexBufferOffset + indicesSize, access,
		// null);
		//
		// if (mappedData != null && mappedIndices != null) {
		// writeFloatsToBuffer(mappedData.asFloatBuffer(), data, size);
		// writeIntsToBuffer(mappedIndices.asIntBuffer(), indices, indexSize);
		// GL15.glUnmapBuffer(GL15.GL_ARRAY_BUFFER);
		// GL15.glUnmapBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER);
		//
		// GL11.glDrawElements(GL11.GL_TRIANGLES, indices.length,
		// GL11.GL_UNSIGNED_INT, indexBufferOffset);
		//
		// bufferOffset += batchSize;
		// indexBufferOffset += indicesSize;
		// }
	}

	private void writeIntsToBuffer(IntBuffer mappedData, int[] indices, int size) {
		for (int i = 0; i < size; ++i) {
			mappedData.put(indices[i]);
		}
		mappedData.flip();
	}

	private void writeFloatsToBuffer(FloatBuffer mappedData, float[] data, int size) {
		for (int i = 0; i < size; ++i) {
			mappedData.put(data[i]);
		}
		mappedData.flip();
	}
}
