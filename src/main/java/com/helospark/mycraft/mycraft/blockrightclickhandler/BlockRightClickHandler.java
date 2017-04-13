package com.helospark.mycraft.mycraft.blockrightclickhandler;

import com.helospark.mycraft.mycraft.game.Block;
import com.helospark.mycraft.mycraft.mathutils.IntVector;

public abstract class BlockRightClickHandler {
	public abstract boolean onRightClick(Block clickedBlock,
			IntVector position, int itemId);
}
