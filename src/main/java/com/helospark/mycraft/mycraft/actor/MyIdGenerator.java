package com.helospark.mycraft.mycraft.actor;

import org.springframework.stereotype.Service;

@Service
public class MyIdGenerator {
	private int lowerBound = 0;
	private int upperBound = Integer.MAX_VALUE;
	private boolean wasInitialized = false;

	private int id = lowerBound;

	public Integer getNextId() {
		if (id >= upperBound) {
			id = lowerBound;
		}
		return id++;
	}

	public void setLowerBound(int lowerBound) {
		this.lowerBound = lowerBound;
		if (id < lowerBound) {
			id = lowerBound;
		}
	}

	public void setUpperBound(int upperBound) {
		this.upperBound = upperBound;
		if (id >= upperBound) {
			id = lowerBound;
		}
	}

	public boolean wasInitialized() {
		return wasInitialized;
	}

	public void setInitialized() {
		this.wasInitialized = true;
	}

}
