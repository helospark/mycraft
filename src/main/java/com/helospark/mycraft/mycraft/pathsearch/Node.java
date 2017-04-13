package com.helospark.mycraft.mycraft.pathsearch;

import java.util.ArrayList;
import java.util.List;

import com.helospark.mycraft.mycraft.mathutils.IntVector;

public class Node {
	IntVector position;
	List<Node> neighbours = new ArrayList<>();
	Node previous = null;
	float distance;

	public Node(IntVector position) {
		this.position = position;
		this.neighbours = new ArrayList<Node>();
	}

	public void addNeighbour(Node neighbour) {
		if (!neighbours.contains(neighbour))
			neighbours.add(neighbour);
	}

	public List<Node> getNeighbours() {
		return neighbours;
	}

	public Node getPrevious() {
		return previous;
	}

	public void setPrevious(Node previous) {
		this.previous = previous;
	}

	public void setDistance(float distance) {
		this.distance = distance;
	}

	public float getDistance() {
		return distance;
	}

	public void separate(Node other) {
		neighbours.remove(other);
	}

	public IntVector getPosition() {
		return position;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((position == null) ? 0 : position.hashCode());
		return result;
	}

	public boolean equals(IntVector otherPosition) {
		return this.position.equals(otherPosition);
	}

}