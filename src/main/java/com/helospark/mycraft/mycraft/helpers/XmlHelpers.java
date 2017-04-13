package com.helospark.mycraft.mycraft.helpers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.lwjgl.util.vector.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.helospark.mycraft.mycraft.mathutils.IntVector;

public class XmlHelpers {
	private static final Logger LOGGER = LoggerFactory.getLogger(XmlHelpers.class);

	public static Document loadXMLDocumentFromFileName(String fileName) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		Document document = null;
		try {
			builder = factory.newDocumentBuilder();
			document = builder.parse(fileName);
		} catch (ParserConfigurationException e) {
			LOGGER.error(e.getMessage());
		} catch (SAXException e) {
			LOGGER.error(e.getMessage());
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}
		document.normalize();
		return document;
	}

	public static Element getFirstChildElement(Node node) {
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				return (Element) child;
			}
		}
		LOGGER.warn("Unable to find a sufficient child element");
		return null;
	}

	public static double parseDoubleValue(Element element, String name) {
		NodeList nodes = element.getElementsByTagName(name);
		if (nodes.getLength() != 1) {
			LOGGER.warn("There must be exactly one " + name + " element present, returrn default");
			return 0;
		} else {
			try {
				return Double.parseDouble(nodes.item(0).getTextContent());
				// throw new RuntimeException("Not implemnted");
			} catch (NumberFormatException ex) {
				LOGGER.warn("Unable to parse double for " + name + " " + ex.getStackTrace());
				return 0;
			}
		}
	}

	public static Vector3f parseVector(Element element, String name) {
		NodeList nodes = element.getElementsByTagName(name);
		if (nodes.getLength() != 1) {
			LOGGER.warn("There must be exactly one " + name + " element present, returrn default");
			return new Vector3f(0.0f, 0.0f, 0.0f);
		} else {
			Element vectorNode = (Element) nodes.item(0);
			try {
				double x = Double.parseDouble(vectorNode.getAttribute("x"));
				double y = Double.parseDouble(vectorNode.getAttribute("y"));
				double z = Double.parseDouble(vectorNode.getAttribute("z"));
				return new Vector3f((float) x, (float) y, (float) z);
			} catch (NumberFormatException ex) {
				LOGGER.warn("Unable to parse vector, number format exception " + ex.getMessage());
				return new Vector3f(0, 0, 0);
			}
		}
	}

	public static String getStringValue(Element element, String name) {
		NodeList nodes = element.getElementsByTagName(name);
		if (nodes.getLength() != 1) {
			LOGGER.warn("There must be exactly one " + name
					+ " element present, return empty string");
			return "";
		} else {
			Element elementNode = (Element) nodes.item(0);
			return elementNode.getTextContent();
			// throw new RuntimeException("Not implemnted");
		}
	}

	public static int parseIntegerValue(Element element, String name) {
		NodeList nodes = element.getElementsByTagName(name);
		if (nodes.getLength() != 1) {
			LOGGER.warn("There must be exactly one " + name + " element present, returrn default");
			return 0;
		} else {
			try {
				return Integer.parseInt(nodes.item(0).getTextContent());
				// throw new RuntimeException("Not implemnted");
			} catch (NumberFormatException ex) {
				LOGGER.warn("Unable to parse int for " + name + " " + ex.getStackTrace());
				return 0;
			}
		}
	}

	public static Element findFirstElement(NodeList nodeList) {
		for (int i = 0; i < nodeList.getLength(); ++i) {
			if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
				return (Element) nodeList.item(i);
			}
		}
		return null;
	}

	public static boolean parseBooleanValue(Element element, String name) {
		NodeList nodes = element.getElementsByTagName(name);
		if (nodes.getLength() != 1) {
			LOGGER.warn("There must be exactly one " + name + " element present, return false");
			return false;
		} else {
			try {
				return Boolean.parseBoolean(nodes.item(0).getTextContent());
			} catch (NumberFormatException ex) {
				LOGGER.warn("Unable to parse boolean for " + name + " " + ex.getStackTrace());
				return false;
			}
		}
	}

	public static IntVector parseTexture(NodeList textureCoordinateNodeList, int i) {
		Element element = (Element) textureCoordinateNodeList.item(i);
		int u = XmlHelpers.parseIntegerValue(element, "u");
		int v = XmlHelpers.parseIntegerValue(element, "v");
		return new IntVector(u, v, 0);
	}

	public static int parseIntegerValue(Element node) {
		return Integer.parseInt(node.getTextContent());
	}

	public static List<Integer> parseIntegerList(Element rootElement, String itemName) {
		NodeList itemList = rootElement.getElementsByTagName(itemName);
		List<Integer> result = new ArrayList<>();
		for (int i = 0; i < itemList.getLength(); ++i) {
			Node node = itemList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				int parsedInteger = XmlHelpers.parseIntegerValue((Element) node);
				result.add(parsedInteger);
			}
		}
		return result;
	}

	public static List<String> getStringList(Element rootElement, String name) {
		List<String> result = new ArrayList<>();
		NodeList nList = rootElement.getElementsByTagName(name);
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node node = nList.item(temp);

			if (node.getNodeType() == Node.ELEMENT_NODE) {
				result.add(((Element) node).getTextContent());
			}
		}
		return result;
	}

	public static Element openFileAndGetRootElement(String fileName) {
		Document document = loadXMLDocumentFromFileName(fileName);
		Element rootNode = document.getDocumentElement();
		return XmlHelpers.findFirstElement(rootNode.getChildNodes());
	}

	public static List<String> parseStringList(Element rootElement, String itemName) {
		NodeList itemList = rootElement.getElementsByTagName(itemName);
		List<String> result = new ArrayList<>();
		for (int i = 0; i < itemList.getLength(); ++i) {
			Node node = itemList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				String string = node.getTextContent();
				result.add(string);
			}
		}
		return result;
	}
}