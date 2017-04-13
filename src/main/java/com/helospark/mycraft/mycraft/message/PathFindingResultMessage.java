package com.helospark.mycraft.mycraft.message;

import java.util.List;

import com.helospark.mycraft.mycraft.mathutils.IntVector;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public class PathFindingResultMessage extends Message {
	private int taskId;
	private List<IntVector> resultList;

	public PathFindingResultMessage(MessageTypes type, int target, int taskId,
			List<IntVector> resultList) {
		super(type, target);
		this.taskId = taskId;
		this.resultList = resultList;
	}

	public int getTaskId() {
		return taskId;
	}

	public List<IntVector> getResultList() {
		return resultList;
	}

	@Override
	public String serializeToString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Message deserializeFromString(String[] data, MessageTypes messageType) {
		// TODO Auto-generated method stub
		return null;
	}

}
