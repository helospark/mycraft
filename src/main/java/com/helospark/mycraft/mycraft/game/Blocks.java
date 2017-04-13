package com.helospark.mycraft.mycraft.game;

import java.util.HashMap;
import java.util.Map;

public class Blocks {
	private static Map<String, Integer> blockTypes = new HashMap<>();
	private static Map<Integer, Block> blockFromId = new HashMap<>();

	public static void addBlock(int id, String name, Block loadedBlock) {
		blockFromId.put(id, loadedBlock);
		blockTypes.put(name, id);
	}

	public static int get(String id) {
		return blockTypes.get(id);
	}

	public static Block getBlockForId(int blockId) {
		return blockFromId.get(blockId);
	}
}
