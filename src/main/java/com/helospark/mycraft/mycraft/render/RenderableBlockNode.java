package com.helospark.mycraft.mycraft.render;

import org.lwjgl.util.vector.Vector3f;

import com.helospark.mycraft.mycraft.attributes.Model;
import com.helospark.mycraft.mycraft.shader.Shader;
import com.helospark.mycraft.mycraft.transformation.Transformation;

public class RenderableBlockNode extends RenderableNode {
	private Model model;

	public RenderableBlockNode(Shader shader, Model initFrom) {
		super(shader, initFrom);
		initFrom.getVaoForProgram(shader);
		this.model = new Model(initFrom);
	}

	public RenderableBlockNode(RenderableBlockNode node) {
		super(node);
		this.model = new Model(node.model);
	}

	@Override
	public void setPosition(Vector3f position) {
		super.setPosition(position);
		model.applyTranslation(position);
	}

	@Override
	public void preRender() {
		if (isVisible) {
			transformation.setMatrixMode(Transformation.MODEL_MATRIX);
			transformation.loadIdentity();
			transformation.pushMatrix();
			pushedMatrix = true;
			transformation.translate(position);
			transformation.rotateX(rotation.x);
			transformation.rotateY(rotation.y);
			transformation.rotateZ(rotation.z);
			transformation.scale(scale);
			transformation.uploadToOpenglShaders(shader);
		}
		uploadUniformsForActiveProgram();
	}

	public Model getModel() {
		return model;
	}

	public void setTexturePosition(int side, float u, float v) {
		model.applyUv(side, u, v);
	}

	public void setLight(float light) {
		model.applyLight(light);
	}

	public void addLight(int index, float amount) {
		model.addLight(index, amount);
	}

	public Vector3f[] getVertices() {
		return model.getPositionArray();
	}

	public float getLight(int index) {
		return model.getLight(index);
	}
}
