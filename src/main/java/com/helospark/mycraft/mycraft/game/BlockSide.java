package com.helospark.mycraft.mycraft.game;

import org.lwjgl.util.vector.Vector3f;

import com.helospark.mycraft.mycraft.attributes.Model;
import com.helospark.mycraft.mycraft.mathutils.IntVector;
import com.helospark.mycraft.mycraft.render.Material;
import com.helospark.mycraft.mycraft.render.RenderableBlockNode;
import com.helospark.mycraft.mycraft.shader.Shader;

public class BlockSide {
	private RenderableBlockNode node;
	private IntVector side;
	int textureX, textureY;
	IntVector intPosition;
	Vector3f position;

	public BlockSide(Model model, Shader shader, IntVector side) {
		node = new RenderableBlockNode(shader, model);
		this.side = side;
		position = node.getPosition();
		intPosition = GameMap.getIntPositionFromPosition(node.getPosition());
	}

	public BlockSide(BlockSide blockSide) {
		this.side = blockSide.side;
		this.node = new RenderableBlockNode(blockSide.node);
		this.textureX = blockSide.textureX;
		this.textureY = blockSide.textureY;
		intPosition = GameMap.getIntPositionFromPosition(node.getPosition());
		position = node.getPosition();
	}

	public RenderableBlockNode getNode() {
		return node;
	}

	public Material getMaterial() {
		return node.getMaterial();
	}

	public void setMaterial(Material material) {
		node.setMaterial(material);
	}

	public void setTexturePosition(int textureX, int textureY) {
		this.textureX = textureX;
		this.textureY = textureY;
	}

	public int getTexturePositionU() {
		return textureY;
	}

	public int getTexturePositionV() {
		return textureX;
	}

	public IntVector getIntPosition() {
		return intPosition;
	}

	public void calculateIntPosition() {
		// intPosition = GameMap.getIntPositionFromPosition(node.getPosition());
	}

	public void setIntPosition(IntVector intVector) {
		this.intPosition = intVector;
		this.position = new Vector3f(intPosition.x * Block.SIZE, intPosition.y * Block.SIZE,
				intPosition.z * Block.SIZE);
	}

	public Vector3f getPosition() {
		return position;
	}

	public IntVector getSide() {
		return side;
	}

}
