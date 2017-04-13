package com.helospark.mycraft.mycraft.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.helospark.mycraft.mycraft.actor.InventoryItem;

@Service
public class CraftResolverService {
	public static final int EMPTY_ELEMENT_ID = -1;

	private class Craft {
		private int[][] matrix;
		private InventoryItem result;
		private boolean canRotate;
		private boolean canTranslate;

		public Craft(int[][] matrix, InventoryItem result, boolean canRotate, boolean canTranslate) {
			super();
			this.matrix = matrix;
			this.result = result;
			this.canRotate = canRotate;
			this.canTranslate = canTranslate;
		}

		public int[][] getMatrix() {
			return matrix;
		}

		public InventoryItem getResult() {
			return result;
		}

		public boolean isCanRotate() {
			return canRotate;
		}

		public boolean isCanTranslate() {
			return canTranslate;
		}
	}

	private List<Craft> crafts = new ArrayList<>();
	private Map<InventoryItem, int[][]> itemToMatrix = new HashMap<>();

	public void addCraft(int[][] pattern, InventoryItem inventoryItem, boolean canRotate,
			boolean canTranslate) {

		int[][] newMatrix = removeEmptyColsAndRows(pattern);

		crafts.add(new Craft(newMatrix, inventoryItem, canRotate, canTranslate));
		itemToMatrix.put(inventoryItem, newMatrix);
	}

	public InventoryItem resolveFromMatrix(InventoryItem[][] items) {
		InventoryItem result = findMatchingPattern(items);

		if (result != null) {
			InventoryItem[][] copy = createCopy(items);
			determineResultAmount(copy, result);
		}
		return result;
	}

	private InventoryItem[][] createCopy(InventoryItem[][] items) {
		InventoryItem[][] copied = new InventoryItem[items.length][items[0].length];
		for (int i = 0; i < items.length; ++i) {
			for (int j = 0; j < items[0].length; ++j) {
				if (items[i][j] != null) {
					copied[i][j] = new InventoryItem(items[i][j]);
				}
			}
		}
		return copied;
	}

	private void determineResultAmount(InventoryItem[][] items, InventoryItem result) {
		int newAmount = 1;
		while (true) {
			decreaseMatrix(items, result, 1);
			InventoryItem found = findMatchingPattern(items);
			if (found == null || found.getId() != result.getId()) {
				break;
			}
			++newAmount;
		}
		for (int i = 0; i < newAmount; ++i) {
			increaseMatrix(items, result, result.getAmount());
		}

		result.setAmount(newAmount);
	}

	private void increaseMatrix(InventoryItem[][] items, InventoryItem result, int amount) {
		addToMatrix(items, result, amount);
	}

	private int[][] removeEmptyColsAndRows(int[][] matrix) {
		int top = 0;

		while (top < matrix.length && isEmptyLine(matrix[top])) {
			++top;
		}
		int bottom = matrix.length - 1;
		while (bottom >= top && isEmptyLine(matrix[bottom])) {
			--bottom;
		}
		int left = 0;
		while (left < matrix[0].length && isEmptyColumn(matrix, left)) {
			++left;
		}
		int right = matrix[0].length - 1;
		while (right >= left && isEmptyColumn(matrix, right)) {
			--right;
		}

		int[][] result = new int[bottom - top + 1][right - left + 1];

		for (int i = top, y = 0; i <= bottom; ++i, ++y) {
			for (int j = left, x = 0; j <= right; ++j, ++x) {
				result[y][x] = matrix[i][j];
			}
		}

		return result;
	}

	private boolean isEmptyColumn(int[][] matrix, int col) {
		for (int i = 0; i < matrix.length; ++i) {
			if (matrix[i][col] != CraftResolverService.EMPTY_ELEMENT_ID) {
				return false;
			}
		}
		return true;
	}

	private boolean isEmptyLine(int[] line) {
		for (int i = 0; i < line.length; ++i) {
			if (line[i] != CraftResolverService.EMPTY_ELEMENT_ID) {
				return false;
			}
		}
		return true;
	}

	private InventoryItem findMatchingPattern(InventoryItem[][] items) {
		for (int i = 0; i < crafts.size(); ++i) {
			if (isPatternMatch(crafts.get(i), items)) {
				return new InventoryItem(crafts.get(i).getResult());
			}
		}
		return null;
	}

	private boolean isPatternMatch(Craft craft, InventoryItem[][] items) {
		int[][] craftMatrix = craft.getMatrix();
		int[][] itemMatrix = createItemMatrix(items);

		if (craftMatrix.length != itemMatrix.length
				|| craftMatrix[0].length != itemMatrix[0].length) {
			return false;
		}

		boolean isOk = true;
		for (int y = 0; y < itemMatrix.length && isOk; ++y) {
			for (int x = 0; x < itemMatrix[y].length && isOk; ++x) {
				if (itemMatrix[y][x] != craftMatrix[y][x]) {
					isOk = false;
					break;
				}
			}
		}

		return isOk;
	}

	private int[][] createItemMatrix(InventoryItem[][] items) {
		int[][] matrix = new int[items.length][items[0].length];
		for (int i = 0; i < items.length; ++i) {
			for (int j = 0; j < items[0].length; ++j) {
				if (items[i][j] == null || items[i][j].getAmount() <= 0) {
					matrix[i][j] = EMPTY_ELEMENT_ID;
				} else {
					matrix[i][j] = items[i][j].getId();
				}
			}
		}
		int[][] result = removeEmptyColsAndRows(matrix);

		return result;
	}

	public void decreaseMatrix(InventoryItem[][] items, InventoryItem result, int amount) {
		addToMatrix(items, result, Math.max(-result.getAmount(), -amount));
	}

	private void addToMatrix(InventoryItem[][] items, InventoryItem result, int amount) {
		InventoryItem item = findMatchingPattern(items);

		if (item != null && item.getId() == result.getId()) {
			for (int i = 0; i < items.length; ++i) {
				for (int j = 0; j < items[i].length; ++j) {
					if (items[i][j] != null) {
						items[i][j].increaseAmountBy(amount);
					}
				}
			}
		}
	}
}
