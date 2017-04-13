package com.helospark.mycraft.mycraft.views;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.helospark.mycraft.mycraft.messages.GenericIntMessage;
import com.helospark.mycraft.mycraft.render.SpriteAndTextBatchData;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageHandler;
import com.helospark.mycraft.mycraft.window.MessageListener;
import com.helospark.mycraft.mycraft.window.MessageTypes;

@Service
public class ViewStack implements MessageListener {
	private Map<Integer, View> allViews = new HashMap<Integer, View>();
	private Map<Integer, View> activeViews = new HashMap<Integer, View>();
	private Map<String, Integer> viewNameToId = new HashMap<>();
	private MessageHandler messager;

	public ViewStack() {
		ApplicationContext context = Singleton.getInstance().getContext();
		messager = context.getBean(MessageHandler.class);
		messager.registerListener(this, MessageTypes.CLEAR_ALL_VIEWS);
		messager.registerListener(this, MessageTypes.SHOW_VIEW);
		messager.registerListener(this, MessageTypes.REMOVE_VIEW);
	}

	public void addView(int id, View view) {
		activeViews.put(id, view);
	}

	public void fillBuffers(SpriteAndTextBatchData spriteTextBatchData) {
		for (View view : activeViews.values()) {
			view.fillBuffers(spriteTextBatchData);
		}
	}

	@Override
	public boolean receiveMessage(Message message) {
		if (message.getType() == MessageTypes.SHOW_VIEW) {
			GenericIntMessage intMessage = (GenericIntMessage) message;
			int id = intMessage.getParameter();
			View view = allViews.get(id);
			addView(id, view);
		} else if (message.getType() == MessageTypes.REMOVE_VIEW) {
			GenericIntMessage intMessage = (GenericIntMessage) message;
			int id = intMessage.getParameter();
			activeViews.remove(id);
		} else if (message.getType() == MessageTypes.CLEAR_ALL_VIEWS) {
			activeViews.clear();
		}
		return false;
	}

	public void registerView(String name, View view) {
		allViews.put(view.getId(), view);
		viewNameToId.put(name, view.getId());
	}

	public int getViewFromName(String string) {
		Integer result = viewNameToId.get(string);
		if (result == null) {
			return -1;
		}
		return result;
	}
}
