package com.helospark.mycraft.mycraft.messages;

import org.springframework.context.ApplicationContext;

import com.helospark.mycraft.mycraft.actor.Actor;
import com.helospark.mycraft.mycraft.services.ActorSearchService;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public class ActorLifeMessage extends Message {

	private Actor actor;
	ActorSearchService actorSearchService;

	public ActorLifeMessage(MessageTypes type, int targetId, Actor actor) {
		super(type, targetId);
		this.actor = actor;
	}

	public ActorLifeMessage(MessageTypes newActorMessage) {
		// TODO Auto-generated constructor stub
	}

	public Actor getActor() {
		return actor;
	}

	@Override
	public String serializeToString() {
		int actorId = actor.getId();
		String result = String.valueOf(actorId);
		return result;
	}

	@Override
	public Message deserializeFromString(String[] data, MessageTypes messageType) {
		if (data.length != 1) {
			throw new RuntimeException("Unable to deserialize");
		}
		ApplicationContext context = Singleton.getInstance().getContext();
		actorSearchService = context.getBean(ActorSearchService.class);
		int newActorId = Integer.parseInt(data[0]);
		Actor foundActor = actorSearchService.findActorById(newActorId);
		if (foundActor == null) {
			// throw new RuntimeException("Haven't found actor");
			System.out.println("Haven't found actor");
		}
		ActorLifeMessage result = new ActorLifeMessage(messageType,
				Message.MESSAGE_TARGET_ANYONE, foundActor);
		return result;
	}

}
