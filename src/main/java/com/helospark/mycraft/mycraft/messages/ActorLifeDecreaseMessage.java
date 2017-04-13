package com.helospark.mycraft.mycraft.messages;

import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public class ActorLifeDecreaseMessage extends Message {

	private int amount;
	private int actorId;

	public ActorLifeDecreaseMessage(MessageTypes messageType) {
		super(messageType, Message.MESSAGE_TARGET_ANYONE);
	}

	public ActorLifeDecreaseMessage(MessageTypes messageType, int actorId,
			int amount) {
		super(messageType, Message.MESSAGE_TARGET_ANYONE);
		this.amount = amount;
		this.actorId = actorId;
	}

	@Override
	public String serializeToString() {
		return actorId + ";" + amount;
	}

	@Override
	public Message deserializeFromString(String[] data, MessageTypes messageType) {
		int amount = Integer.parseInt(data[0]);
		int actorId = Integer.parseInt(data[1]);
		return new ActorLifeDecreaseMessage(messageType, actorId, amount);
	}

	public int getActorId() {
		return actorId;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public void setActorId(int actorId) {
		this.actorId = actorId;
	}

}
