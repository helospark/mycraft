package com.helospark.mycraft.mycraft.messages;

import org.springframework.context.ApplicationContext;

import com.helospark.mycraft.mycraft.actor.GameItem;
import com.helospark.mycraft.mycraft.actor.GameItems;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public class ActiveInventoryItemChangedMessage extends Message {
	GameItem newInventoryItem;
	int actorId;
	GameItems items;

	public ActiveInventoryItemChangedMessage(MessageTypes type, int targetId,
			GameItem newInventoryItem, int actorId) {
		super(type, targetId);
		this.actorId = actorId;
		this.newInventoryItem = newInventoryItem;
		ApplicationContext context = Singleton.getInstance().getContext();
		items = context.getBean(GameItems.class);
	}

	public GameItem getNewInventoryItem() {
		return newInventoryItem;
	}

	public void setNewInventoryItem(GameItem newInventoryItem) {
		this.newInventoryItem = newInventoryItem;
	}

	public int getActorId() {
		return actorId;
	}

	public void setActorId(int actorId) {
		this.actorId = actorId;
	}

	@Override
	public String serializeToString() {
		String result = newInventoryItem.getId() + ";" + actorId;
		return result;
	}

	@Override
	public Message deserializeFromString(String[] data, MessageTypes messageType) {
		if (data.length != 2) {
			throw new RuntimeException("Unable to serialize");
		}
		GameItem newGameItem = new GameItem();
		int itemId = Integer.parseInt(data[0]);
		items.getById(itemId);
		int newActorId = Integer.parseInt(data[1]);
		ActiveInventoryItemChangedMessage result = new ActiveInventoryItemChangedMessage(
				messageType, Message.MESSAGE_TARGET_ANYONE, newGameItem,
				newActorId);
		return result;
	}
}
