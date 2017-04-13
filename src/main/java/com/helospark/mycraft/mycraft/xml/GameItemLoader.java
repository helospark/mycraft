package com.helospark.mycraft.mycraft.xml;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.helospark.mycraft.mycraft.actor.GameItem;
import com.helospark.mycraft.mycraft.actor.GameItems;
import com.helospark.mycraft.mycraft.attributes.Model;
import com.helospark.mycraft.mycraft.helpers.XmlHelpers;
import com.helospark.mycraft.mycraft.itemrightclickhandlers.GameItemRightClickHandler;
import com.helospark.mycraft.mycraft.mathutils.IntVector;
import com.helospark.mycraft.mycraft.render.Material;
import com.helospark.mycraft.mycraft.render.MyTexture;
import com.helospark.mycraft.mycraft.render.RenderableModelNode;
import com.helospark.mycraft.mycraft.shader.FloatShaderUniform;
import com.helospark.mycraft.mycraft.shader.Shader;
import com.helospark.mycraft.mycraft.singleton.Singleton;

@Service
public class GameItemLoader implements XmlLoader {

    private static final String BLOCK_TYPE_NAME = "block";

    @Autowired
    ModelLoader modelLoader;

    @Autowired
    MaterialLoader materialLoader;

    @Autowired
    GameItems items;

    ApplicationContext context;

    public GameItemLoader() {
        context = Singleton.getInstance().getContext();
    }

    @Override
    public void parseXml(Element rootElement) {
        String name = rootElement.getAttribute("name");
        String type = rootElement.getAttribute("type");

        int id = XmlHelpers.parseIntegerValue(rootElement, "id");
        GameItemRightClickHandler blockRightClickHandler = loadRightClickHandler(rootElement);
        IntVector spriteTexturePosition = loadSpritePosition(rootElement);

        String nodeId = loadDroppableItemModels(id, rootElement, type);
        Material spriteMaterial = loadSpriteMaterial(rootElement);
        MyTexture spriteTexture = spriteMaterial.getTexture();

        int blockId = -1;
        if (type.equals(BLOCK_TYPE_NAME)) {
            blockId = XmlHelpers.parseIntegerValue(rootElement, "blockId");
        }
        GameItem gameItem = new GameItem(id, blockId, nodeId,
                blockRightClickHandler);
        gameItem.setSpriteTexture(spriteTexture);
        gameItem.setUv(spriteTexturePosition.x, spriteTexturePosition.y);
        gameItem.setName(name);

        fillUpBlockMultiplierIds(rootElement, gameItem);

        items.addGameItem(gameItem);
    }

    private void fillUpBlockMultiplierIds(Element rootElement, GameItem gameItem) {
        NodeList destroySpeedMultiplierList = rootElement
                .getElementsByTagName("block-destroy-speed");

        if (destroySpeedMultiplierList.getLength() != 0) {
            NodeList childNodes = XmlHelpers.findFirstElement(
                    destroySpeedMultiplierList).getChildNodes();
            Map<Integer, Float> blockToMultiplierMap = new HashMap<>();
            for (int i = 0; i < childNodes.getLength(); ++i) {
                if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) childNodes.item(i);
                    int id = Integer.parseInt(element.getAttribute("id"));
                    float multiplier = Float.parseFloat(element
                            .getAttribute("multiplier"));
                    blockToMultiplierMap.put(id, multiplier);
                }
            }
            gameItem.addBlockMultipliers(blockToMultiplierMap);
        }
    }

    private String loadDroppableItemModels(int id, Element rootElement,
            String type) {
        Shader shader = loadShader(rootElement);
        Material regularMaterial = materialLoader
                .getMaterialFromName(XmlHelpers.getStringValue(rootElement,
                        "material"));

        Model[] models = loadModels(rootElement);
        RenderableModelNode[] nodes = new RenderableModelNode[models.length];

        if (type.equals(BLOCK_TYPE_NAME)) {
            MyTexture regularTexture = regularMaterial.getTexture();
            IntVector[] textureCoordinates = parseTextureCoordinate(
                    rootElement, models);

            for (int i = 0; i < nodes.length; ++i) {
                nodes[i] = new RenderableModelNode(shader, new Model(models[i]));
                nodes[i].addTexture("sampler", regularTexture);
                float inverseTextureWidth = 16.0f / 256;
                float inverseTextureHeight = 16.0f / 256;
                nodes[i].setUniform("inverseTextureUnitSize",
                        new FloatShaderUniform(inverseTextureWidth));

                // TODO: for some reason the item's u and v is reversed, that's
                // why
                // I
                // reversed here to make it consistent.
                nodes[i].setUniform("textureX", new FloatShaderUniform(
                        textureCoordinates[i].y * inverseTextureWidth));
                nodes[i].setUniform("textureY", new FloatShaderUniform(
                        textureCoordinates[i].x * inverseTextureHeight));
            }
        } else {
            for (int i = 0; i < nodes.length; ++i) {
                nodes[i] = new RenderableModelNode(shader, new Model(models[i]));
            }
        }
        String modelId = "block_" + id;
        modelLoader.registerModel(modelId, nodes);
        return modelId;
    }

    private Shader loadShader(Element rootElement) {
        String material = XmlHelpers.getStringValue(rootElement, "material");
        Shader shader = materialLoader.getMaterialFromName(material)
                .getShader();
        return shader;
    }

    private IntVector[] parseTextureCoordinate(Element rootElement,
            Model[] models) {
        Element textureCoordinateElement = XmlHelpers
                .findFirstElement(rootElement
                        .getElementsByTagName("block-side-textures"));
        IntVector[] textureCoordinates = loadTextureCoordinates(rootElement,
                textureCoordinateElement, models.length);
        return textureCoordinates;
    }

    private Model[] loadModels(Element rootElement) {
        String modelName = XmlHelpers.getStringValue(rootElement, "model");
        Model[] models = modelLoader.getModelFromName(modelName);
        return models;
    }

    private IntVector loadSpritePosition(Element rootElement) {
        IntVector spriteTexturePosition = new IntVector();
        spriteTexturePosition.x = XmlHelpers.parseIntegerValue(rootElement,
                "spriteU");
        spriteTexturePosition.y = XmlHelpers.parseIntegerValue(rootElement,
                "spriteV");
        return spriteTexturePosition;
    }

    private Material loadSpriteMaterial(Element rootElement) {
        String spriteMaterialName = XmlHelpers.getStringValue(rootElement,
                "sprite-material");
        Material spriteMaterial = materialLoader
                .getMaterialFromName(spriteMaterialName);
        return spriteMaterial;
    }

    private GameItemRightClickHandler loadRightClickHandler(Element rootElement) {
        String rightClickHandlerName = XmlHelpers.getStringValue(rootElement,
                "right-click-handler");
        GameItemRightClickHandler blockRightClickHandler = (GameItemRightClickHandler) context
                .getBean(rightClickHandlerName);
        return blockRightClickHandler;
    }

    private IntVector[] loadTextureCoordinates(Element rootElement,
            Element element, int length) {
        IntVector[] result = new IntVector[length];
        NodeList list = rootElement.getElementsByTagName("side");
        int i = 0;
        for (i = 0; i < list.getLength() && i < length; ++i) {
            IntVector textureCoordinate = XmlHelpers.parseTexture(list, i);
            result[i] = textureCoordinate;
        }

        if (i == 0) {
            throw new RuntimeException("No texture coordinate found");
        }

        for (; i < length; ++i) {
            result[i] = result[i - 1];
        }
        return result;
    }
}
