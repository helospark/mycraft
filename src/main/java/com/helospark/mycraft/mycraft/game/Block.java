package com.helospark.mycraft.mycraft.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector3f;
import org.springframework.context.ApplicationContext;

import com.helospark.mycraft.mycraft.actor.GameItems;
import com.helospark.mycraft.mycraft.attributes.Model;
import com.helospark.mycraft.mycraft.blockrightclickhandler.BlockRightClickHandler;
import com.helospark.mycraft.mycraft.blockupdaters.BlockTimeUpdater;
import com.helospark.mycraft.mycraft.mathutils.BoundingBox;
import com.helospark.mycraft.mycraft.mathutils.IntVector;
import com.helospark.mycraft.mycraft.render.Material;
import com.helospark.mycraft.mycraft.render.MyTexture;
import com.helospark.mycraft.mycraft.render.RenderableModelNode;
import com.helospark.mycraft.mycraft.services.GlobalParameters;
import com.helospark.mycraft.mycraft.shader.FloatShaderUniform;
import com.helospark.mycraft.mycraft.shader.IntegerShaderUniform;
import com.helospark.mycraft.mycraft.shader.Shader;
import com.helospark.mycraft.mycraft.singleton.Singleton;

public class Block {
	public static final int RIGHT_SIDE = 0;
	public static final int LEFT_SIDE = 1;
	public static final int TOP_SIDE = 2;
	public static final int BOTTOM_SIDE = 3;
	public static final int FAR_SIDE = 4;
	public static final int NEAR_SIDE = 5;
	public static final int NUM_SIDES = 6;
	public static final float SIZE = 1.0f;
	public static final int MAX_LIGHT_DISTANCE = 10;

	private int id;
	private float strength;
	private BlockSide[] sides;
	private boolean isTransparent = false;
	private BoundingBox boundingBox;
	private List<DroppableItem> droppableItems = new ArrayList<DroppableItem>();
	private List<Integer> itemsCapableOfDestroyingBlock = null;
	private Shader damageShader;
	private Model[] models;
	private int lightStrength = 0;
	private int lightDistance = 0;
	private GameItems gameItems;
	private BlockRightClickHandler blockRightClickHandler;
	GlobalParameters globalParameters;
	private boolean canCollide = true;
	private BlockTimeUpdater blockTimeUpdater;

	public Block(Model[] models, Shader shader, int type) {
		sides = new BlockSide[models.length];

		for (int i = 0; i < models.length; ++i) {
			IntVector side = RenderableBlock.intVectorFromSide(i);
			sides[i] = new BlockSide(models[i], shader, side);
		}
		// sides[RIGHT_SIDE] = new BlockSide(models[0], shader, new IntVector(1,
		// 0, 0));
		// sides[LEFT_SIDE] = new BlockSide(models[1], shader, new IntVector(-1,
		// 0, 0));
		// sides[TOP_SIDE] = new BlockSide(models[2], shader, new IntVector(0,
		// 1,
		// 0));
		// sides[BOTTOM_SIDE] = new BlockSide(models[3], shader, new
		// IntVector(0,
		// -1, 0));
		// sides[FAR_SIDE] = new BlockSide(models[4], shader, new IntVector(0,
		// 0,
		// 1));
		// sides[NEAR_SIDE] = new BlockSide(models[5], shader, new IntVector(0,
		// 0,
		// -1));
		this.id = type;
		boundingBox = BoundingBox.fromTwoPoints(new Vector3f(0, 0, 0), new Vector3f(SIZE, SIZE,
				SIZE));
		this.models = models;
		ApplicationContext applicationContext = Singleton.getInstance().getContext();
		globalParameters = applicationContext.getBean(GlobalParameters.class);
		gameItems = applicationContext.getBean(GameItems.class);
	}

	public void setDamageShader(Shader damageShader) {
		this.damageShader = damageShader;
	}

	public void setMaterial(Material material) {
		for (int i = 0; i < sides.length; ++i) {
			sides[i].setMaterial(material);
		}
	}

	public BlockSide generateNewBlockSide(int h) {
		return new BlockSide(sides[h]);
	}

	public int getType() {
		return id;
	}

	public boolean isTransparent() {
		return isTransparent;
	}

