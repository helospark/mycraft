package com.helospark.mycraft.mycraft.blockupdaters;

import com.helospark.mycraft.mycraft.game.Block;
import com.helospark.mycraft.mycraft.mathutils.IntVector;

public abstract class BlockTimeUpdater {

	public abstract float onUpdate(IntVector position, Block block, float lastUpdated);

	public abstract float getDefaultTime();

}
