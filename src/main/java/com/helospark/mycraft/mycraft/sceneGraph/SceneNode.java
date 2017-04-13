package com.helospark.mycraft.mycraft.sceneGraph;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;
import org.springframework.context.ApplicationContext;

import com.helospark.mycraft.mycraft.game.Camera;
import com.helospark.mycraft.mycraft.mathutils.BoundingBox;
import com.helospark.mycraft.mycraft.mathutils.CullingResult;
import com.helospark.mycraft.mycraft.render.Material;
import com.helospark.mycraft.mycraft.shader.Shader;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.transformation.Transformation;

public abstract class SceneNode {
	protected List<SceneNode> children = new ArrayList<SceneNode>();
	protected Vector3f position = new Vector3f(0.0f, 0.0f, 0.0f);
	protected Vector3f rotation = new Vector3f(0.0f, 0.0f, 0.0f);
	protected Vector3f scale = new Vector3f(1.0f, 1.0f, 1.0f);
	protected boolean isVisible = true;
	protected Transformation transformation;
	protected BoundingBox boundingBox = new BoundingBox();

	private boolean pushedMatrix = false;

	public SceneNode() {
		ApplicationContext context = Singleton.getInstance().getContext();
		transformation = context.getBean(Transformation.class);
	}

	public void addChild(SceneNode child) {
		children.add(child);
	}

	public void preBatchRender() {

	}

	public void preRender(Shader shader) {
		if (isVisible) {
			transformation.pushMatrix();
			pushedMatrix = true;
			transformation.scale(scale);
			transformation.rotateX(rotation.x);
			transformation.rotateY(rotation.y);
			transformation.rotateZ(rotation.z);
			transformation.translate(position);
			transformation.uploadToOpenglShaders(shader);
		}
	}

	public void render() {

	}

	public void postRender() {
		if (pushedMatrix) {
			transformation.popMatrix();
		}
	}

	public void postBatchRender() {

	}

	public Material getMaterial() {
		return null;
	}

	public void getVisibleChildrenList(List<SceneNode> visibleObjects,
			Camera camera) {
		CullingResult result = camera.containsBox(getBoundingBox());
		if (result != CullingResult.FULLY_OUT) {
			visibleObjects.add(this);
		}

		for (SceneNode child : children) {
			child.getVisibleChildrenList(visibleObjects, camera);
		}
	}

	public BoundingBox getBoundingBox() {
		return boundingBox;
	}

	public void setPosition(Vector3f position) {
		this.position = position;
	}

	public abstract int getProgramId();

	public abstract Shader getShader();
}
