package com.helospark.mycraft.mycraft.network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.context.ApplicationContext;

import com.helospark.mycraft.mycraft.helpers.ArrayHelper;
import com.helospark.mycraft.mycraft.helpers.SerializationHelpers;
import com.helospark.mycraft.mycraft.messages.NetworkMessage;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageHandler;
import com.helospark.mycraft.mycraft.window.MessageListener;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public class Network implements MessageListener {
	private static final int MAX_MESSAGE_SIZE_IN_BYTES = 10000;
	protected static final int NON_EXISTING_ORDINAL = -1;
	private MessageHandler messager;

	private Thread sendThread;
	private Thread receiveThread;

	private Map<MessageTypes, Message> typeToMessage;
	private BlockingQueue<NetworkMessage> messagesToSend = new LinkedBlockingQueue<NetworkMessage>();

	private DatagramSocket socket;
	private volatile boolean running = true;

	// ServerData serverData;

	public Network(int port) {
		initializeCommon();
		initializeSocketFromPort(port);
	}

	public Network() {
		initializeCommon();
		initializeSocketAtRandomPort();
	}

	public void initialize() {
		startReceivingMessages();
		startSendingThread();
	}

	private void initializeSocketAtRandomPort() {
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			throw new RuntimeException("Unable to initialize socket");
		}
	}

	private void initializeSocketFromPort(int port) {
		try {
			socket = new DatagramSocket(port);
			socket.setReuseAddress(true);
		} catch (SocketException e) {
			throw new RuntimeException("Unable to open datagram socket");
		}
	}

	private void initializeCommon() {
		ApplicationContext context = Singleton.getInstance().getContext();
		typeToMessage = context.getBean(NetworkMessagesList.class).getTypeToMessage();
		messager = context.getBean(MessageHandler.class);
		messager.registerListener(this, MessageTypes.ESCAPE_REQUESTED);
	}

	public void sendMessageToUser(final NetworkUser user, final Message messageToSend) {
		messagesToSend.add(new NetworkMessage(messageToSend, user.getAddress(), user.getPort()));
	}

	private void startSendingThread() {
		Thread thread = new Thread(new Runnable() {
			byte[] messageBytes = new byte[MAX_MESSAGE_SIZE_IN_BYTES];

			@Override
			public void run() {
				while (running) {
					NetworkMessage messageToSend;
					try {
						messageToSend = messagesToSend.take();
					} catch (InterruptedException e) {
						throw new RuntimeException("Interrupted");
					}
					sendSingleMessage(messageToSend.getAddress(), messageToSend.getPort(),
							messageToSend.getMessage());
				}
			}

			private void sendSingleMessage(InetAddress address, int port, Message messageToSend) {

				if (messageToSend.getType() == MessageTypes.BLOCK_DESTROYED_MESSAGE) {
					System.out.println("STOP");
				}
				String serializedMessage = "";
				serializedMessage = messageToSend.serializeToString();
				byte[] data = serializedMessage.getBytes();
				int size = data.length;

				byte[] sizeArray = SerializationHelpers.getByteArray(size);
				byte[] messageOrdinalArray = SerializationHelpers.getByteArray(messageToSend
						.getType().ordinal());
				// System.out.println("SENDING: " + size + " "
				// + (messageToSend.getType().ordinal()));

				int sizeInBytes = ArrayHelper.mergeArrays(messageBytes, sizeArray,
						messageOrdinalArray, data);
				DatagramPacket packet = new DatagramPacket(messageBytes, 0, sizeInBytes, address,
						port);
				try {
					socket.send(packet);
				} catch (Exception e) {
					throw new RuntimeException("Unable to send message");
				}
			}
		});

		thread.start();
	}

	public void startReceivingMessages() {
		Thread listenThread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (running) {
					byte[] messageBytes = new byte[MAX_MESSAGE_SIZE_IN_BYTES];
					byte[] sizeBytes = new byte[Integer.BYTES];
					// System.out.println("Try to receive");

					DatagramPacket intPacket = new DatagramPacket(sizeBytes, sizeBytes.length);
					DatagramPacket messagePacket = null;
					int ordinal;
					int size;
					messagePacket = new DatagramPacket(messageBytes, messageBytes.length);

					try {

						socket.receive(messagePacket);

						for (int i = 0, j = 0; i < 4; ++i, ++j) {
							sizeBytes[j] = messageBytes[i];
						}
						size = SerializationHelpers.getInt(sizeBytes);

						for (int i = 4, j = 0; i < 8; ++i, ++j) {
							sizeBytes[j] = messageBytes[i];
						}
						ordinal = SerializationHelpers.getInt(sizeBytes);

						if (ordinal >= 0 && ordinal < MessageTypes.values().length
								&& messagePacket != null) {
							MessageTypes receivedMessage = MessageTypes.values()[ordinal];
							String messageString = new String(messageBytes, 8, size);
							String[] splittedString = messageString.split(";");
							Message message = typeToMessage.get(receivedMessage);
							if (message != null) {
								Message newMessage = message.deserializeFromString(splittedString,
										receivedMessage);
								InetAddress address = messagePacket.getAddress();
								int port = messagePacket.getPort();

								if (receivedMessage != MessageTypes.ACTOR_ROTATION_CHANGED
										&& receivedMessage != MessageTypes.ACTOR_POSITION_CHANGED) {
									System.out.println("RECIEVED MESSAGE: "
											+ address.getHostAddress().toString() + " "
											+ receivedMessage.toString());
								}
								if (receivedMessage == MessageTypes.BLOCK_DESTROYED_MESSAGE) {
									System.out.println("STOP");
								}

								Message createdMessage = new NetworkMessage(newMessage, address,
										port);
								messager.sendImmediateMessage(createdMessage);
							} else {
								System.out.println("NULL MESSAGE " + receivedMessage);
							}
						}
					} catch (Exception e) {
						throw new RuntimeException("Unable to receive message");
					}
				}
			}
		}, "ListenThread");
		listenThread.start();
	}

	private void clearSocket() {
		throw new RuntimeException("Make this");
	}

	@Override
	public boolean receiveMessage(Message message) {
		if (message.getType() == MessageTypes.ESCAPE_REQUESTED) {
			running = false;
		}
		return false;
	}
}
