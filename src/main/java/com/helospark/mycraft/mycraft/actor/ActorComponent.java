package com.helospark.mycraft.mycraft.actor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helospark.mycraft.mycraft.factories.CreatableFromXML;

public abstract class ActorComponent implements CreatableFromXML {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ActorComponent.class);

	String name;
	Actor owner;

	public ActorComponent(String name) {
		this.name = name;
	}

	protected boolean checkIfRightElement(String attribute) {
		if (!attribute.equals(name)) {
			LOGGER.error("Trying to create a " + name
					+ " from an XML with type: " + attribute
					+ " returning empty component");
			return false;
		}
		return true;
	}

	public abstract void afterInit();

	public abstract void update(double deltaTime);

	public abstract void onRemove();

	public void setOwner(Actor owner) {
		this.owner = owner;
	}

	public String getName() {
		return name;
	}

	public abstract ActorComponent createNew();

	public void createNewCommon(ActorComponent controller) {
		controller.name = new String(name);
		controller.owner = owner;
	}
}
