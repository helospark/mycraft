package com.helospark.mycraft.mycraft.itemrightclickhandlers;

import com.helospark.mycraft.mycraft.game.Block;
import com.helospark.mycraft.mycraft.mathutils.IntVector;

public abstract class GameItemRightClickHandler {

	public abstract boolean applyEffect(Block activeBlock, IntVector position,
			int blockId, IntVector sideVector);
}
