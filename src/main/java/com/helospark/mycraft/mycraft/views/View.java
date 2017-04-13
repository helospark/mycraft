package com.helospark.mycraft.mycraft.views;

import org.springframework.context.ApplicationContext;

import com.helospark.mycraft.mycraft.messages.GenericIntMessage;
import com.helospark.mycraft.mycraft.render.SpriteAndTextBatchData;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageHandler;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public abstract class View {
	protected int id;
	protected MessageHandler messager;

	public View(int id) {
		this.id = id;
		ApplicationContext context = Singleton.getInstance().getContext();
		messager = context.getBean(MessageHandler.class);
	}

	public Integer getId() {
		return id;
	}

	public abstract void fillBuffers(SpriteAndTextBatchData spriteTextBatchData);

	protected void addView() {
		messager.sendMessage(new GenericIntMessage(MessageTypes.SHOW_VIEW,
				Message.MESSAGE_TARGET_ANYONE, id));
	}

	protected void removeView() {
		messager.sendMessage(new GenericIntMessage(MessageTypes.REMOVE_VIEW,
				Message.MESSAGE_TARGET_ANYONE, id));
	}
}
