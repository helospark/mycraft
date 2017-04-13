package com.helospark.mycraft.mycraft.views;

import org.springframework.context.ApplicationContext;

import com.helospark.mycraft.mycraft.actor.GameItems;
import com.helospark.mycraft.mycraft.actor.InventoryComponent;
import com.helospark.mycraft.mycraft.actor.InventoryItem;
import com.helospark.mycraft.mycraft.mathutils.IntVector;
import com.helospark.mycraft.mycraft.messages.GenericMessage;
import com.helospark.mycraft.mycraft.messages.MouseMotionMessage;
import com.helospark.mycraft.mycraft.messages.MouseStateChangeMessage;
import com.helospark.mycraft.mycraft.render.SpriteAndTextBatchData;
import com.helospark.mycraft.mycraft.services.ActorSearchService;
import com.helospark.mycraft.mycraft.services.SpriteWriterService;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageTypes;
import com.helospark.mycraft.mycraft.window.Window;

public abstract class InventoryItemView extends View {

	protected boolean isActive;
	protected ActorSearchService actorSearchService;
	protected GameItems items;
	protected SpriteWriterService spriteWriterService;
	protected InventoryComponent inventory = null;
	protected Window window;
	protected InventoryItem heldItem = null;

	// cache
	private IntVector mousePosition = new IntVector();

	ItemDrawerService itemDrawerService;

	public InventoryItemView(int id) {
		super(id);
		ApplicationContext context = Singleton.getInstance().getContext();
		window = context.getBean(Window.class);
		items = context.getBean(GameItems.class);
		spriteWriterService = context.getBean(SpriteWriterService.class);
		actorSearchService = context.getBean(ActorSearchService.class);
		itemDrawerService = context.getBean(ItemDrawerService.class);
	}

	@Override
	public void fillBuffers(SpriteAndTextBatchData spriteTextBatchData) {
		if (heldItem != null) {
			itemDrawerService.drawOneItemAt(spriteTextBatchData, mousePosition.toVector3f(),
					heldItem);
		}
	}

	public void handleMouseEvents(Message message, MenuActions[] menuActions) {
		if (message.getType() == MessageTypes.MOUSE_MOTION_MESSAGE) {
			MouseMotionMessage mouseMessage = (MouseMotionMessage) message;
			mousePosition.x = mouseMessage.getNewX();
			mousePosition.y = mouseMessage.getNewY();
			for (int i = 0; i < menuActions.length; ++i) {
				menuActions[i].onMouseMotion(mousePosition);
			}
		} else if (message.getType() == MessageTypes.MOUSE_STATE_CHANGED_MESSAGE) {
			MouseStateChangeMessage mouseMessage = (MouseStateChangeMessage) message;
			mousePosition.x = mouseMessage.getX();
			mousePosition.y = mouseMessage.getY();
			if (mouseMessage.getMouseMessageType() == MouseStateChangeMessage.MOUSE_BUTTON_PRESSED) {
				for (int i = 0; i < menuActions.length; ++i) {
					if (menuActions[i].isPositionOver(mousePosition)) {
						int amount = 64;
						if (mouseMessage.getButton() == MouseStateChangeMessage.MOUSE_BUTTON_RIGHT) {
							amount = 1;
						}
						heldItem = menuActions[i].onMouseDown(mousePosition, heldItem, amount);
					}
				}
			} else {
				for (int i = 0; i < menuActions.length; ++i) {
					if (menuActions[i].isPositionOver(mousePosition)) {
						heldItem = menuActions[i].onMouseUp(mousePosition, heldItem);
					}
				}
			}
		}
	}

	protected void handleCommonMessage(Message message, MenuActions[] menuActions) {
		if (message.getType() == MessageTypes.CLOSE_ALL_OPTIONAL_VIEWS) {
			isActive = false;
			hideCursor();
			removeView();
		} else if (message.getType() == MessageTypes.MOUSE_MOTION_MESSAGE
				|| message.getType() == MessageTypes.MOUSE_STATE_CHANGED_MESSAGE) {
			if (isActive && menuActions != null) {
				handleMouseEvents(message, menuActions);
			}
		}
	}

	public void showCursor() {
		Message cursorMessage = new GenericMessage(MessageTypes.TURN_ON_CURSOR,
				Message.MESSAGE_TARGET_ANYONE);
		messager.sendMessage(cursorMessage);
	}

	public void hideCursor() {
		Message cursorMessage = new GenericMessage(MessageTypes.TURN_OFF_CURSOR,
				Message.MESSAGE_TARGET_ANYONE);
		messager.sendMessage(cursorMessage);
	}

	protected void handleToggle() {
		isActive = !isActive;
		if (isActive) {
			addView();
			showCursor();
		} else {
			removeView();
			hideCursor();
		}
	}

	protected void fillCommonBuffer(SpriteAndTextBatchData spriteTextBatchData,
			MenuActions[] menuActions) {
		itemDrawerService.drawInventory(spriteTextBatchData, menuActions[1]);
		itemDrawerService.drawHandView(spriteTextBatchData, menuActions[2]);
		itemDrawerService.drawCraftingView(spriteTextBatchData, (CraftMenuActions) menuActions[0]);
	}
}
