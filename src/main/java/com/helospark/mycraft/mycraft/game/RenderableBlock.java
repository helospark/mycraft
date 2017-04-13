package com.helospark.mycraft.mycraft.game;

import com.helospark.mycraft.mycraft.mathutils.IntVector;

public class RenderableBlock {
	public static final IntVector leftVector = new IntVector(-1, 0, 0);
	public static final IntVector rightVector = new IntVector(1, 0, 0);
	public static final IntVector topVector = new IntVector(0, 1, 0);
	public static final IntVector bottomVector = new IntVector(0, -1, 0);
	public static final IntVector farVector = new IntVector(0, 0, 1);
	public static final IntVector nearVector = new IntVector(0, 0, -1);
	public static final IntVector[] sides;

	public int id;

	static {
		sides = new IntVector[Block.NUM_SIDES];
		sides[Block.LEFT_SIDE] = RenderableBlock.leftVector;
		sides[Block.RIGHT_SIDE] = RenderableBlock.rightVector;
		sides[Block.TOP_SIDE] = RenderableBlock.topVector;
		sides[Block.BOTTOM_SIDE] = RenderableBlock.bottomVector;
		sides[Block.NEAR_SIDE] = RenderableBlock.nearVector;
		sides[Block.FAR_SIDE] = RenderableBlock.farVector;
	}

	public RenderableBlock(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public static IntVector intVectorFromSide(int side) {
		return sides[side];
	}

	public static int getIdFromSide(IntVector side) {
		for (int i = 0; i < sides.length; ++i) {
			if (sides[i].equals(side)) {
				return i;
			}
		}
		return -1;
	}
}
