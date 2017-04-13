package com.helospark.mycraft.mycraft.views;

import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import com.helospark.mycraft.mycraft.actor.InventoryItem;

public class InventoryMenuActions extends MenuActions {

	private List<InventoryItem> inventoryList;
	private InventoryItem defaultItem;

	public InventoryMenuActions(Vector3f position, int width, int height,
			List<InventoryItem> inventoryList, InventoryItem defaultItem) {
		super(position, width, height);
		this.inventoryList = inventoryList;
		this.defaultItem = defaultItem;
	}

	@Override
	protected InventoryItem getItemAtActivePosition() {
		int index = activeElementPosition.y * width + activeElementPosition.x;
		InventoryItem result = inventoryList.get(index);
		return result;
	}

	@Override
	protected void setInventoryItemAtActivePosition(InventoryItem element) {
		int index = activeElementPosition.y * width + activeElementPosition.x;
		if (element == null) {
			inventoryList.set(index, defaultItem);
		} else {
			inventoryList.set(index, element);
		}
	}

	@Override
	protected void removeNullElements() {
		for (int i = 0; i < inventoryList.size(); ++i) {
			if (inventoryList.get(i).getAmount() <= 0) {
				inventoryList.set(i, defaultItem);
			}
		}
	}

	@Override
	public boolean isEmptyAtPosition(int i, int j) {
		int index = i * width + j;
		if (index >= inventoryList.size())
			return false;
		return inventoryList.get(index) == null || inventoryList.get(index) == defaultItem;
	}

	@Override
	public InventoryItem getItemAtPosition(int i, int j) {
		int index = i * width + j;
		return inventoryList.get(index);
	}

	@Override
	protected void clearItemAtActivePosition() {
		setInventoryItemAtActivePosition(defaultItem);
	}
}
