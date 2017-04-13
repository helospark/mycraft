package com.helospark.mycraft.mycraft.services;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.helospark.mycraft.mycraft.blockrightclickhandler.TntRightClickHandler;
import com.helospark.mycraft.mycraft.game.Block;
import com.helospark.mycraft.mycraft.game.Blocks;
import com.helospark.mycraft.mycraft.game.GameMap;
import com.helospark.mycraft.mycraft.mathutils.BoundingBox;
import com.helospark.mycraft.mycraft.mathutils.IntVector;
import com.helospark.mycraft.mycraft.mathutils.VectorMathUtils;
import com.helospark.mycraft.mycraft.messages.BlockDestroyedMessage;
import com.helospark.mycraft.mycraft.messages.ExplosionMessage;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageHandler;
import com.helospark.mycraft.mycraft.window.MessageListener;
import com.helospark.mycraft.mycraft.window.MessageTypes;

@Service
public class ExplosionResolverService implements MessageListener {

	private MessageHandler messager;
	private Random random;

	@Autowired
	GameMap map;

	@Autowired
	TntRightClickHandler tnt;

	public ExplosionResolverService() {
		ApplicationContext context = Singleton.getInstance().getContext();
		messager = context.getBean(MessageHandler.class);
		messager.registerListener(this, MessageTypes.EXPLOSION_MESSAGE);
		random = new Random();
	}

	@Override
	public boolean receiveMessage(Message message) {
		if (message.getType() == MessageTypes.EXPLOSION_MESSAGE) {
			ExplosionMessage explosionMessage = (ExplosionMessage) message;
			int distance = explosionMessage.getRadius();
			int distanceSquared = distance * distance;
			IntVector position = explosionMessage.getPosition();
			for (int i = -distance; i <= distance; ++i) {
				for (int j = -distance; j <= distance; ++j) {
					for (int k = -distance; k <= distance; ++k) {
						IntVector blockPosition = new IntVector(position.x + i, position.y + j,
								position.z + k);
						int vectorLengthSquared = (int) VectorMathUtils.distanceSquareBetween(
								blockPosition, position);
						if (vectorLengthSquared <= distanceSquared) {
							float value;
							if (vectorLengthSquared > 0) {
								value = (float) Math.min(1.0f,
										1.0f - (distanceSquared / vectorLengthSquared) + 0.5);
							} else {
								value = 1.0f;
							}
							// if (random.nextFloat() < value)
							{
								Block block = map.getBlockAtPosition(blockPosition);
								if (block == null) {
									continue;
								}
								if (block.getType() == Blocks.get("Tnt")) {
									ExplosionMessage newExplosionMessage = new ExplosionMessage(
											MessageTypes.EXPLOSION_MESSAGE,
											Message.MESSAGE_TARGET_ANYONE,
											TntRightClickHandler.EXPLOSION_RADIUS, position);
									messager.sendMessage(newExplosionMessage);
								}
								if (block.getType() != Blocks.get("Air")) {
									BoundingBox boundingBox = block.getBoundingBox()
											.getBoundingBoxWithPosition(
													blockPosition.x * Block.SIZE,
													blockPosition.y * Block.SIZE,
													blockPosition.z * Block.SIZE);
									BlockDestroyedMessage blockDestroyedMessage = new BlockDestroyedMessage(
											MessageTypes.BLOCK_DESTROYED_MESSAGE,
											Message.MESSAGE_TARGET_ANYONE, blockPosition.x,
											blockPosition.y, blockPosition.z, boundingBox,
											random.nextFloat() < 0.3);
									blockDestroyedMessage.setBlockId(block.getType());
									messager.sendMessage(blockDestroyedMessage);
								}
							}
						}
					}
				}
			}
		}
		return false;
	}
}
