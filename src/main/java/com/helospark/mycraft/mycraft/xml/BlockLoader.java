package com.helospark.mycraft.mycraft.xml;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.helospark.mycraft.mycraft.actor.InventoryItem;
import com.helospark.mycraft.mycraft.attributes.Model;
import com.helospark.mycraft.mycraft.blockrightclickhandler.BlockRightClickHandler;
import com.helospark.mycraft.mycraft.blockupdaters.BlockTimeUpdater;
import com.helospark.mycraft.mycraft.game.Block;
import com.helospark.mycraft.mycraft.game.Blocks;
import com.helospark.mycraft.mycraft.game.DroppableItem;
import com.helospark.mycraft.mycraft.helpers.XmlHelpers;
import com.helospark.mycraft.mycraft.mathutils.IntVector;
import com.helospark.mycraft.mycraft.render.Material;
import com.helospark.mycraft.mycraft.shader.Shader;
import com.helospark.mycraft.mycraft.shader.ShaderLoader;
import com.helospark.mycraft.mycraft.singleton.Singleton;

@Service
public class BlockLoader implements XmlLoader {

	ApplicationContext context;
	@Autowired
	ModelLoader modelHandler;
	@Autowired
	ShaderLoader shaderHandler;
	@Autowired
	MaterialLoader materialHandler;

	public BlockLoader() {
		context = Singleton.getInstance().getContext();
	}

	@Override
	public void parseXml(Element rootElement) {
		try {
			String blockName = rootElement.getAttribute("name");
			String modelName = XmlHelpers.getStringValue(rootElement, "model");

			int id = XmlHelpers.parseIntegerValue(rootElement, "id");
			Model[] model = modelHandler.getModelFromName(modelName);
			Material material = parseMaterial(rootElement);
			Block block = new Block(model, material.getShader(), id);

			fillUpTransparent(rootElement, block);
			fillUpTextureCoordinates(rootElement, block);
			fillUpDamageShader(rootElement, block);
			block.setMaterial(material);
			fillUpStrength(rootElement, block);
			fillUpRightClickHandler(rootElement, block);
			block.setMaterial(material);

			NodeList lightNodeList = rootElement.getElementsByTagName("light");

			if (lightNodeList.getLength() != 0) {
				Element lightElement = XmlHelpers.findFirstElement(lightNodeList);
				parseLight(lightElement, block);
			}

			if (rootElement.getElementsByTagName("can-collide").getLength() > 0) {
				boolean canCollide = XmlHelpers.parseBooleanValue(rootElement, "can-collide");
				block.setCanCollide(canCollide);
			}

			NodeList itemsWhichCanDestroyThis = rootElement
					.getElementsByTagName("can-be-destroyed-with");
			if (itemsWhichCanDestroyThis.getLength() != 0) {
				List<Integer> itemsCapableOfDestroyingBlock = XmlHelpers.parseIntegerList(
						XmlHelpers.findFirstElement(itemsWhichCanDestroyThis), "item");
				if (itemsCapableOfDestroyingBlock.size() != 0) {
					block.addItemsCapableOfDestroyingBlock(itemsCapableOfDestroyingBlock);
				}
			}

			NodeList blockUpdaterNodes = rootElement.getElementsByTagName("block-updaters");
			if (blockUpdaterNodes.getLength() > 0) {
				List<String> blockUpdaters = XmlHelpers.parseStringList(
						XmlHelpers.findFirstElement(blockUpdaterNodes), "block-updater");
				for (int i = 0; i < blockUpdaters.size(); ++i) {
					BlockTimeUpdater blockTimeUpdater = (BlockTimeUpdater) context
							.getBean(blockUpdaters.get(i));
					block.addTimeBlockUpdater(blockTimeUpdater);
				}
			}

			fillUpDroppableItems(rootElement, block);

			Blocks.addBlock(id, blockName, block);
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	private void parseLight(Element lightElement, Block block) {
		int lightStrength = XmlHelpers.parseIntegerValue(lightElement, "light-strength");
		int lightDistance = XmlHelpers.parseIntegerValue(lightElement, "light-distance");
		block.enableLighting(lightStrength, lightDistance);
	}

	private void fillUpRightClickHandler(Element rootElement, Block block) {
		String rightClickHandler = XmlHelpers.getStringValue(rootElement, "right-click-handler");
		BlockRightClickHandler blockRightClickHandler = (BlockRightClickHandler) context
				.getBean(rightClickHandler);
		block.setRightClickHandler(blockRightClickHandler);
	}

	private void fillUpStrength(Element rootElement, Block block) {
		double strength = XmlHelpers.parseDoubleValue(rootElement, "strength");
		block.setStrength((float) strength);
	}

	private Material parseMaterial(Element rootElement) {
		String materialName = XmlHelpers.getStringValue(rootElement, "material");
		Material material = materialHandler.getMaterialFromName(materialName);
		return material;
	}

	private void fillUpTransparent(Element rootElement, Block block) {
		boolean transparent = XmlHelpers.parseBooleanValue(rootElement, "transparent");
		block.setTransparent(transparent);
	}

	private void fillUpDamageShader(Element rootElement, Block block) {
		String damageShaderName = XmlHelpers.getStringValue(rootElement, "damage-shader");
		Shader damageShader = shaderHandler.getFromName(damageShaderName);
		block.setDamageShader(damageShader);
	}

	private void fillUpDroppableItems(Element rootElement, Block block) {
		Element dropElements = XmlHelpers.findFirstElement(rootElement
				.getElementsByTagName("drops"));
		NodeList dropElementList = dropElements.getChildNodes();
		for (int i = 0; i < dropElementList.getLength(); ++i) {
			if (dropElementList.item(i).getNodeType() == Node.ELEMENT_NODE) {
				DroppableItem droppableItem = parseDroppableItem((Element) dropElementList.item(i));
				block.addDroppableItem(droppableItem);
			}
		}
	}

	private void fillUpTextureCoordinates(Element rootElement, Block block) {
		Element textureCoordinates = XmlHelpers.findFirstElement(rootElement
				.getElementsByTagName("block-side-textures"));
		NodeList textureCoordinateNodeList = textureCoordinates.getElementsByTagName("side");
		if (textureCoordinates.hasAttribute("same-for-all-sides")) {
			IntVector vector = XmlHelpers.parseTexture(textureCoordinateNodeList, 0);
			for (int i = 0; i < block.getNumberOfModels(); ++i) {
				block.setTexturePosition(i, vector.x, vector.y);
			}
		} else {
			for (int i = 0; i < block.getNumberOfModels(); ++i) {
				IntVector vector = XmlHelpers.parseTexture(textureCoordinateNodeList, i);
				block.setTexturePosition(i, vector.x, vector.y);
			}
		}
	}

	private DroppableItem parseDroppableItem(Element item) {
		int id = XmlHelpers.parseIntegerValue(item, "drop-id");
		int amount = XmlHelpers.parseIntegerValue(item, "drop-amount");
		float chance = (float) XmlHelpers.parseDoubleValue(item, "drop-chance");

		return new DroppableItem(new InventoryItem(id, 1), chance, amount);
	}

}
