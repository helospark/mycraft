package com.helospark.mycraft.mycraft.maze;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MazePanel {
	public static final int MAZE_CELL_VERTICAL = 64;
	public static final int MAZE_CELL_HORIZONTAL = 64;

	public static final int MAZE_CELL_WIDTH = 1;
	public static final int MAZE_CELL_HEIGHT = 1;
	private static final int MAX_CELL_NUMBER = 10;
	Random random = new Random(0);
	long sleepTime = 0;
	Dimension currentCell = new Dimension(0, 0);
	boolean finished = false;

	List<Dimension> listOfLastCells = new ArrayList<Dimension>();

	MazeCell[][] mazeCells;
	List<Dimension> cells = new ArrayList<Dimension>();

	public MazePanel() {
		mazeCells = new MazeCell[MAZE_CELL_HORIZONTAL][MAZE_CELL_VERTICAL];
		for (int i = 0; i < mazeCells.length; ++i) {
			for (int j = 0; j < mazeCells[i].length; ++j) {
				mazeCells[i][j] = new MazeCell();
			}
		}
	}

	public void generateMaze() {
		generate(0, 0);
		finished = true;
	}

	public void generate(int currentX, int currentY) {
		Dimension[] dimensions = new Dimension[4];
		dimensions[0] = new Dimension(0, 1);
		dimensions[1] = new Dimension(0, -1);
		dimensions[2] = new Dimension(1, 0);
		dimensions[3] = new Dimension(-1, 0);

		shuffleArray(dimensions);

		for (int i = 0; i < 4; ++i) {
			int newX = currentX + dimensions[i].width;
			int newY = currentY + dimensions[i].height;
			currentCell = new Dimension(newX, newY);

			listOfLastCells.add(0, currentCell);
			if (listOfLastCells.size() > MAX_CELL_NUMBER) {
				listOfLastCells.remove(listOfLastCells.size() - 1);
			}

			if (newX >= 0 && newY >= 0 && newX < MAZE_CELL_VERTICAL - 1
					&& newY < MAZE_CELL_HORIZONTAL - 1) {

				boolean succeded = false;

				if (dimensions[i].height == 0 && dimensions[i].width == -1
						&& !isVisited(newX, newY)) {
					mazeCells[currentY][currentX].setLeftWall(false);
					succeded = true;
				}
				if (dimensions[i].height == -1 && dimensions[i].width == 0
						&& !isVisited(newX, newY)) {
					mazeCells[currentY][currentX].setTopWall(false);
					succeded = true;
				}
				if (dimensions[i].height == 0 && dimensions[i].width == 1
						&& !isVisited(newX, newY)) {
					mazeCells[currentY][currentX + 1].setLeftWall(false);
					succeded = true;
				}
				if (dimensions[i].height == 1 && dimensions[i].width == 0
						&& !isVisited(newX, newY)) {
					mazeCells[currentY + 1][currentX].setTopWall(false);
					succeded = true;
				}
				if (succeded)
					generate(newX, newY);
			}
		}
	}

	private boolean isVisited(int newX, int newY) {
		return !(mazeCells[newY][newX].isTopWall()
				&& mazeCells[newY][newX].isLeftWall()
				&& mazeCells[newY + 1][newX].isTopWall() && mazeCells[newY][newX + 1]
				.isLeftWall());
	}

	private void shuffleArray(Dimension[] dimensions) {
		for (int i = 0; i < 10; ++i) {
			int a = random.nextInt(dimensions.length);
			int b = random.nextInt(dimensions.length);

			Dimension tmp = dimensions[a];
			dimensions[a] = dimensions[b];
			dimensions[b] = tmp;
		}
	}

	public MazeCell getCurrentCell(int x, int y) {
		return mazeCells[y][x];
	}

	public int getWidth() {
		return MAZE_CELL_HORIZONTAL;
	}

	public int getHeight() {
		return MAZE_CELL_VERTICAL;
	}
}
