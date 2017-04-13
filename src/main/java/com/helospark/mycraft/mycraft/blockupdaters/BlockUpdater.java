package com.helospark.mycraft.mycraft.blockupdaters;

import com.helospark.mycraft.mycraft.game.Block;
import com.helospark.mycraft.mycraft.mathutils.IntVector;

public abstract class BlockUpdater {
	public abstract boolean onBlockUpdate(IntVector position, Block type, float lastUpdated);
}
