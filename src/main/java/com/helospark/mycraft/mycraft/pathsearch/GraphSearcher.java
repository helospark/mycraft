package com.helospark.mycraft.mycraft.pathsearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import com.helospark.mycraft.mycraft.game.Blocks;
import com.helospark.mycraft.mycraft.mathutils.IntVector;
import com.helospark.mycraft.mycraft.mathutils.VectorMathUtils;

public class GraphSearcher implements Callable<List<IntVector>> {

	private List<Node> allNodes = new ArrayList<Node>();
	IntVector centerPoint;
	IntVector startPosition, endPosition;
	int[][][] mapCopy;

	// cache
	int airBlockId = Blocks.get("Air");

	public GraphSearcher(int[][][] mapCopy) {
		this.mapCopy = mapCopy;
	}

	public List<IntVector> getShortestPath(Node startPoint, Node endPoint) {
		NodeComparator comparator = new NodeComparator();
		List<Node> smallestNodes = new ArrayList<Node>();
		List<IntVector> resultIntVectorList = new ArrayList<IntVector>();
		for (Node node : allNodes) {
			node.setDistance(Integer.MAX_VALUE);
			node.setPrevious(null);
			smallestNodes.add(node);
		}
		startPoint.setDistance(0);
		startPoint.setPrevious(null);

		while (!smallestNodes.isEmpty()) {
			Collections.sort(smallestNodes, comparator);
			Node current = smallestNodes.get(0);
			smallestNodes.remove(current);

			for (Node neighbour : current.getNeighbours()) {
				float newDistance = current.getDistance()
						+ VectorMathUtils.distanceSquareBetween(current.getPosition(),
								neighbour.getPosition());
				if (neighbour.getDistance() > newDistance) {
					neighbour.setDistance(newDistance);
					neighbour.setPrevious(current);
				}
			}
		}

		Node currentNode = endPoint;

		while (currentNode.getPrevious() != null) {
			resultIntVectorList.add(currentNode.getPosition());
			currentNode = currentNode.getPrevious();
		}
		resultIntVectorList.add(currentNode.getPosition());
		Collections.reverse(resultIntVectorList);

		return resultIntVectorList;
	}

	@Override
	public List<IntVector> call() throws Exception {
		generateGraph();
		Node startNode = getNodeAndAddToAllNodesAtPosition(startPosition);
		Node endNode = getNodeAndAddToAllNodesAtPosition(endPosition);

		List<IntVector> result = getShortestPath(startNode, endNode);
		return result;
	}

	private void generateGraph() {
		for (int x = 0; x < mapCopy.length; ++x) {
			for (int y = 0; y < mapCopy[x].length; ++y) {
				for (int z = 0; z < mapCopy[x][y].length; ++z) {
					try {
						processNodeIfNeeded(x, y, z);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		System.out.println("FINISHED GRAPH BUILDING");
	}

	private void processNodeIfNeeded(int x, int y, int z) {
		if (canNodeBeSteppedOn(x, y, z)) {
			Node node = getNodeAndAddToAllNodesAtPosition(getAbsolutePosition(x, y, z));
			for (int dx = -1; dx <= 1; ++dx) {
				for (int dy = -1; dy <= 3; ++dy) {
					for (int dz = -1; dz <= 1; ++dz) {
						if (canNodeBeSteppedOn(x + dx, y + dy, z + dz)
								&& (dx != 0 || dy != 0 || dz != 0)) {
							Node neightbourNode = getNodeAndAddToAllNodesAtPosition(getAbsolutePosition(
									x + dx, y + dy, z + dz));
							node.addNeighbour(neightbourNode);
						}
					}
				}
			}
		}
	}

	private Node getNodeAndAddToAllNodesAtPosition(IntVector position) {
		Node result = null;

		for (int i = 0; i < allNodes.size(); ++i) {
			if (allNodes.get(i).equals(position)) {
				result = allNodes.get(i);
				break;
			}
		}

		if (result == null) {
			result = new Node(position);
			allNodes.add(result);
		}

		return result;
	}

	private IntVector getAbsolutePosition(int i, int j, int k) {
		int size = mapCopy.length / 2;
		IntVector result = new IntVector();
		result.x = i - size + centerPoint.x;
		result.y = j - size + centerPoint.y;
		result.z = k - size + centerPoint.z;
		return result;
	}

	private boolean canNodeBeSteppedOn(int x, int y, int z) {
		return getIdAt(x, y, z) == airBlockId && getIdAt(x, y - 1, z) != airBlockId;
	}

	private int getIdAt(int i, int j, int k) {
		if (j == 1) {
			// System.out.println("STOP");
		}
		if (i < 0 || i >= mapCopy.length) {
			return airBlockId;
		}
		if (j < 0 || j >= mapCopy[i].length) {
			return airBlockId;
		}
		if (k < 0 || k >= mapCopy[i][j].length) {
			return airBlockId;
		}
		return mapCopy[i][j][k];
	}

	public void setCenterPoint(IntVector position) {
		this.centerPoint = position;
	}

	public void shortestPathBetween(IntVector position1, IntVector position2) {
		this.startPosition = position1;
		this.endPosition = position2;
	}
}
