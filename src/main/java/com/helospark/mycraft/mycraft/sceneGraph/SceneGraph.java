package com.helospark.mycraft.mycraft.sceneGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.helospark.mycraft.mycraft.game.Camera;
import com.helospark.mycraft.mycraft.render.Material;
import com.helospark.mycraft.mycraft.shader.Shader;

public class SceneGraph {

	private List<SceneNode> children = new ArrayList<SceneNode>();

	// cache
	private List<SceneNode> visibleObjects = new ArrayList<SceneNode>();
	private Map<Material, List<SceneNode>> sortedMaterialList = new HashMap<Material, List<SceneNode>>();

	public SceneGraph() {

	}

	public void render(Camera camera) {
		visibleObjects = getVisibleObjectList(camera);
		sortByDistance(visibleObjects);
		Map<Material, List<SceneNode>> sortedMap = sortByMaterial(visibleObjects);
		render(sortedMap);
	}

	private void render(Map<Material, List<SceneNode>> objects) {
		for (Map.Entry<Material, List<SceneNode>> entry : objects.entrySet()) {
			List<SceneNode> objectsWithMaterial = entry.getValue();
			objectsWithMaterial.get(0).preBatchRender();
			int programId = objectsWithMaterial.get(0).getProgramId();
			Shader shader = objectsWithMaterial.get(0).getShader();

			for (SceneNode node : objectsWithMaterial) {
				node.preRender(shader);
				node.render();
				node.postRender();
			}

			objectsWithMaterial.get(0).postBatchRender();
		}
	}

	private Map<Material, List<SceneNode>> sortByMaterial(
			List<SceneNode> visibleObjects) {
		for (SceneNode sceneNode : visibleObjects) {
			Material material = sceneNode.getMaterial();
			List<SceneNode> materialList = sortedMaterialList.get(material);
			if (materialList != null) {
				materialList.add(sceneNode);
			} else {
				materialList = new ArrayList<SceneNode>();
				materialList.add(sceneNode);
				sortedMaterialList.put(material, materialList);
			}
		}
		return sortedMaterialList;
	}

	private void sortByDistance(List<SceneNode> visibleObjects) {
		// Collections.sort(visibleObjects, sceneNodeDistanceComparator);
	}

	private List<SceneNode> getVisibleObjectList(Camera camera) {
		// TODO: save all the transformations
		visibleObjects.clear();
		for (SceneNode sceneNode : children) {
			sceneNode.getVisibleChildrenList(visibleObjects, camera);
		}
		return visibleObjects;

	}

	public void addChild(SceneNode child) {
		children.add(child);
	}
}
