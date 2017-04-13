package com.helospark.mycraft.mycraft.blockrightclickhandler;

import org.springframework.stereotype.Service;

import com.helospark.mycraft.mycraft.game.Block;
import com.helospark.mycraft.mycraft.mathutils.IntVector;

@Service
public class NoOperationRightClickHandler extends BlockRightClickHandler {

	@Override
	public boolean onRightClick(Block clickedBlock, IntVector position,
			int itemId) {
		return false;
	}

}
