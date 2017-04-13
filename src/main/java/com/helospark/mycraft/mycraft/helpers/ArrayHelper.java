package com.helospark.mycraft.mycraft.helpers;

import java.util.List;

public class ArrayHelper {
	public static void copyToList(float[] array, List<Float> data) {
		for (int i = 0; i < array.length; ++i) {
			data.add(array[i]);
		}
	}

	public static void copyToList(int[] array, List<Integer> data) {
		for (int i = 0; i < array.length; ++i) {
			data.add(array[i]);
		}
	}

	public static int mergeArrays(byte[] result, byte[]... arrays) {

		checkArraySize(result, arrays);
		int index = doCopyArray(result, arrays);
		return index;
	}

	private static int doCopyArray(byte[] result, byte[]... arrays) {
		int index = 0;
		for (int i = 0; i < arrays.length; ++i) {
			for (int j = 0; j < arrays[i].length; ++j) {
				result[index++] = arrays[i][j];
			}
		}
		return index;
	}

	private static void checkArraySize(byte[] result, byte[]... arrays) {
		int size = 0;
		for (int i = 0; i < arrays.length; ++i) {
			size += arrays[i].length;
		}
		if (size > result.length) {
			throw new RuntimeException("Too small result array");
		}
	}
}
