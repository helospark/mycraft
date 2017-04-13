package com.helospark.mycraft.mycraft.views;

import org.lwjgl.util.vector.Vector3f;
import org.springframework.context.ApplicationContext;

import com.helospark.mycraft.mycraft.actor.Actor;
import com.helospark.mycraft.mycraft.actor.InventoryComponent;
import com.helospark.mycraft.mycraft.messages.GenericIntMessage;
import com.helospark.mycraft.mycraft.render.SpriteAndTextBatchData;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageListener;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public class InventoryView extends InventoryItemView implements MessageListener {

	private Vector3f position = new Vector3f();
	ItemDrawerService itemDrawerService;
	MenuActions[] menuActions = null;

	public InventoryView(int id) {
		super(id);
		messager.registerListener(this, MessageTypes.TOGGLE_INVENTORY_MESSAGE);
		messager.registerListener(this, MessageTypes.MOUSE_MOTION_MESSAGE);
		messager.registerListener(this, MessageTypes.MOUSE_STATE_CHANGED_MESSAGE);
		messager.registerListener(this, MessageTypes.CLOSE_ALL_OPTIONAL_VIEWS);
		ApplicationContext context = Singleton.getInstance().getContext();
		itemDrawerService = context.getBean(ItemDrawerService.class);
	}

	@Override
	public boolean receiveMessage(Message message) {
		handleCommonMessage(message, menuActions);
		if (message.getType() == MessageTypes.TOGGLE_INVENTORY_MESSAGE) {
			handleToggleInventory(message);
		}
		return false;
	}

	private void handleToggleInventory(Message message) {
		GenericIntMessage intMessage = (GenericIntMessage) message;
		int senderId = intMessage.getParameter();
		Actor localPlayer = actorSearchService.getLocalHumanPlayer();
		if (localPlayer == null) {
			return;
		}
		if (inventory == null) {
			inventory = (InventoryComponent) localPlayer
					.getComponent(InventoryComponent.INVENTORY_COMPONENT_NAME);
		}
		if (localPlayer.getId() == senderId) {
			handleToggle();
		}
		if (menuActions == null) {
			menuActions = new MenuActions[3];
			menuActions[0] = new CraftMenuActions(new Vector3f(20, 20, 0), 2);
			menuActions[1] = new InventoryMenuActions(new Vector3f(20, 150, 0), 10, 4,
					inventory.getInventoryElementList(), inventory.getDefaultItem());
			menuActions[2] = new InventoryMenuActions(new Vector3f(20, 350, 0), 10, 1,
					inventory.getHandItemList(), inventory.getDefaultItem());
		}
	}

	@Override
	public void fillBuffers(SpriteAndTextBatchData spriteTextBatchData) {
		fillCommonBuffer(spriteTextBatchData, menuActions);
		super.fillBuffers(spriteTextBatchData);
	}
}