	public void setTransparent(boolean isTransparent) {
		this.isTransparent = isTransparent;
	}

	public void setTexturePosition(int side, int i, int j) {
		sides[side].setTexturePosition(i, j);
	}

	public BoundingBox getBoundingBox() {
		return boundingBox;
	}

	public List<DroppableItem> getPossibleDroppableItems() {
		return droppableItems;
	}

	public BoundingBox calculateBoundingBox(int x, int y, int z) {
		BoundingBox newBoundingBox = new BoundingBox(boundingBox);
		newBoundingBox.setPosition(x * Block.SIZE, y * Block.SIZE, z * Block.SIZE);
		return newBoundingBox;
	}

	public void addDroppableItem(DroppableItem droppableItem) {
		this.droppableItems.add(droppableItem);
	}

	public RenderableModelNode[] createRenderableModelNodeForDamagedBlock() {
		RenderableModelNode[] result = new RenderableModelNode[sides.length];
		for (int i = 0; i < sides.length; ++i) {
			result[i] = new RenderableModelNode(damageShader, models[i]);
			result[i].setUniform("blockY", new IntegerShaderUniform(sides[i].textureX));
			result[i].setUniform("blockX", new IntegerShaderUniform(sides[i].textureY));
			float inverseTextureSize = globalParameters.inverseTextureUnitSize;
			result[i].setUniform("inverseTextureUnitSize", new FloatShaderUniform(
					inverseTextureSize));
			result[i].setUniform("textureX", new FloatShaderUniform(15 * inverseTextureSize));

			Map<String, MyTexture> textures = sides[i].getMaterial().getTextures();
			for (Map.Entry<String, MyTexture> texture : textures.entrySet()) {
				result[i].addTexture(texture.getKey(), texture.getValue());
			}
		}
		return result;
	}

	public MyTexture getTexture() {
		return sides[0].getMaterial().getTexture();
	}

	public void setStrength(float strength) {
		this.strength = strength;
	}

	public int getNumberOfModels() {
		return models.length;
	}

	public void enableLighting(int lightStrength, int lightDistance) {
		this.lightDistance = lightDistance;
		this.lightStrength = lightStrength;
	}

	public boolean isLightEnabled() {
		return lightStrength > 0;
	}

	public int getLightStrength() {
		return lightStrength;
	}

	public void setLightStrength(int lightStrength) {
		this.lightStrength = lightStrength;
	}

	public int getLightDistance() {
		return lightDistance;
	}

	public void setLightDistance(int lightDistance) {
		this.lightDistance = lightDistance;
	}

	public void addItemsCapableOfDestroyingBlock(List<Integer> itemsCapableOfDestroyingBlock) {
		this.itemsCapableOfDestroyingBlock = itemsCapableOfDestroyingBlock;
	}

	public boolean canDestroyBlockWithItem(int itemId) {
		if (itemsCapableOfDestroyingBlock == null) {
			return true;
		}
		if (itemsCapableOfDestroyingBlock.contains(itemId)) {
			return true;
		}
		for (int i = 0; i < itemsCapableOfDestroyingBlock.size(); ++i) {
			if (gameItems.isItemBetterThan(itemId, itemsCapableOfDestroyingBlock.get(i))) {
				return true;
			}
		}
		return false;
	}

	public boolean handleRightClick(IntVector position, int itemId) {
		if (blockRightClickHandler == null) {
			return false;
		}
		return blockRightClickHandler.onRightClick(this, position, itemId);
	}

	public void setRightClickHandler(BlockRightClickHandler blockRightClickHandler) {
		this.blockRightClickHandler = blockRightClickHandler;
	}

	public boolean canCollide() {
		return canCollide;
	}

	public void setCanCollide(boolean canCollide) {
		this.canCollide = canCollide;
	}

	public BlockTimeUpdater getBlockTimeUpdater() {
		return blockTimeUpdater;
	}

	public void setBlockTimeUpdater(BlockTimeUpdater blockTimeUpdater) {
		this.blockTimeUpdater = blockTimeUpdater;
	}

	public void addTimeBlockUpdater(BlockTimeUpdater updater) {
		this.blockTimeUpdater = updater;
	}
}
