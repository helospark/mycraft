package com.helospark.mycraft.mycraft.render;

import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector3f;

import com.helospark.mycraft.mycraft.attributes.Model;
import com.helospark.mycraft.mycraft.shader.Shader;
import com.helospark.mycraft.mycraft.transformation.Transformation;

public class RenderableModelNode extends RenderableNode {
	int vao;

	public RenderableModelNode(Shader shader, Model initFrom) {
		super(shader, initFrom);
		vao = initFrom.getVaoForProgram(shader);
	}

	public RenderableModelNode(RenderableModelNode node) {
		super(node);
		this.vao = node.vao;
	}

	@Override
	public void preBatchRender() {
		GL30.glBindVertexArray(vao);
		super.preBatchRender();
	}

	@Override
	public void preRender() {
		int programId = material.shader.getProgramId();
		pushedMatrix = false;
		if (isVisible) {
			transformation.setMatrixMode(Transformation.MODEL_MATRIX);
			transformation.loadIdentity();
			transformation.pushMatrix();
			pushedMatrix = true;
			transformation.rotateX(rotation.x);
			transformation.rotateY(rotation.y);
			transformation.rotateZ(rotation.z);
			transformation.translate(position);
			transformation.scale(scale);
			transformation.uploadToOpenglShaders(shader);
		}
		uploadUniformsForActiveProgram();
	}

	@Override
	public void postBatchRender() {
		super.postBatchRender();
		GL30.glBindVertexArray(0);
	}

	public void setScale(float x, float y, float z) {
		scale.x = x;
		scale.y = y;
		scale.z = z;
	}

	public void setRotation(Vector3f direction) {
		this.rotation = direction;
	}

	public void setScale(Vector3f scale) {
		this.scale = scale;
	}

	public String serialize() {

		return null;
	}
}
