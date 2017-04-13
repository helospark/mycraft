package com.helospark.mycraft.mycraft.helpers;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SerializationHelpers {

	private static final int BITS_PER_BYTE = 8;
	// cache
	private static byte[] intData = new byte[Integer.BYTES];

	public static int serializeIntegerIntoArrayAtPosition(byte[] result, int resultIndex, int data) {

		result[resultIndex + 0] = (byte) (data >> 24);
		result[resultIndex + 1] = (byte) (data >> 16);
		result[resultIndex + 2] = (byte) (data >> 8);
		result[resultIndex + 3] = (byte) (data >> 0);
		return Integer.BYTES;
	}

	public static int deserializeIntegerFromArray(byte[] data, int resultIndex) {
		return data[resultIndex + 0] << 24 | (data[resultIndex + 1] & 0xFF) << 16
				| (data[resultIndex + 2] & 0xFF) << 8 | (data[resultIndex + 3] & 0xFF);
	}

	public static int getInt(byte[] byteBarray) {
		return ByteBuffer.wrap(byteBarray).order(ByteOrder.BIG_ENDIAN).getInt();
	}

	public static byte[] getByteArray(int myInteger) {
		return ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(myInteger).array();
	}
}
