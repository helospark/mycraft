package com.helospark.mycraft.mycraft.factories;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

public class CreateObjectFromXMLFactory {

	private static Map<String, CreatableFromXML> creationMethods = new HashMap<String, CreatableFromXML>();
	private static final Logger LOGGER = LoggerFactory
			.getLogger(CreateObjectFromXMLFactory.class);

	public static <T> T create(Element node) {
		String name = node.getAttribute("type");
		CreatableFromXML objectToCreate = creationMethods.get(name);
		if (objectToCreate == null) {
			LOGGER.error("Unable to find " + name + " for factory");
		}
		T createdObject = (T) objectToCreate.createFromXML(node);
		return createdObject;
	}

	public static void add(String name, CreatableFromXML creationInterface) {
		creationMethods.put(name, creationInterface);
	}
}
