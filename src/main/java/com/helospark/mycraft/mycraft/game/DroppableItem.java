package com.helospark.mycraft.mycraft.game;

import com.helospark.mycraft.mycraft.actor.InventoryItem;

public class DroppableItem {

	private float chance;
	private int maxDrop;
	private InventoryItem inventoryItem;

	public DroppableItem(InventoryItem inventoryItem, float chance, int maxDrop) {
		this.inventoryItem = inventoryItem;
		this.chance = chance;
		this.maxDrop = maxDrop;
	}

	public float getChance() {
		return chance;
	}

	public int getMaxDrop() {
		return maxDrop;
	}

	public InventoryItem getInventoryItem() {
		return inventoryItem;
	}

}
