package com.helospark.mycraft.mycraft.actor;

public class InventoryItem {
	private int id;
	private int amount;

	public InventoryItem() {
		id = -1;
		amount = 1;
	}

	public InventoryItem(InventoryItem inventoryItem) {
		this.id = inventoryItem.id;
		this.amount = inventoryItem.amount;
	}

	public InventoryItem(int id, int amount) {
		this.id = id;
		this.amount = amount;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InventoryItem other = (InventoryItem) obj;
		if (id != other.id)
			return false;
		return true;
	}

	public void increaseAmountBy(int amount) {
		this.amount += amount;
	}

	public void decreaseAmount() {
		--amount;
	}

	@Override
	public String toString() {
		return "InventoryItem [id=" + id + ", amount=" + amount + "]";
	}

	public void decreaseAmount(int amount) {
		this.amount -= amount;
	}

}