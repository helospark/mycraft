package com.helospark.mycraft.mycraft.game;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import com.helospark.mycraft.mycraft.mathutils.BoundingBox;
import com.helospark.mycraft.mycraft.mathutils.IntVector;
import com.helospark.mycraft.mycraft.render.RenderableBlockNode;

public class LightSource {

	class LitVertex {
		float lightAmount;
		RenderableBlockNode blockNode;
		int vertexIndex;

		public LitVertex(RenderableBlockNode blockNode, int vertexIndex, float lightAmount) {
			this.blockNode = blockNode;
			this.vertexIndex = vertexIndex;
			this.lightAmount = lightAmount;

		}

		public float getLightAmount() {
			return lightAmount;
		}

		public void setLightAmount(float lightAmount) {
			this.lightAmount = lightAmount;
		}

		public RenderableBlockNode getBlockNode() {
			return blockNode;
		}

		public void setBlockNode(RenderableBlockNode blockNode) {
			this.blockNode = blockNode;
		}

		public int getVertexIndex() {
			return vertexIndex;
		}

		public void setVertexIndex(int vertexIndex) {
			this.vertexIndex = vertexIndex;
		}

	}

	private Block block;
	private IntVector position;
	private List<LitVertex> litVertices = new ArrayList<>();
	private BoundingBox boundingBox;

	public LightSource(Block block, IntVector position) {
		this.block = block;
		this.position = position;

		Vector3f lowerPosition = new Vector3f(position.x - block.getLightDistance(), position.z - block.getLightDistance(), position.y - block.getLightDistance());

		Vector3f upperPosition = new Vector3f(position.x + block.getLightDistance(), position.z + block.getLightDistance(), position.y + block.getLightDistance());
		boundingBox = BoundingBox.fromTwoPoints(lowerPosition, upperPosition);
	}

	public Block getBlock() {
		return block;
	}

	public void setBlock(Block block) {
		this.block = block;
	}

	public IntVector getPosition() {
		return position;
	}

	public void setPosition(IntVector position) {
		this.position = position;
	}

	public void addLitVertex(RenderableBlockNode blockNode, int vertexIndex, float lightAmount) {
		litVertices.add(new LitVertex(blockNode, vertexIndex, lightAmount));
	}

	public void clearLitVertices() {
		litVertices.clear();
	}

	public List<LitVertex> getLitVertices() {
		return litVertices;
	}

	public BoundingBox getBoundingBox() {
		return boundingBox;
	}
}