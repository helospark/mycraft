package com.helospark.mycraft.mycraft.itemrightclickhandlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.helospark.mycraft.mycraft.game.Block;
import com.helospark.mycraft.mycraft.mathutils.IntVector;
import com.helospark.mycraft.mycraft.messages.NewBlockMessage;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageHandler;
import com.helospark.mycraft.mycraft.window.MessageTypes;

@Service
public class RegularBlockPlacementRightClickHandler extends
		GameItemRightClickHandler {

	@Autowired
	private MessageHandler messager;

	public RegularBlockPlacementRightClickHandler() {
		ApplicationContext context = Singleton.getInstance().getContext();
		messager = context.getBean(MessageHandler.class);
	}

	@Override
	public boolean applyEffect(Block activeBlock, IntVector position,
			int blockId, IntVector sideVector) {
		Message message = new NewBlockMessage(MessageTypes.NEW_BLOCK_MESSAGE,
				Message.MESSAGE_TARGET_ANYONE, position.add(sideVector),
				blockId);
		messager.sendMessage(message);
		return true;
	}

	public void setMessager(MessageHandler messager) {
		this.messager = messager;
	}
}
