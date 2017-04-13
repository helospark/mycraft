package com.helospark.mycraft.mycraft.render;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Vector3f;
import org.springframework.context.ApplicationContext;

import com.helospark.mycraft.mycraft.attributes.Model;
import com.helospark.mycraft.mycraft.game.Block;
import com.helospark.mycraft.mycraft.mathutils.BoundingBox;
import com.helospark.mycraft.mycraft.shader.Shader;
import com.helospark.mycraft.mycraft.shader.ShaderUniform;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.transformation.Transformation;

public class RenderableNode {
	protected Material material;
	protected boolean isVisible = true;
	protected boolean pushedMatrix = false;
	protected Transformation transformation;
	protected Vector3f position = new Vector3f(0.0f, 0.0f, 0.0f);
	protected Vector3f scale = new Vector3f(1.0f, 1.0f, 1.0f);
	protected Vector3f rotation = new Vector3f(0.0f, 0.0f, 0.0f);
	protected List<Integer> vertexAttribArrays;
	protected int size;
	protected int programId;
	private Map<String, ShaderUniform> shaderUniforms = new HashMap<>();
	protected Shader shader;

	public RenderableNode(Shader shader, Model initFrom) {
		ApplicationContext context = Singleton.getInstance().getContext();
		transformation = context.getBean(Transformation.class);
		programId = shader.getProgramId();
		this.shader = shader;
		material = new Material(shader);

		vertexAttribArrays = initFrom.getVertexAttribArrays();
		size = initFrom.getIndexCount();
	}

	public RenderableNode(RenderableNode renderableNode) {
		this.transformation = renderableNode.transformation;
		this.position = new Vector3f(renderableNode.position);
		this.vertexAttribArrays = renderableNode.vertexAttribArrays;
		this.size = renderableNode.size;
		this.material = renderableNode.material;
		this.programId = renderableNode.programId;
	}

	public void setPosition(Vector3f position) {
		this.position = position;
	}

	public void addTexture(String name, MyTexture texture) {
		material.textures.put(name, texture);
	}

	public void preBatchRender() {
		material.bind();
		bindVertexAttribArrays();
	}

	public void preRender() {
		uploadUniformsForActiveProgram();
	}

	public void render() {
		GL11.glDrawElements(GL11.GL_TRIANGLES, size, GL11.GL_UNSIGNED_INT, 0);
	}

	private void bindVertexAttribArrays() {
		for (Integer vertexAttribIndex : vertexAttribArrays) {
			GL20.glEnableVertexAttribArray(vertexAttribIndex);
		}
	}

	public void postRender() {
		if (pushedMatrix) {
			transformation.setMatrixMode(Transformation.MODEL_MATRIX);
			transformation.popMatrix();
		}
		// transformation.loadIdentity();
	}

	public void postBatchRender() {
		for (Integer vertexAttribIndex : vertexAttribArrays) {
			GL20.glDisableVertexAttribArray(vertexAttribIndex);
		}
		// TODO: rest of the targets
		material.unBind();
	}

	public Material getMaterial() {
		return material;
	}

	public BoundingBox calculateBoundingBox() {
		Vector3f bottomRight = new Vector3f(position);
		bottomRight.x += Block.SIZE * scale.x;
		bottomRight.y += Block.SIZE * scale.y;
		bottomRight.z += Block.SIZE * scale.z;
		return BoundingBox.fromTwoPoints(position, bottomRight);
	}

	public void setMaterial(Material material) {
		this.material = material;
	}

	public Vector3f getPosition() {
		return position;
	}

	public void setUniform(String name, ShaderUniform data) {
		ShaderUniform foundData = shaderUniforms.get(name);
		if (foundData != null) {
			if (foundData.getClass().equals(data.getClass())) {
				foundData.setValue(data.getValue());
			} else {
				shaderUniforms.put(name, data);
			}
		}
		shaderUniforms.put(name, data);
	}

	public void uploadUniformsForActiveProgram() {
		for (Map.Entry<String, ShaderUniform> entry : shaderUniforms.entrySet()) {
			entry.getValue().uploadToShader(shader, entry.getKey());
		}
	}
}
