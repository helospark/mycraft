package com.helospark.mycraft.mycraft.itemrightclickhandlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.helospark.mycraft.mycraft.actor.Actor;
import com.helospark.mycraft.mycraft.game.Block;
import com.helospark.mycraft.mycraft.game.Blocks;
import com.helospark.mycraft.mycraft.mathutils.BoundingBox;
import com.helospark.mycraft.mycraft.mathutils.IntVector;
import com.helospark.mycraft.mycraft.messages.BlockDestroyedMessage;
import com.helospark.mycraft.mycraft.messages.NewBlockMessage;
import com.helospark.mycraft.mycraft.services.ActorSearchService;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageHandler;
import com.helospark.mycraft.mycraft.window.MessageTypes;

@Service
public class HoeRightClickHandler extends GameItemRightClickHandler {

	@Autowired
	MessageHandler messager;

	@Autowired
	ActorSearchService actorSearchService;

	@Override
	public boolean applyEffect(Block activeBlock, IntVector position,
			int blockId, IntVector sideVector) {
		if (activeBlock.getType() == Blocks.get("Dirt")) {
			BoundingBox boundingBox = new BoundingBox(
					activeBlock.getBoundingBox());
			boundingBox.setPosition(position.x * Block.SIZE, position.y
					* Block.SIZE, position.z * Block.SIZE);

			Actor localPlayer = actorSearchService.getLocalHumanPlayer();

			messager.sendImmediateMessage(new BlockDestroyedMessage(
					MessageTypes.BLOCK_DESTROYED_MESSAGE,
					Message.MESSAGE_TARGET_ANYONE, position, blockId,
					localPlayer.getId(), boundingBox, false));

			messager.sendImmediateMessage(new NewBlockMessage(
					MessageTypes.NEW_BLOCK_MESSAGE,
					Message.MESSAGE_TARGET_ANYONE, position, Blocks
							.get("HoedDirt")));
		}
		return false;
	}
}
