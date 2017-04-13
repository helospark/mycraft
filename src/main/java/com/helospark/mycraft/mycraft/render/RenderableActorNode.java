package com.helospark.mycraft.mycraft.render;

import com.helospark.mycraft.mycraft.actor.Actor;
import com.helospark.mycraft.mycraft.actor.TransformComponent;
import com.helospark.mycraft.mycraft.attributes.Model;
import com.helospark.mycraft.mycraft.shader.Shader;

public class RenderableActorNode extends RenderableModelNode {

	Actor actor;

	public RenderableActorNode(Shader shader, Model initFrom, Actor actor) {
		super(shader, initFrom);
		this.actor = actor;
	}

	public RenderableActorNode(RenderableActorNode actorNode) {
		super(actorNode);
		this.actor = actorNode.actor;
	}

	@Override
	public void preRender() {
		TransformComponent component = (TransformComponent) actor
				.getComponent(TransformComponent.TRANSFORM_COMPONENT_NAME);
		position = component.getPosition();
		super.preRender();
	}
}
