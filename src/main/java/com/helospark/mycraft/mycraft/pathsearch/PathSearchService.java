package com.helospark.mycraft.mycraft.pathsearch;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.helospark.mycraft.mycraft.actor.MyIdGenerator;
import com.helospark.mycraft.mycraft.game.Block;
import com.helospark.mycraft.mycraft.game.Blocks;
import com.helospark.mycraft.mycraft.game.GameMap;
import com.helospark.mycraft.mycraft.mathutils.IntVector;
import com.helospark.mycraft.mycraft.message.PathFindingResultMessage;
import com.helospark.mycraft.mycraft.services.MyExecutorService;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageHandler;
import com.helospark.mycraft.mycraft.window.MessageTypes;

@Service
public class PathSearchService {
	private static final int MAX_WAITS_PER_TRY_IN_MILLISECOND = 5;
	private PriorityQueue<FutureTimedTask<List<IntVector>>> taskList = new PriorityQueue<FutureTimedTask<List<IntVector>>>();

	@Autowired
	private MyExecutorService executorService;
	@Autowired
	private GameMap gameMap;
	@Autowired
	private MessageHandler messager;

	@Autowired
	MyIdGenerator idGenerator;

	// cache
	private IntVector newPosition = new IntVector();
	List<FutureTimedTask<List<IntVector>>> elementsToRemove = new ArrayList<>();

	public int requestShortestPathFinding(IntVector position1, IntVector position2,
			int maxDistance, float timeForResult) {
		int taskId = idGenerator.getNextId();
		int[][][] mapCopy = generateCopyFromMap(position1, maxDistance);
		GraphSearcher graph = new GraphSearcher(mapCopy);
		graph.setCenterPoint(position1);
		graph.shortestPathBetween(position1, position2);
		// TODO: add priority low for these tasks!
		Future<List<IntVector>> graphFuture = executorService.getExecutorService().submit(graph);
		FutureTimedTask<List<IntVector>> futureTimedTask = new FutureTimedTask<List<IntVector>>(
				taskId, graphFuture, timeForResult);
		taskList.add(futureTimedTask);
		return taskId;
	}

	private int[][][] generateCopyFromMap(IntVector position1, int maxDistance) {
		int[][][] mapCopy = new int[maxDistance * 2 + 1][maxDistance * 2 + 1][maxDistance * 2 + 1];

		for (int x = -maxDistance; x <= maxDistance; ++x) {
			for (int y = -maxDistance; y <= maxDistance; ++y) {
				for (int z = -maxDistance; z <= maxDistance; ++z) {
					newPosition.x = position1.x + x;
					newPosition.y = position1.y + y;
					newPosition.z = position1.z + z;
					Block block = gameMap.getBlockAtPosition(newPosition);
					int id = Blocks.get("Air");
					if (block != null) {
						id = block.getType();
					}
					mapCopy[x + maxDistance][y + maxDistance][z + maxDistance] = id;
				}
			}
		}

		// for (int z = 0; z < mapCopy[0][0].length; ++z) {
		// for (int y = 0; y < mapCopy.length; ++y) {
		// for (int x = 0; x < mapCopy[y].length; ++x) {
		// System.out.print(mapCopy[x][y][z] + " ");
		// }
		// System.out.println();
		// }
		// System.out.println("---------------------");
		// }

		return mapCopy;
	}

	public void update(double deltaTime) {
		elementsToRemove.clear();
		for (FutureTimedTask<List<IntVector>> iterator : taskList) {
			iterator.decreaseTime(deltaTime);
			if (iterator.getTime() < 0.0f) {
				List<IntVector> result = null;
				try {
					try {
						result = iterator.getTask().get(MAX_WAITS_PER_TRY_IN_MILLISECOND,
								TimeUnit.MILLISECONDS);
					} catch (InterruptedException | ExecutionException e) {
						elementsToRemove.add(iterator);
					}
				} catch (TimeoutException e) {
					// try it later
				}
				if (result != null) {
					messager.sendMessage(new PathFindingResultMessage(
							MessageTypes.PATH_FINDING_FINISHED, Message.MESSAGE_TARGET_ANYONE,
							iterator.getTaskId(), result));
					elementsToRemove.add(iterator);
				}
			}
		}
		for (int i = 0; i < elementsToRemove.size(); ++i) {
			taskList.remove(elementsToRemove.get(i));
		}
	}
}
