package com.helospark.mycraft.mycraft.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector3f;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.helospark.mycraft.mycraft.actor.Actor;
import com.helospark.mycraft.mycraft.actor.TransformComponent;
import com.helospark.mycraft.mycraft.mathutils.VectorMathUtils;
import com.helospark.mycraft.mycraft.messages.ActorLifeMessage;
import com.helospark.mycraft.mycraft.messages.GenericIntMessage;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageHandler;
import com.helospark.mycraft.mycraft.window.MessageListener;
import com.helospark.mycraft.mycraft.window.MessageTypes;

@Service
public class ActorSearchService implements MessageListener {

	private Map<Integer, Actor> actors = new HashMap<Integer, Actor>();
	private MessageHandler messager;
	private List<Integer> hittableActors = new ArrayList<>();

	public ActorSearchService() {
		ApplicationContext context = Singleton.getInstance().getContext();
		messager = context.getBean(MessageHandler.class);
		messager.registerListener(this, MessageTypes.NEW_ACTOR_MESSAGE);
		messager.registerListener(this, MessageTypes.DELETED_ACTOR_MESSAGE);
		messager.registerListener(this, MessageTypes.NEW_HITTABLE_ACTOR_COMPONENT);
		messager.registerListener(this, MessageTypes.REMOVED_HITTABLE_ACTOR_COMPONENT);
	}

	public void addActor(Actor actor) {
		int id = actor.getId();
		actors.put(id, actor);
	}

	public Actor findActorById(int actorId) {
		return actors.get(actorId);
	}

	@Override
	public boolean receiveMessage(Message message) {
		if (message.getType() == MessageTypes.NEW_ACTOR_MESSAGE) {
			handleNewActorMessage(message);
		} else if (message.getType() == MessageTypes.DELETED_ACTOR_MESSAGE) {
			handleDeletedActorMessage(message);
		} else if (message.getType() == MessageTypes.NEW_HITTABLE_ACTOR_COMPONENT) {
			GenericIntMessage intMessage = (GenericIntMessage) message;
			hittableActors.add(intMessage.getParameter());
		} else if (message.getType() == MessageTypes.REMOVED_HITTABLE_ACTOR_COMPONENT) {
			GenericIntMessage intMessage = (GenericIntMessage) message;
			hittableActors.remove(Integer.valueOf(intMessage.getParameter()));
		}
		return false;
	}

	private void handleDeletedActorMessage(Message message) {
		Actor actor = ((ActorLifeMessage) message).getActor();
		actor.remove();
		actors.remove(actor.getId());
	}

	private void handleNewActorMessage(Message message) {
		Actor actor = ((ActorLifeMessage) message).getActor();
		if (actor != null) {
			actors.put(actor.getId(), actor);
		}
	}

	public void updateActors(double deltaTime) {
		for (Map.Entry<Integer, Actor> entry : actors.entrySet()) {
			entry.getValue().update(deltaTime);
		}
	}

	public Actor getLocalHumanPlayer() {
		for (Map.Entry<Integer, Actor> entry : actors.entrySet()) {
			if (entry.getValue().isHumanPlayer() && entry.getValue().isLocalPlayer()) {
				return entry.getValue();
			}
		}
		return null;
	}

	public List<Actor> getActorsCloseToPosition(Vector3f position, float distance) {
		List<Actor> result = new ArrayList<>();
		float distanceSquared = distance * distance;
		for (int i = 0; i < hittableActors.size(); ++i) {
			Actor actor = actors.get(hittableActors.get(i));
			if (actor != null) {
				TransformComponent transformComponent = (TransformComponent) actor
						.getComponent(TransformComponent.TRANSFORM_COMPONENT_NAME);
				if (transformComponent != null
						&& VectorMathUtils.distanceSquareBetween(position,
								transformComponent.getPosition()) < distanceSquared)
					result.add(actor);
			}
		}
		return result;
	}
}
