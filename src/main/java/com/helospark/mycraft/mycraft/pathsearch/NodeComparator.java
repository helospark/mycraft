package com.helospark.mycraft.mycraft.pathsearch;

import java.util.Comparator;

class NodeComparator implements Comparator<Node> {
	public int compare(Node node1, Node node2) {
		if (node1.getDistance() > node2.getDistance()) {
			return 1;
		} else if (node1.getDistance() < node2.getDistance()) {
			return -1;
		}
		return 0;
	}
}
