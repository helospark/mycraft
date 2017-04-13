package com.helospark.mycraft.mycraft.pathsearch;

import java.util.concurrent.Future;

public class FutureTimedTask<T> implements Comparable<FutureTimedTask<T>> {
	private Future<T> task;
	private float time;
	private int taskId;

	public FutureTimedTask(int taskId, Future<T> task, float time) {
		this.taskId = taskId;
		this.task = task;
		this.time = time;
	}

	public Future<T> getTask() {
		return task;
	}

	public void setTask(Future<T> task) {
		this.task = task;
	}

	public float getTime() {
		return time;
	}

	public void setTime(float time) {
		this.time = time;
	}

	@Override
	public int compareTo(FutureTimedTask<T> other) {
		if (time > other.time) {
			return 1;
		} else if (time < other.time) {
			return -1;
		}
		return 0;
	}

	public void decreaseTime(double deltaTime) {
		time -= deltaTime;
	}

	public int getTaskId() {
		return taskId;
	}

}
