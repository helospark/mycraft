package com.helospark.mycraft.mycraft.maze;
import java.awt.Dimension;

public class MazeCell {
	boolean leftWall = true;
	boolean topWall = true;

	public MazeCell() {

	}

	public void destroyWall(Dimension dimension) {
		if (dimension.width == -1 && dimension.height == 0) {
			leftWall = false;
		}
		if (dimension.width == 0 && dimension.height == -1) {
			topWall = false;
		}
	}

	public boolean isLeftWall() {
		return leftWall;
	}

	public void setLeftWall(boolean leftWall) {
		this.leftWall = leftWall;
	}

	public boolean isTopWall() {
		return topWall;
	}

	public void setTopWall(boolean topWall) {
		this.topWall = topWall;
	}

}
