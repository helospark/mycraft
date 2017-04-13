package com.helospark.mycraft.mycraft.xml;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.context.ApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.helospark.mycraft.mycraft.helpers.XmlHelpers;
import com.helospark.mycraft.mycraft.render.MyTextureLoader;
import com.helospark.mycraft.mycraft.services.CraftFileLoader;
import com.helospark.mycraft.mycraft.services.GlobalParameters;
import com.helospark.mycraft.mycraft.services.ToolFileReader;
import com.helospark.mycraft.mycraft.shader.ShaderLoader;
import com.helospark.mycraft.mycraft.singleton.Singleton;

public class XmlParser implements XmlLoader {
	private Map<String, XmlLoader> tagLoaders = new HashMap<>();
	ShaderLoader shaderLoader;
	MaterialLoader materialHandler;
	BlockLoader blockLoader;
	GameItemLoader gameItemLoader;
	MyTextureLoader textureLoader;
	ModelLoader modelLoader;
	ItemBetterListLoader itemBetterListLoader;
	GlobalParameters globalParameters;
	ToolFileReader toolFileReader;
	CraftFileLoader craftFileLoader;

	public XmlParser() {
		ApplicationContext context = Singleton.getInstance().getContext();
		shaderLoader = context.getBean(ShaderLoader.class);
		materialHandler = context.getBean(MaterialLoader.class);
		blockLoader = context.getBean(BlockLoader.class);
		gameItemLoader = context.getBean(GameItemLoader.class);
		textureLoader = context.getBean(MyTextureLoader.class);
		modelLoader = context.getBean(ModelLoader.class);
		itemBetterListLoader = context.getBean(ItemBetterListLoader.class);
		globalParameters = context.getBean(GlobalParameters.class);
		craftFileLoader = context.getBean(CraftFileLoader.class);
		toolFileReader = context.getBean(ToolFileReader.class);
		tagLoaders.put("shader", shaderLoader);
		tagLoaders.put("material", materialHandler);
		tagLoaders.put("block", blockLoader);
		tagLoaders.put("inventory-item", gameItemLoader);
		tagLoaders.put("texture", textureLoader);
		tagLoaders.put("model", modelLoader);
		tagLoaders.put("item-better-list", itemBetterListLoader);
		tagLoaders.put("init-data", globalParameters);
		tagLoaders.put("tool-files", toolFileReader);
		tagLoaders.put("craft-files", craftFileLoader);
		tagLoaders.put("texture-files", this);
		tagLoaders.put("shader-files", this);
		tagLoaders.put("material-files", this);
		tagLoaders.put("model-files", this);
		tagLoaders.put("inventory-item-files", this);
		tagLoaders.put("block-files", this);
	}

	public void parseXml(String fileName) {
		System.out.println(fileName);
		try {
			Document document = openAsXmlFile(fileName);
			Element rootElement = getRootElement(document);
			iterateOverChildElements(rootElement);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			// throw new RuntimeException("Unabel to parse xml " +
			// e.getMessage() + " " + fileName);
		}
	}

	private Element getRootElement(Document document) {
		Element rootNode = document.getDocumentElement();
		return rootNode;
	}

	private Document openAsXmlFile(String fileName) {
		try {
			File fXmlFile = new File(fileName);
			if (!fXmlFile.exists()) {
				throw new RuntimeException("File does not exists " + fileName);
			}
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = dbFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(fXmlFile);
			document.getDocumentElement().normalize();
			return document;
		} catch (Exception e) {
			throw new RuntimeException("Error while opening XML file ", e);
		}
	}

	private void iterateOverChildElements(Element rootNode) {
		NodeList nList = rootNode.getChildNodes();

		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node node = nList.item(temp);

			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) node;

				String tagName = element.getTagName();
				tagLoaders.get(tagName).parseXml(element);

			}
		}
	}

	@Override
	public void parseXml(Element rootElement) {
		List<String> files = XmlHelpers.getStringList(rootElement, "file");
		for (int i = 0; i < files.size(); ++i) {
			parseXml(files.get(i));
		}
	}
}
