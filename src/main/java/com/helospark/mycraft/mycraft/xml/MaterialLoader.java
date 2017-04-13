package com.helospark.mycraft.mycraft.xml;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;

import com.helospark.mycraft.mycraft.helpers.XmlHelpers;
import com.helospark.mycraft.mycraft.render.Material;
import com.helospark.mycraft.mycraft.render.MyTexture;
import com.helospark.mycraft.mycraft.render.MyTextureLoader;
import com.helospark.mycraft.mycraft.shader.Shader;
import com.helospark.mycraft.mycraft.shader.ShaderLoader;

@Service
public class MaterialLoader implements XmlLoader {

	@Autowired
	MyTextureLoader textureLoader;

	@Autowired
	ShaderLoader shaderLoader;

	private Map<String, Material> loadedMaterials = new HashMap<>();

	@Override
	public void parseXml(Element rootElement) {
		String name = rootElement.getAttribute("name");

		if (rootElement.getElementsByTagName("textures").getLength() > 0) {
			Element texturesRoot = XmlHelpers.findFirstElement(rootElement
					.getElementsByTagName("textures"));
			textureLoader.parseXml(texturesRoot);
		}

		String textureName = XmlHelpers.getStringValue(rootElement, "texture");
		Element textureElement = XmlHelpers.findFirstElement(rootElement
				.getElementsByTagName("texture"));
		String textureid = "";
		if (textureElement != null) {
			textureid = textureElement.getAttribute("name");
		}
		String shaderName = XmlHelpers.getStringValue(rootElement, "shader");
		boolean transparent = XmlHelpers.parseBooleanValue(rootElement, "is-transparent");

		Shader shader = shaderLoader.getFromName(shaderName);

		Material material = new Material(shader);
		MyTexture texture = textureLoader.getTextureFromName(textureName);
		if (texture != null) {
			material.addTexture(textureid, texture);
		}
		material.setTransparent(transparent);
		loadedMaterials.put(name, material);
	}

	public Material getMaterialFromName(String materialName) {
		return loadedMaterials.get(materialName);
	}

	public Material readFromXml(String fileName) {
		Element rootElement = XmlHelpers.openFileAndGetRootElement(fileName);
		parseXml(rootElement);
		return loadedMaterials.get(fileName);
	}

}
