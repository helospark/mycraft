package com.helospark.mycraft.mycraft.actor;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.helospark.mycraft.mycraft.factories.CreateObjectFromXMLFactory;
import com.helospark.mycraft.mycraft.factories.IDFactory;
import com.helospark.mycraft.mycraft.helpers.XmlHelpers;

public class Actor {
	private static final Logger LOGGER = LoggerFactory.getLogger(Actor.class);
	Map<String, ActorComponent> components = new HashMap<String, ActorComponent>();
	int id;
	private boolean isLocalPlayer = true;
	private boolean isHumanPlayer = true;

	public Actor(int id) {
		this.id = id;
	}

	public Actor(int id, String xmlFileName) {
		this.id = id;
		createComponentsFromXMLFile(xmlFileName);
	}

	private void createComponentsFromXMLFile(String xmlFileName) {
		Document document = XmlHelpers.loadXMLDocumentFromFileName(xmlFileName);

		Node root = document.getFirstChild();
		NodeList rootComponentNodes = root.getChildNodes();
		loadAllComponentsFromNodeList(rootComponentNodes);
	}

	private void loadAllComponentsFromNodeList(NodeList rootComponentNodes) {
		for (int i = 0; i < rootComponentNodes.getLength(); i++) {
			Node componentNode = rootComponentNodes.item(i);
			if (componentNode.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) componentNode;
				createComponentFromNode(element);
			}
		}
	}

	private void createComponentFromNode(Element element) {
		ActorComponent component = CreateObjectFromXMLFactory
				.<ActorComponent> create(element);
		component.setOwner(this);
		component.afterInit();
		addComponent(component.getName(), component);
	}

	public void addComponent(String componentName, ActorComponent component) {
		if (components.get(componentName) != null) {
			LOGGER.warn("Trying to add a component with " + componentName
					+ " but this does already exists withing actor with id "
					+ id);
		}
		components.put(componentName, component);
	}

	public void removeComponent(String componentName) {
		ActorComponent componentToRemove = components.remove(componentName);
		if (componentToRemove == null) {
			LOGGER.warn("Trying to remove " + componentName
					+ " from actor with id " + id
					+ " but this component does not currently exists.");
		}
	}

	public void update(double deltaTime) {
		for (ActorComponent component : components.values()) {
			component.update(deltaTime);
		}
	}

	public ActorComponent getComponent(String transformComponentName) {
		for (ActorComponent component : components.values()) {
			if (component.getName().equals(transformComponentName))
				return component;
		}
		LOGGER.warn("Trying to find " + transformComponentName
				+ " but it's not part of the actor");
		return null;
	}

	public int getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Actor createNew() {
		Actor actor = new Actor(IDFactory.getNextId());

		for (ActorComponent component : components.values()) {
			ActorComponent clonedComponent = component.createNew();
			actor.addComponent(clonedComponent.getName(), clonedComponent);
		}
		return actor;
	}

	public void remove() {
		for (ActorComponent component : components.values()) {
			component.onRemove();
		}
		components.clear();
	}

	public boolean isLocalPlayer() {
		return isLocalPlayer;
	}

	public boolean isHumanPlayer() {
		return isHumanPlayer;
	}
}
