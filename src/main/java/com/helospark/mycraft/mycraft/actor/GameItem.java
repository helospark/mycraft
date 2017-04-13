package com.helospark.mycraft.mycraft.actor;

import java.util.Map;

import org.lwjgl.util.vector.Vector3f;

import com.helospark.mycraft.mycraft.game.Block;
import com.helospark.mycraft.mycraft.itemrightclickhandlers.GameItemRightClickHandler;
import com.helospark.mycraft.mycraft.mathutils.IntVector;
import com.helospark.mycraft.mycraft.render.MyTexture;

public class GameItem {
	public static final float DEFAULT_STRENGTH = 0.008f;
	public static final Vector3f ITEM_SIZE = new Vector3f(0.2f, 0.2f, 0.2f);
	private GameItems gameItems; // to deserialize
	String name;
	private int id;
	private int blockId;
	private boolean isStackable = true;
	String modelsId;
	GameItemRightClickHandler blockHandler;
	private int u, v;
	MyTexture spriteTexture;
	Map<Integer, Float> blockMultipliers;
	float strength = DEFAULT_STRENGTH;

	public GameItem(int id, int blockId, String modelsId,
			GameItemRightClickHandler handler) {
		this.id = id;
		this.modelsId = modelsId;
		this.blockId = blockId;
		this.blockHandler = handler;
	}

	public GameItem() {
		id = -1;
	}

	public boolean isStackable() {
		return isStackable;
	}

	public boolean applyEffectToBlock(Block activeBlock, IntVector position,
			IntVector sideVector) {
		if (blockHandler != null) {
			return blockHandler.applyEffect(activeBlock, position, blockId,
					sideVector);
		}
		return false;
	}

	public int getId() {
		return id;
	}

	public Vector3f getSize() {
		return ITEM_SIZE;
	}

	public String getModels() {
		return modelsId;
	}

	public float getU() {
		return u;
	}

	public float getV() {
		return v;
	}

	public void setUv(int u, int v) {
		this.u = u;
		this.v = v;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSpriteTexture(MyTexture spriteTexture) {
		this.spriteTexture = spriteTexture;
	}

	public void addBlockMultipliers(Map<Integer, Float> blockToMultiplierMap) {
		this.blockMultipliers = blockToMultiplierMap;
	}

	public float getMultiplierForBlock(int blockId) {
		if (blockMultipliers == null) {
			return 1.0f;
		}
		Float multiplier = blockMultipliers.get(blockId);
		if (multiplier == null) {
			return 1.0f;
		}
		return multiplier;
	}

	public void setStrength(float strength) {
		this.strength = strength;
	}

	public float getStrength() {
		return strength;
	}
}
