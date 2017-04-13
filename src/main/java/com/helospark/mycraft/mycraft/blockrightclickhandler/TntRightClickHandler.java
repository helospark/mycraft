package com.helospark.mycraft.mycraft.blockrightclickhandler;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.helospark.mycraft.mycraft.game.Block;
import com.helospark.mycraft.mycraft.mathutils.IntVector;
import com.helospark.mycraft.mycraft.messages.ExplosionMessage;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageHandler;
import com.helospark.mycraft.mycraft.window.MessageTypes;

@Service
public class TntRightClickHandler extends BlockRightClickHandler {
	public static final int EXPLOSION_RADIUS = 4;
	private MessageHandler messager;

	public TntRightClickHandler() {
		ApplicationContext context = Singleton.getInstance().getContext();
		messager = context.getBean(MessageHandler.class);
	}

	@Override
	public boolean onRightClick(Block clickedBlock, IntVector position, int itemId) {
		ExplosionMessage message = new ExplosionMessage(MessageTypes.EXPLOSION_MESSAGE,
				Message.MESSAGE_TARGET_ANYONE, EXPLOSION_RADIUS, position);
		messager.sendDelayedMessage(message, 2000);
		return true;
	}
}
