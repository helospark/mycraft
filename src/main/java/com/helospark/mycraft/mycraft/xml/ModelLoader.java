package com.helospark.mycraft.mycraft.xml;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.w3c.dom.Element;

import com.helospark.mycraft.mycraft.attributes.Model;
import com.helospark.mycraft.mycraft.attributes.SimpleModelCreator;
import com.helospark.mycraft.mycraft.render.RenderableModelNode;

@Service
public class ModelLoader implements XmlLoader {

	private Map<String, Model[]> loadedModels = new HashMap<>();
	private Map<String, RenderableModelNode[]> renderableModelNodes = new HashMap<>();

	@Override
	public void parseXml(Element rootElement) {
		String modelToCreate = rootElement.getAttribute("name");
		if (modelToCreate.equals("cube")) {
			Model[] sides = new Model[6];
			sides[0] = SimpleModelCreator.getLeftCubeSide();
			sides[1] = SimpleModelCreator.getRightCubeSide();
			sides[2] = SimpleModelCreator.getTopCubeSide();
			sides[3] = SimpleModelCreator.getBottomCubeSide();
			sides[4] = SimpleModelCreator.getFarCubeSide();
			sides[5] = SimpleModelCreator.getNearCubeSide();
			loadedModels.put(modelToCreate, sides);
		} else if (modelToCreate.equals("XYPlane")) {
			Model[] sides = new Model[2];
			sides[0] = SimpleModelCreator.getXYPlaneInverted();
			sides[1] = SimpleModelCreator.getXYPlaneNormal();
			loadedModels.put(modelToCreate, sides);
		}
	}

	public Model[] getModelFromName(String modelName) {
		return loadedModels.get(modelName);
	}

	public void addModel(String key, Model model) {
		this.loadedModels.put(key, new Model[] { model });
	}

	public void registerModel(String id, RenderableModelNode[] nodes) {
		renderableModelNodes.put(id, nodes);
	}

	public RenderableModelNode[] getRenderableModelNodes(String id) {
		return renderableModelNodes.get(id);
	}

	public void addLoadedModel(String key, RenderableModelNode[] models) {
		renderableModelNodes.put(key, models);
	}

}
