package com.helospark.mycraft.mycraft.factories;

public class IDFactory {
	private static int id = 0;

	private IDFactory() {
	}

	public static int getNextId() {
		return ++id;
	}
}
