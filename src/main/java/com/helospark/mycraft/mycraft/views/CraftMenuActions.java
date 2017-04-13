package com.helospark.mycraft.mycraft.views;

import org.lwjgl.util.vector.Vector3f;
import org.springframework.context.ApplicationContext;

import com.helospark.mycraft.mycraft.actor.InventoryItem;
import com.helospark.mycraft.mycraft.mathutils.IntVector;
import com.helospark.mycraft.mycraft.services.CraftResolverService;
import com.helospark.mycraft.mycraft.singleton.Singleton;

public class CraftMenuActions extends MenuActions {
	public static final Vector3f ARROW_SCALE = new Vector3f(40, 30, 0);
	public static final float VERTICAL_SPACE_NEXT_TO_ARROW = 20.0f;
	private InventoryItem[][] items;
	private boolean isResultActive = false;
	private InventoryItem craftResultedItemAtCell = null;
	private int size;
	private Vector3f resultStartPosition;
	private CraftResolverService craftResolverService;
	private InventoryItem craftResult = null;

	public CraftMenuActions(Vector3f position, int size) {
		super(position, size, size);
		items = new InventoryItem[size][size];
		this.size = size;
		resultStartPosition = new Vector3f();
		resultStartPosition.x = position.x + size * SCALE_VECTOR.x + ARROW_SCALE.x
				+ VERTICAL_SPACE_NEXT_TO_ARROW * 2;
		resultStartPosition.y = position.y + size * SCALE_VECTOR.y / 2.0f
				- InventoryMenuActions.SCALE_VECTOR.y / 2.0f;
		ApplicationContext context = Singleton.getInstance().getContext();
		craftResolverService = context.getBean(CraftResolverService.class);
	}

	@Override
	protected boolean calculateActiveElement(IntVector mousePosition) {
		boolean hasFoundElement = super.calculateActiveElement(mousePosition);
		if (isPositionAboveResultCell(mousePosition)) {
			isResultActive = true;
		} else {
			isResultActive = false;
		}
		return hasFoundElement || isResultActive;
	}

	private boolean isPositionAboveResultCell(IntVector mouseIntPosition) {
		return mouseIntPosition.x >= resultStartPosition.x
				&& mouseIntPosition.x < resultStartPosition.x + SCALE_VECTOR.x
				&& mouseIntPosition.y >= resultStartPosition.y
				&& mouseIntPosition.y < resultStartPosition.y + SCALE_VECTOR.y;
	}

	@Override
	public InventoryItem onMouseDown(IntVector inMousePosition, InventoryItem heldItem, int amount) {
		InventoryItem methodResult = super.onMouseDown(inMousePosition, heldItem, amount);
		calculateActiveElement(inMousePosition);
		if (isResultActive && heldItem == null) {
			methodResult = craftItem(amount);
			craftResult = methodResult;
		}
		craftResultedItemAtCell = craftResolverService.resolveFromMatrix(items);
		return methodResult;
	}

	private InventoryItem craftItem(int amount) {
		InventoryItem resultHeldItem;
		craftResolverService.decreaseMatrix(items, craftResultedItemAtCell, amount);
		resultHeldItem = new InventoryItem(craftResultedItemAtCell);
		resultHeldItem.setAmount(Math.min(amount, craftResultedItemAtCell.getAmount()));
		craftResultedItemAtCell = null;
		removeNullElements();
		return resultHeldItem;
	}

	@Override
	protected void removeNullElements() {
		for (int i = 0; i < items.length; ++i) {
			for (int j = 0; j < items[i].length; ++j) {
				if (items[i][j] != null && items[i][j].getAmount() <= 0) {
					items[i][j] = null;
				}
			}
		}
	}

	public boolean isResultActive() {
		return isResultActive;
	}

	public InventoryItem getResultInventoryItem() {
		return craftResultedItemAtCell;
	}

	public InventoryItem[][] getItems() {
		return items;
	}

	public Vector3f getResultStartPosition() {
		return resultStartPosition;
	}

	public int getSize() {
		return size;
	}

	@Override
	protected InventoryItem getItemAtActivePosition() {
		if (isResultActive) {
			return craftResult;
		} else {
			return items[activeElementPosition.y][activeElementPosition.x];
		}
	}

	@Override
	protected void setInventoryItemAtActivePosition(InventoryItem heldItem) {
		if (isResultActive) {
			isResultActive = false;
			craftResult = null;
		}
		if (activeElementPosition.y >= 0 && activeElementPosition.x >= 0) {
			items[activeElementPosition.y][activeElementPosition.x] = heldItem;
		}
	}

	@Override
	public boolean isEmptyAtPosition(int i, int j) {
		if (i < 0 || j < 0 || i >= height || j >= width) {
			return false;
		}
		return items[i][j] == null;
	}

	@Override
	public InventoryItem getItemAtPosition(int i, int j) {
		return items[i][j];
	}

	@Override
	protected void clearItemAtActivePosition() {
		setInventoryItemAtActivePosition(null);
	}
}
