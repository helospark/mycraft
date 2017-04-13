package com.helospark.mycraft.mycraft.window;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.springframework.stereotype.Service;

import com.helospark.mycraft.mycraft.window.DelayedMessage.DelayedMessageComparator;

@Service
public final class MessageHandler {
	private Map<MessageTypes, List<MessageListener>> listeners = new HashMap<>();
	private List<Message> messages = new LinkedList<Message>();
	// TODO: might order in the wrong direction
	private PriorityQueue<DelayedMessage> delayedMessages = new PriorityQueue<DelayedMessage>(100,
			new DelayedMessageComparator());

	public MessageHandler() {

	}

	public void registerListener(MessageListener listener, MessageTypes messageType) {
		if (!listeners.containsKey(messageType)) {
			this.listeners.put(messageType, new ArrayList<MessageListener>());
		}
		listeners.get(messageType).add(listener);
	}

	public synchronized void sendMessage(Message message) {
		this.messages.add(message);
	}

	public void sendDelayedMessage(Message message, int delayInMilliseconds) {
		delayedMessages.add(new DelayedMessage(message, System.currentTimeMillis()
				+ delayInMilliseconds));
	}

	public synchronized void sendImmediateMessage(Message message) {
		distributeSingleMessage(message);
	}

	public void distributeMessages() {
		distributeRegularMessages();
		distributeDelayedMessages();
	}

	private void distributeDelayedMessages() {
		long currentTime = System.currentTimeMillis();
		int lastIndex = delayedMessages.size();

		for (int i = 0; i < lastIndex; ++i) {
			if (delayedMessages.peek().shouldDeliverMessage(currentTime)) {
				DelayedMessage message = delayedMessages.poll();
				distributeSingleMessage(message.getMessage());
			} else {
				break;
			}
		}
	}

	private void distributeRegularMessages() {
		int lastIndex = messages.size();
		for (int i = 0; i < lastIndex; i++) {
			if (messages.size() == 0)
				break;
			Message message = messages.get(0);
			distributeSingleMessage(message);
			messages.remove(0);
		}
	}

	private void distributeSingleMessage(Message message) {
		List<MessageListener> listeners = this.listeners.get(message.getType());
		if (listeners == null) {
			return; // no message listener for this type of message
		}
		for (int i = 0; i < listeners.size(); ++i) {
			listeners.get(i).receiveMessage(message);
		}
	}

	public void removeListener(MessageListener listener, MessageTypes tankUpPressed) {
		// TODO: create this
	}
}
