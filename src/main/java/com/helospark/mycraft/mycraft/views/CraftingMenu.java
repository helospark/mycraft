package com.helospark.mycraft.mycraft.views;

import org.lwjgl.util.vector.Vector3f;
import org.springframework.context.ApplicationContext;

import com.helospark.mycraft.mycraft.actor.Actor;
import com.helospark.mycraft.mycraft.actor.InventoryComponent;
import com.helospark.mycraft.mycraft.render.SpriteAndTextBatchData;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageHandler;
import com.helospark.mycraft.mycraft.window.MessageListener;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public class CraftingMenu extends InventoryItemView implements MessageListener {
	private MessageHandler messager;
	MenuActions[] menuActions;

	public CraftingMenu(int id) {
		super(id);
		ApplicationContext context = Singleton.getInstance().getContext();
		messager = context.getBean(MessageHandler.class);
		messager.registerListener(this, MessageTypes.TURN_ON_CRAFTING_VIEW);
		messager.registerListener(this, MessageTypes.MOUSE_MOTION_MESSAGE);
		messager.registerListener(this, MessageTypes.MOUSE_STATE_CHANGED_MESSAGE);
		messager.registerListener(this, MessageTypes.CLOSE_ALL_OPTIONAL_VIEWS);
	}

	@Override
	public void fillBuffers(SpriteAndTextBatchData spriteTextBatchData) {
		fillCommonBuffer(spriteTextBatchData, menuActions);
		super.fillBuffers(spriteTextBatchData);
	}

	@Override
	public boolean receiveMessage(Message message) {
		handleCommonMessage(message, menuActions);
		if (message.getType() == MessageTypes.TURN_ON_CRAFTING_VIEW) {
			handleToggleCraftingView();
		}
		return false;
	}

	private void handleToggleCraftingView() {
		if (menuActions == null) {

			Actor localActor = actorSearchService.getLocalHumanPlayer();

			inventory = (InventoryComponent) localActor
					.getComponent(InventoryComponent.INVENTORY_COMPONENT_NAME);

			menuActions = new MenuActions[3];
			menuActions[0] = new CraftMenuActions(new Vector3f(20, 20, 0), 3);
			menuActions[1] = new InventoryMenuActions(new Vector3f(20, 150, 0), 10, 4,
					inventory.getInventoryElementList(), inventory.getDefaultItem());
			menuActions[2] = new InventoryMenuActions(new Vector3f(20, 350, 0), 10, 1,
					inventory.getHandItemList(), inventory.getDefaultItem());
		}
		isActive = true;
		addView();
		showCursor();
	}
}
