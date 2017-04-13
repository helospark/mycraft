package com.helospark.mycraft.mycraft.views;

import org.lwjgl.util.vector.Vector3f;
import org.springframework.context.ApplicationContext;

import com.helospark.mycraft.mycraft.actor.Actor;
import com.helospark.mycraft.mycraft.actor.InventoryComponent;
import com.helospark.mycraft.mycraft.messages.ActorLifeMessage;
import com.helospark.mycraft.mycraft.render.SpriteAndTextBatchData;
import com.helospark.mycraft.mycraft.services.GlobalParameters;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageListener;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public class HandItemView extends InventoryItemView implements MessageListener {
	private int actorId;

	ItemDrawerService itemDrawerService;
	GlobalParameters globalParameters;
	// cache
	private Vector3f position = new Vector3f();
	private MenuActions menuAction = null;

	public HandItemView(int id) {
		super(id);
		messager.registerListener(this, MessageTypes.NEW_ACTOR_MESSAGE);
		messager.registerListener(this, MessageTypes.DELETED_ACTOR_MESSAGE);

		ApplicationContext context = Singleton.getInstance().getContext();
		itemDrawerService = context.getBean(ItemDrawerService.class);
		globalParameters = context.getBean(GlobalParameters.class);
	}

	@Override
	public boolean receiveMessage(Message message) {
		if (message.getType() == MessageTypes.NEW_ACTOR_MESSAGE) {
			ActorLifeMessage actorLifeMessage = (ActorLifeMessage) message;
			Actor actor = actorLifeMessage.getActor();
			if (actor.isLocalPlayer() && actor.isHumanPlayer()) {
				InventoryComponent tmpInventory = (InventoryComponent) actor
						.getComponent(InventoryComponent.INVENTORY_COMPONENT_NAME);
				if (tmpInventory != null) {
					inventory = tmpInventory;
					isActive = true;
					this.actorId = actor.getId();
					addView();
					menuAction = new InventoryMenuActions(
							new Vector3f(20, globalParameters.initialWindowHeight
									- MenuActions.SCALE_VECTOR.y, 0), 10, 1,
							inventory.getHandItemList(), inventory.getDefaultItem());
				}
			}
		} else if (message.getType() == MessageTypes.DELETED_ACTOR_MESSAGE) {
			ActorLifeMessage actorLifeMessage = (ActorLifeMessage) message;
			int deletedActorId = actorLifeMessage.getActor().getId();
			if (actorId == deletedActorId) {
				actorId = -1;
				isActive = false;
				inventory = null;
				removeView();
			}
		}
		return false;
	}

	@Override
	public void fillBuffers(SpriteAndTextBatchData spriteTextBatchData) {
		menuAction.setActiveElement(inventory.getCurrentGameItemIndex());
		itemDrawerService.drawHandView(spriteTextBatchData, menuAction);
	}
}
