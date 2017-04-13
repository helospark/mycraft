package com.helospark.mycraft.mycraft.input;

import org.lwjgl.input.Keyboard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.helospark.mycraft.mycraft.actor.Actor;
import com.helospark.mycraft.mycraft.messages.GenericIntMessage;
import com.helospark.mycraft.mycraft.messages.GenericIntegerVectorMessage;
import com.helospark.mycraft.mycraft.messages.GenericMessage;
import com.helospark.mycraft.mycraft.messages.KeyStateChangedMessage;
import com.helospark.mycraft.mycraft.services.ActorSearchService;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageHandler;
import com.helospark.mycraft.mycraft.window.MessageListener;
import com.helospark.mycraft.mycraft.window.MessageTypes;

@Service
public class InputMapper implements MessageListener {
	MessageHandler messager;
	ActorSearchService actorSearchService;

	@Autowired
	public InputMapper(MessageHandler messager, ActorSearchService actorSearchService) {
		this.messager = messager;
		this.actorSearchService = actorSearchService;
		registerForMessages();
	}

	private void registerForMessages() {
		messager.registerListener(this, MessageTypes.KEY_STATE_CHANGE_MESSAGE);
	}

	@Override
	public boolean receiveMessage(Message message) {
		if (message.getType() == MessageTypes.KEY_STATE_CHANGE_MESSAGE) {
			KeyStateChangedMessage keyMessage = (KeyStateChangedMessage) message;
			if (keyMessage.getKeyStateChangeType() == KeyStateChangedMessage.KEY_RELEASED) {
				if (keyMessage.getKeyCode() == Keyboard.KEY_I) {
					Actor localActor = actorSearchService.getLocalHumanPlayer();
					messager.sendMessage(new GenericIntMessage(
							MessageTypes.TOGGLE_INVENTORY_MESSAGE, Message.MESSAGE_TARGET_ANYONE,
							localActor.getId()));
				}
				if (keyMessage.getKeyCode() == Keyboard.KEY_ESCAPE) {
					messager.sendMessage(new GenericMessage(MessageTypes.CLOSE_ALL_OPTIONAL_VIEWS,
							Message.MESSAGE_TARGET_ANYONE));
				}
				if (keyMessage.getKeyCode() == Keyboard.KEY_UP) {
					messager.sendMessage(new GenericIntegerVectorMessage(
							MessageTypes.UNLOAD_CHUNK_MESSAGE, Message.MESSAGE_TARGET_ANYONE, 0, 0,
							0));
				}
				if (keyMessage.getKeyCode() == Keyboard.KEY_DOWN) {
					messager.sendMessage(new GenericIntegerVectorMessage(
							MessageTypes.LOAD_CHUNK_MESSAGE, Message.MESSAGE_TARGET_ANYONE, 0, 0, 0));
				}
				if (keyMessage.getKeyCode() == Keyboard.KEY_F4) {
					messager.sendMessage(new GenericMessage(MessageTypes.ESCAPE_REQUESTED,
							Message.MESSAGE_TARGET_ANYONE));
				}
			}
		}
		return false;
	}
}
