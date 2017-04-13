package com.helospark.mycraft.mycraft.itemrightclickhandlers;

import org.springframework.context.ApplicationContext;

import com.helospark.mycraft.mycraft.game.Block;
import com.helospark.mycraft.mycraft.game.Blocks;
import com.helospark.mycraft.mycraft.mathutils.IntVector;
import com.helospark.mycraft.mycraft.messages.NewBlockMessage;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageHandler;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public class StonePlacerBlockHandler extends GameItemRightClickHandler {

	MessageHandler messager;

	public StonePlacerBlockHandler() {
		ApplicationContext context = Singleton.getInstance().getContext();
		messager = context.getBean(MessageHandler.class);
	}

	@Override
	public boolean applyEffect(Block activeBlock, IntVector position,
			int blockId, IntVector sideVector) {
		Message message = new NewBlockMessage(MessageTypes.NEW_BLOCK_MESSAGE,
				Message.MESSAGE_TARGET_ANYONE, position.add(sideVector),
				Blocks.get("Stone"));
		messager.sendMessage(message);
		return false;
	}
}