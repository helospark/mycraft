package com.helospark.mycraft.mycraft.services;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.helospark.mycraft.mycraft.actor.MyIdGenerator;
import com.helospark.mycraft.mycraft.messages.NetworkMessage;
import com.helospark.mycraft.mycraft.network.Network;
import com.helospark.mycraft.mycraft.network.NetworkMessagesList;
import com.helospark.mycraft.mycraft.network.NetworkUser;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageHandler;
import com.helospark.mycraft.mycraft.window.MessageListener;
import com.helospark.mycraft.mycraft.window.MessageTypes;

@Service
public class ClientService implements MessageListener, Runnable {
	private static final int NETWORK_SOURCE_ID = 1;
	BlockingQueue<Message> messages = new LinkedBlockingQueue<>();
	volatile boolean running = true;
	MessageHandler messager;
	Network network;
	NetworkUser server;
	Map<MessageTypes, Message> typeToMessage;
	Thread thread = new Thread(this);
	private boolean isClientAndServerSame = false;
	@Autowired
	MyIdGenerator idGenerator;

	public ClientService() {
		ApplicationContext context = Singleton.getInstance().getContext();
		messager = context.getBean(MessageHandler.class);
		typeToMessage = context.getBean(NetworkMessagesList.class)
				.getTypeToMessage();
		messager.registerListener(this, MessageTypes.NETWORK_MESSAGE_RECEIVED);
	}

	public void setServerUser(NetworkUser server) {
		this.server = server;
	}

	public void initialize() {
		registerToMessages();
	}

	public void registerToMessages() {

		for (Map.Entry<MessageTypes, Message> entry : typeToMessage.entrySet()) {
			MessageTypes messageToRegister = entry.getKey();
			messager.registerListener(this, messageToRegister);
		}
	}

	public void start() {
		thread.start();
	}

	@Override
	public boolean receiveMessage(Message message) {

		if (message.getSource() == NETWORK_SOURCE_ID) {
			return false;
		}

		if (message.getType() == MessageTypes.NETWORK_MESSAGE_RECEIVED) {
			NetworkMessage networkMessage = (NetworkMessage) message;
			networkMessage.getMessage().setSource(NETWORK_SOURCE_ID);
			messager.sendMessage(networkMessage.getMessage());
			return false;
		}

		if (message.getType() != MessageTypes.USER_CONNECTION_RESULT_MESSAGE) {
			messages.offer(message);
		}
		return false;
	}

	@Override
	public void run() {
		while (running) {
			Message message;
			try {
				message = messages.take();
			} catch (InterruptedException e) {
				throw new RuntimeException("Interrupted exception");
			}
			if (message != null) {
				network.sendMessageToUser(server, message);
			}
		}
	}

	public void setNetwork(Network network) {
		this.network = network;
	}

	public void setClientAndServerSame(boolean isServer) {
		this.isClientAndServerSame = isServer;
	}
}
