package com.helospark.mycraft.mycraft.views;

import org.lwjgl.util.vector.Vector3f;

import com.helospark.mycraft.mycraft.actor.InventoryComponent;
import com.helospark.mycraft.mycraft.actor.InventoryItem;
import com.helospark.mycraft.mycraft.mathutils.IntVector;

public abstract class MenuActions {
	public static final Vector3f SCALE_VECTOR = new Vector3f(40, 40, 0);
	protected Vector3f position;
	private boolean hasActiveElement = false;
	protected IntVector activeElementPosition = new IntVector();
	protected int width, height;

	// cache
	private IntVector mouseIntVector = new IntVector();

	public MenuActions(Vector3f position, int width, int height) {
		this.position = position;
		this.width = width;
		this.height = height;
	}

	public Vector3f getPosition() {
		return position;
	}

	public void onMouseMotion(IntVector mousePosition) {
		calculateActiveElement(mousePosition);
	}

	protected boolean calculateActiveElement(IntVector mousePosition) {
		IntVector relativePosition = calculateRelativePosition(mousePosition);
		if (relativePosition.x >= 0 && relativePosition.x < width * SCALE_VECTOR.x
				&& relativePosition.y >= 0 && relativePosition.y < height * SCALE_VECTOR.y) {
			hasActiveElement = true;
			activeElementPosition.x = (int) (relativePosition.x / SCALE_VECTOR.x);
			activeElementPosition.y = (int) (relativePosition.y / SCALE_VECTOR.y);
		} else {
			hasActiveElement = false;
			activeElementPosition.x = -1;
			activeElementPosition.y = -1;
		}
		return hasActiveElement;
	}

	protected IntVector calculateRelativePosition(IntVector mousePosition) {
		int mouseX = (int) (mousePosition.x - position.x);
		int mouseY = (int) (mousePosition.y - position.y);
		mouseIntVector.x = mouseX;
		mouseIntVector.y = mouseY;
		return mouseIntVector;
	}

	public InventoryItem onMouseDown(IntVector mousePosition, InventoryItem heldItem, int amount) {
		boolean hasActiveElement = calculateActiveElement(mousePosition);
		InventoryItem finalResult = null;
		if (hasActiveElement) {
			finalResult = handleMenuChange(heldItem, finalResult, amount);
			removeNullElements();
		}
		return finalResult;
	}

	private InventoryItem handleMenuChange(InventoryItem heldItem, InventoryItem finalResult,
			int amount) {
		InventoryItem activeElement = getItemAtActivePosition();
		if (isEmptyAtPosition(activeElementPosition.y, activeElementPosition.x)) {
			finalResult = addItemToCurrentPlace(heldItem, amount);
		} else if (activeElement == null) {
			// cannot put anything to result, make user not switch
			return heldItem;
		} else if (heldItem == null) {
			finalResult = getItemAtActivePosition();
			clearItemAtActivePosition();
		} else if (activeElement.getId() == heldItem.getId()) {
			finalResult = addItems(heldItem, activeElement, amount);
		} else {
			finalResult = exchangeElements(heldItem, activeElement);
		}
		return finalResult;
	}

	protected abstract void clearItemAtActivePosition();

	protected abstract InventoryItem getItemAtActivePosition();

	private InventoryItem addItems(InventoryItem heldItem, InventoryItem activeElement, int amount) {
		InventoryItem finalResult = heldItem;
		int newAmount = activeElement.getAmount() + Math.min(amount, heldItem.getAmount());
		if (newAmount <= InventoryComponent.MAX_AMOUNT_PER_ITEM) {
			activeElement.setAmount(newAmount);
		} else {
			activeElement.setAmount(InventoryComponent.MAX_AMOUNT_PER_ITEM);
			heldItem.setAmount(newAmount - InventoryComponent.MAX_AMOUNT_PER_ITEM);
		}
		heldItem.decreaseAmount(amount);
		if (heldItem.getAmount() <= 0) {
			finalResult = null;
		}
		return finalResult;
	}

	private InventoryItem addItemToCurrentPlace(InventoryItem heldItem, int amount) {
		InventoryItem finalResult = null;
		if (heldItem != null) {
			if (amount >= heldItem.getAmount()) {
				setInventoryItemAtActivePosition(heldItem);
				finalResult = null;
			} else {
				InventoryItem newItem = new InventoryItem(heldItem.getId(), amount);
				setInventoryItemAtActivePosition(newItem);
				heldItem.decreaseAmount(amount);
				finalResult = heldItem;
			}
		}
		return finalResult;
	}

	protected abstract void setInventoryItemAtActivePosition(InventoryItem heldItem);

	private InventoryItem exchangeElements(InventoryItem heldItem, InventoryItem activeElement) {
		InventoryItem resultItem = activeElement;
		setInventoryItemAtActivePosition(heldItem);
		return resultItem;
	}

	protected abstract void removeNullElements();

	public InventoryItem onMouseUp(IntVector mousePosition, InventoryItem heldInventoryItem) {
		return heldInventoryItem;
	}

	public boolean isActivePosition(int i, int j) {
		return hasActiveElement && activeElementPosition.x == j && activeElementPosition.y == i;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public IntVector getActivePosition() {
		return activeElementPosition;
	}

	public abstract boolean isEmptyAtPosition(int i, int j);

	public abstract InventoryItem getItemAtPosition(int i, int j);

	public boolean isPositionOver(IntVector mousePosition) {
		return calculateActiveElement(mousePosition);
	}

	public void setActiveElement(int currentGameItemIndex) {
		this.activeElementPosition.x = currentGameItemIndex;
		this.activeElementPosition.y = 0;
	}
}
