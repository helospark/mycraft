package com.helospark.mycraft.mycraft.sceneGraph;

import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import com.helospark.mycraft.mycraft.game.Camera;
import com.helospark.mycraft.mycraft.mathutils.BoundingBox;
import com.helospark.mycraft.mycraft.mathutils.CullingResult;
import com.helospark.mycraft.mycraft.shader.Shader;

public class OctreeSceneNode extends SceneNode {
	public static final int SCENE_NODE_ELEMENT_COUNT = 8;
	private OctreeSceneNode[] octreeChildList = new OctreeSceneNode[SCENE_NODE_ELEMENT_COUNT];
	private BoundingBox box;
	private float size;

	public OctreeSceneNode(float size) {
		box = new BoundingBox(position, size, size, size);
		this.size = size;
	}

	@Override
	public void render() {
		// Octrees don't need render
	}

	@Override
	public void getVisibleChildrenList(List<SceneNode> visibleObjects,
			Camera activeCamera) {
		CullingResult result = activeCamera.containsBox(box);
		if (result == CullingResult.FULLY_IN) {
			visibleObjects.addAll(children);
			for (OctreeSceneNode child : octreeChildList) {
				if (child != null) {
					child.addAllSceneNodes(visibleObjects);
				}
			}
		} else if (result == CullingResult.PARTIALLY_IN) {
			for (OctreeSceneNode child : octreeChildList) {
				if (child != null) {
					child.getVisibleChildrenList(visibleObjects, activeCamera);
				}
			}
			for (SceneNode child : children) {
				child.getVisibleChildrenList(visibleObjects, activeCamera);
			}
		}
	}

	private void addAllSceneNodes(List<SceneNode> visibleObjects) {
		visibleObjects.addAll(children);
		for (OctreeSceneNode child : octreeChildList) {
			if (child != null) {
				child.addAllSceneNodes(visibleObjects);
			}
		}
	}

	public void addChild(SceneNode newChild) {

		for (OctreeSceneNode child : octreeChildList) {
			if (child == null) {
				createOctreeChildren();
			}
			CullingResult result = newChild.getBoundingBox().containsBox(
					child.getBoundingBox());
			if (result == CullingResult.FULLY_IN) {
				child.addChild(newChild);
				break;
			} else if (result == CullingResult.PARTIALLY_IN) {
				children.add(newChild);
				break;
			}
		}

	}

	private void createOctreeChildren() {
		float sizePerTwo = size / 2.0f;
		for (int i = 0; i < 8; ++i) {
			octreeChildList[i] = new OctreeSceneNode(size / 2);
			Vector3f newPosition = new Vector3f(position);
			Vector3f.add(newPosition, new Vector3f(), newPosition);
			octreeChildList[i].setPosition(newPosition);
		}

	}

	@Override
	public int getProgramId() {
		return 0;
	}

	@Override
	public Shader getShader() {
		return null;
	}
}
