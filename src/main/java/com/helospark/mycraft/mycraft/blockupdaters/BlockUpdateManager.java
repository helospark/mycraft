package com.helospark.mycraft.mycraft.blockupdaters;

import java.util.PriorityQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.helospark.mycraft.mycraft.game.Block;
import com.helospark.mycraft.mycraft.mathutils.IntVector;
import com.helospark.mycraft.mycraft.window.MessageHandler;

@Service
public class BlockUpdateManager {

	class TimeCallbackBlock implements Comparable<TimeCallbackBlock> {
		private IntVector position;
		private float timeout;
		private Block block;
		private long insertedTime;
		private BlockTimeUpdater blockTimeUpdater;

		public TimeCallbackBlock(IntVector position, float timeout, Block block) {
			this.position = position;
			this.timeout = timeout;
			this.block = block;
			this.insertedTime = System.currentTimeMillis();
			blockTimeUpdater = block.getBlockTimeUpdater();
		}

		public IntVector getPosition() {
			return position;
		}

		public void setPosition(IntVector position) {
			this.position = position;
		}

		public float getTimeout() {
			return timeout;
		}

		public void setTimeout(float timeout) {
			this.timeout = timeout;
		}

		public Block getBlock() {
			return block;
		}

		public void setBlock(Block block) {
			this.block = block;
		}

		public long getInsertedTime() {
			return insertedTime;
		}

		public BlockTimeUpdater getBlockTimeUpdater() {
			return blockTimeUpdater;
		}

		@Override
		public int compareTo(TimeCallbackBlock other) {
			if (this.timeout > other.timeout) {
				return 1;
			} else if (this.timeout < other.timeout) {
				return -1;
			}
			return 0;
		}

		public void reinitTimer(float nextTime) {
			this.timeout = nextTime;
			insertedTime = System.currentTimeMillis();
		}
	}

	private PriorityQueue<TimeCallbackBlock> timeCallbacks = new PriorityQueue<TimeCallbackBlock>();
	@Autowired
	private MessageHandler messager;

	public BlockUpdateManager() {

	}

	public synchronized void registerTimeUpdatedBlock(IntVector position, Block block, float timeout) {
		if (block.getBlockTimeUpdater() != null) {
			timeCallbacks.add(new TimeCallbackBlock(position, timeout, block));
		}
	}

	public void update(double deltaTime) {
		while (true) {
			TimeCallbackBlock timeCallableBlock = timeCallbacks.peek();
			long currentTime = System.currentTimeMillis();
			if (timeCallableBlock != null
					&& (currentTime - timeCallableBlock.insertedTime) / 1000.0f > timeCallableBlock.timeout) {
				timeCallbacks.remove();
				BlockTimeUpdater blockTimeUpdater = timeCallableBlock.getBlockTimeUpdater();
				if (blockTimeUpdater != null) {
					float lastUpdated = (currentTime - timeCallableBlock.insertedTime) / 1000.0f;
					float nextTime = blockTimeUpdater.onUpdate(timeCallableBlock.getPosition(),
							timeCallableBlock.getBlock(), lastUpdated);
					if (nextTime >= 0.0f) {
						timeCallableBlock.reinitTimer(nextTime);
						timeCallbacks.add(timeCallableBlock);
					}
				}
			} else {
				break;
			}
		}
	}
}
