package com.helospark.mycraft.mycraft.blockupdaters;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.helospark.mycraft.mycraft.game.Block;
import com.helospark.mycraft.mycraft.game.GameMap;
import com.helospark.mycraft.mycraft.mathutils.BoundingBox;
import com.helospark.mycraft.mycraft.mathutils.IntVector;
import com.helospark.mycraft.mycraft.messages.BlockDestroyedMessage;
import com.helospark.mycraft.mycraft.messages.GenericIntegerVectorMessage;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageHandler;
import com.helospark.mycraft.mycraft.window.MessageTypes;

@Service
public class TreeBlockTimeUpdater extends BlockTimeUpdater {

	public static final float CHANCE_OF_SKIPPING_TREE_GROWING = 0.5f;
	public static final float DEFAULT_BLOCK_TIME = 3.5f;
	public static final float NEXT_GROW_TIME_MINIMUM = 3.0f;
	public static final float NEXT_GROW_TIME_MAXIMUM = 7.0f;

	@Autowired
	private GameMap gameMap;
	@Autowired
	private MessageHandler messager;
	private Random random = new Random();

	@Override
	public float onUpdate(IntVector position, Block block, float lastUpdated) {
		System.out.println("Trying to grow");
		// if (random.nextFloat() < CHANCE_OF_SKIPPING_TREE_GROWING) {
		// return random.nextFloat() * (NEXT_GROW_TIME_MAXIMUM -
		// NEXT_GROW_TIME_MINIMUM)
		// + NEXT_GROW_TIME_MINIMUM;
		// }

		BoundingBox boundingBox = block.getBoundingBox().getBoundingBoxWithPosition(
				position.x * Block.SIZE, position.y * Block.SIZE, position.z * Block.SIZE);
		Message treeRemovalMessage = new BlockDestroyedMessage(
				MessageTypes.BLOCK_DESTROYED_MESSAGE, Message.MESSAGE_TARGET_ANYONE, position.x,
				position.y, position.z, boundingBox, false);

		messager.sendImmediateMessage(treeRemovalMessage);

		Message growTreeMessage = new GenericIntegerVectorMessage(MessageTypes.GROW_TREE,
				Message.MESSAGE_TARGET_ANYONE, position.x, position.y, position.z);
		messager.sendMessage(growTreeMessage);

		return -1.0f;
	}

	@Override
	public float getDefaultTime() {
		return DEFAULT_BLOCK_TIME;
	}
}
