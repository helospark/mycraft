package com.helospark.mycraft.mycraft.sceneGraph;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import com.helospark.mycraft.mycraft.attributes.Model;
import com.helospark.mycraft.mycraft.shader.Shader;

public class ModelSceneNode extends SceneNode {
	private Model model;
	private int vao;
	private Shader shader;

	public ModelSceneNode(Model model) {
		super();
		this.model = model;
		int programId = model.getShader().getProgramId();
		vao = model.getVaoForProgram(model.getShader());
		this.shader = model.getShader();
	}

	@Override
	public void preBatchRender() {
		super.preBatchRender();
	}

	@Override
	public void render() {
		super.render();
		GL30.glBindVertexArray(vao);
		model.bindVertexAttribArrays();
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, model.getVertexCount());
		GL30.glBindVertexArray(0);
	}

	@Override
	public int getProgramId() {
		return model.getShader().getProgramId();
	}

	@Override
	public Shader getShader() {
		return shader;
	}

}
