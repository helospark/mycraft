package com.helospark.mycraft.mycraft.actor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.helospark.mycraft.mycraft.helpers.XmlHelpers;

public class ActorPropertyComponent extends ActorComponent {
	public static final String ACTOR_PROPERTY_COMPONENT_NAME = "ActorPropertyComponent";
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ActorPropertyComponent.class);
	private static final String XML_SPEED_NODE_NAME = "speed";

	private double speed;

	public ActorPropertyComponent() {
		super(ACTOR_PROPERTY_COMPONENT_NAME);
	}

	@Override
	public Object createFromXML(Element element) {
		if (!checkIfRightElement(element.getAttribute("type"))) {
			return new ActorPropertyComponent();
		}

		ActorPropertyComponent createdComponent = new ActorPropertyComponent();

		createdComponent.speed = XmlHelpers.parseDoubleValue(element, "speed");

		return createdComponent;
	}

	@Override
	public void afterInit() {

	}

	@Override
	public void update(double deltaTime) {

	}

	@Override
	public void onRemove() {

	}

	public double getSpeed() {
		return speed;
	}

	@Override
	public ActorComponent createNew() {
		ActorPropertyComponent created = new ActorPropertyComponent();
		super.createNewCommon(created);
		created.speed = speed;
		return created;
	}

}
