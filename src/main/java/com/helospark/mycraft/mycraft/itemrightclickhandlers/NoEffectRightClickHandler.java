package com.helospark.mycraft.mycraft.itemrightclickhandlers;

import org.springframework.stereotype.Service;

import com.helospark.mycraft.mycraft.game.Block;
import com.helospark.mycraft.mycraft.mathutils.IntVector;

@Service
public class NoEffectRightClickHandler extends GameItemRightClickHandler {
	@Override
	public boolean applyEffect(Block activeBlock, IntVector position,
			int blockId, IntVector sideVector) {
		return false;
	}

}
