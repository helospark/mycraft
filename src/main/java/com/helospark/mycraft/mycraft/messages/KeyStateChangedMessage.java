package com.helospark.mycraft.mycraft.messages;

import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public class KeyStateChangedMessage extends Message {

	public static final int KEY_PRESSED = 0;
	public static final int KEY_RELEASED = 1;
	public static final int KEY_TYPED = 2;

	int keyCode;
	char keyCharacter;
	int stateChangeType;

	public KeyStateChangedMessage(MessageTypes type, int targetId, int keyCode,
			int stateChangeType, char keyCharacter) {
		super(type, targetId);
		this.keyCode = keyCode;
		this.stateChangeType = stateChangeType;
		this.keyCharacter = keyCharacter;
	}

	public char getKeyCharacter() {
		return keyCharacter;
	}

	public int getKeyCode() {
		return keyCode;
	}

	public int getKeyStateChangeType() {
		return stateChangeType;
	}

	@Override
	public String serializeToString() {
		String result = keyCode + ";" + keyCharacter + ";" + stateChangeType;
		return result;
	}

	@Override
	public Message deserializeFromString(String[] data, MessageTypes messageType) {
		int keyCode = Integer.parseInt(data[0]);
		char keyCharacter = data[1].charAt(0);
		int stateChangeType = Integer.parseInt(data[2]);

		KeyStateChangedMessage result = new KeyStateChangedMessage(messageType,
				MESSAGE_TARGET_ANYONE, keyCode, stateChangeType, keyCharacter);
		return result;
	}
}
