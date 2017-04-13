package com.helospark.mycraft.mycraft.services;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;
import org.springframework.context.ApplicationContext;

import com.helospark.mycraft.mycraft.messages.ConnectToServerMessage;
import com.helospark.mycraft.mycraft.messages.NetworkMessage;
import com.helospark.mycraft.mycraft.messages.UserConnectionResultMessage;
import com.helospark.mycraft.mycraft.network.Network;
import com.helospark.mycraft.mycraft.network.NetworkMessagesList;
import com.helospark.mycraft.mycraft.network.NetworkUser;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageHandler;
import com.helospark.mycraft.mycraft.window.MessageListener;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public class ServerService implements MessageListener {
	private static final int ID_RANGE_PER_USER = 1000000;
	public static final int SERVER_PORT = 8889;
	private static final int MAX_PLAYERS = 10;
	private boolean running = true;
	private List<NetworkUser> networkUsers = new ArrayList<>();
	private Network network;
	MessageHandler messager;
	NetworkMessagesList messageList;

	public ServerService(Network network) {
		this.network = network;
		ApplicationContext context = Singleton.getInstance().getContext();
		messager = context.getBean(MessageHandler.class);
		messager.registerListener(this, MessageTypes.NETWORK_MESSAGE_RECEIVED);
		messager.registerListener(this,
				MessageTypes.SERVER_CONNECTION_REQUEST_MESSAGE);
	}

	public void setIp(String ip) {

	}

	public void setPort(int port) {

	}

	private boolean canBeProcessedOnServer(NetworkMessage message) {
		if (message.getMessage().getType() == MessageTypes.SERVER_CONNECTION_REQUEST_MESSAGE) {
			ConnectToServerMessage connectToServerMessage = (ConnectToServerMessage) message
					.getMessage();
			int lowerIdRange = networkUsers.size() * ID_RANGE_PER_USER;
			int upperIdRange = (networkUsers.size() + 1) * ID_RANGE_PER_USER;
			int seed = 0;
			Vector3f initialPosition = new Vector3f(-5, 10, 10);
			UserConnectionResultMessage userConnectionMessage = new UserConnectionResultMessage(
					MessageTypes.USER_CONNECTION_RESULT_MESSAGE,
					initialPosition, lowerIdRange, upperIdRange, seed);
			networkUsers.add(new NetworkUser(message.getAddress(), message
					.getPort()));
			System.out.println("Added new user: " + message.getAddress() + ":"
					+ message.getPort());
			NetworkUser user = findUserByAddress(message.getAddress());
			network.sendMessageToUser(user, userConnectionMessage);
			return true;
		}

		return false;
	}

	private NetworkUser findUserByAddress(InetAddress address) {
		String searchedAddressString = addressToString(address);
		for (int i = 0; i < networkUsers.size(); ++i) {
			String addressString = addressToString(
					networkUsers.get(i).getAddress()).toString();
			if (addressString.equals(searchedAddressString)) {
				return networkUsers.get(i);
			}
		}
		return null;
	}

	private String addressToString(InetAddress address) {
		return address.getHostAddress().toString();
	}

	@Override
	public boolean receiveMessage(Message message) {
		if (message.getType() == MessageTypes.NETWORK_MESSAGE_RECEIVED) {
			NetworkMessage networkMessage = (NetworkMessage) message;

			if (!canBeProcessedOnServer(networkMessage)) {
				for (int i = 0; i < networkUsers.size(); ++i) {
					if (isUserDifferent(networkMessage, i)) {
						network.sendMessageToUser(networkUsers.get(i),
								networkMessage.getMessage());
					}
				}
			}
			messager.sendMessage(networkMessage.getMessage());
		}
		return false;
	}

	private boolean isUserDifferent(NetworkMessage networkMessage, int i) {
		return !(networkMessage
				.getAddress()
				.getHostAddress()
				.toString()
				.equals(networkUsers.get(i).getAddress().getHostAddress()
						.toString()) && networkMessage.getPort() == networkUsers
				.get(i).getPort());
	}
}
