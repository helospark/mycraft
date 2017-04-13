package com.helospark.mycraft.mycraft.blockrightclickhandler;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.helospark.mycraft.mycraft.game.Block;
import com.helospark.mycraft.mycraft.mathutils.IntVector;
import com.helospark.mycraft.mycraft.messages.GenericMessage;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageHandler;
import com.helospark.mycraft.mycraft.window.MessageTypes;

@Service
public class OpenCraftingMenuRightClickHandler extends BlockRightClickHandler {
	private MessageHandler messager;

	public OpenCraftingMenuRightClickHandler() {
		ApplicationContext context = Singleton.getInstance().getContext();
		messager = context.getBean(MessageHandler.class);
	}

	@Override
	public boolean onRightClick(Block clickedBlock, IntVector position, int itemId) {
		Message message = new GenericMessage(MessageTypes.TURN_ON_CRAFTING_VIEW,
				Message.MESSAGE_TARGET_ANYONE);
		messager.sendMessage(message);
		return true;
	}
}