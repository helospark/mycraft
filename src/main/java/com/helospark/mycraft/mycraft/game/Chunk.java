package com.helospark.mycraft.mycraft.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector3f;

import com.helospark.mycraft.mycraft.attributes.Model;
import com.helospark.mycraft.mycraft.helpers.SerializationHelpers;
import com.helospark.mycraft.mycraft.mathutils.BoundingBox;
import com.helospark.mycraft.mycraft.mathutils.Frustum;
import com.helospark.mycraft.mycraft.mathutils.IntVector;
import com.helospark.mycraft.mycraft.render.Material;

public class Chunk {
	public static final int CHUNK_BLOCK_SIZE = 64;
	public static final float CHUNK_UNIT_SIZE = CHUNK_BLOCK_SIZE * Block.SIZE;
	public static final int SERIALIZED_SIZE = CHUNK_BLOCK_SIZE * CHUNK_BLOCK_SIZE
			* CHUNK_BLOCK_SIZE * Integer.BYTES + 3 * Integer.BYTES;
	List<BlockSide> visibleBlocks = new ArrayList<BlockSide>();
	RenderableBlock[][][] blocks = new RenderableBlock[CHUNK_BLOCK_SIZE][CHUNK_BLOCK_SIZE][CHUNK_BLOCK_SIZE];
	Vector3f position;
	IntVector blockPosition;
	boolean isVisible = true;

	OctreeChunk octree;
	List<LightSource> lightSources = new ArrayList<>();

	public Chunk(int x, int y, int z) {
		position = new Vector3f(x * CHUNK_BLOCK_SIZE * Block.SIZE, y * CHUNK_BLOCK_SIZE
				* Block.SIZE, z * CHUNK_BLOCK_SIZE * Block.SIZE);
		// int intPositionX = (int) Math.floor((float) x / (CHUNK_UNIT_SIZE));
		// int intPositionY = (int) Math.floor((float) y / (CHUNK_UNIT_SIZE));
		// int intPositionZ = (int) Math.floor((float) z / (CHUNK_UNIT_SIZE));
		blockPosition = new IntVector(x, y, z);
		octree = new OctreeChunk(position.x, position.y, position.z, CHUNK_UNIT_SIZE);
	}

	public void getVisibleObjectst(Frustum frustum, List<BlockSide> visibleBlocks) {
		visibleBlocks.addAll(this.visibleBlocks);
	}

	public boolean isVisible() {
		return isVisible;
	}

	public void setBlock(int x, int y, int z, RenderableBlock block) {
		blocks[x][y][z] = block;
	}

	public RenderableBlock getRenderableBlockAt(int x, int y, int z) {
		return blocks[x][y][z];
	}

	public void clearVisibleObjects() {
		visibleBlocks.clear();
	}

	public Block getBlock(int x, int y, int z) {
		if (x < 0 || x >= Chunk.CHUNK_BLOCK_SIZE || y < 0 || y >= 64 || z < 0 || z >= 64) {
			return Blocks.getBlockForId(Blocks.get("Air"));
		}
		RenderableBlock block = blocks[x][y][z];
		if (block == null) {
			return Blocks.getBlockForId(Blocks.get("Air"));
		}
		int blockId = block.getId();
		return Blocks.getBlockForId(blockId);
	}

	public IntVector getIntPosition() {
		return blockPosition;
	}

	public Vector3f getPosition() {
		return position;
	}

	public void addVisibleObject(BlockSide side) {
		// visibleBlocks.add(side);
		side.calculateIntPosition();
		octree.addBlock(side);
	}

	public BoundingBox getBoundingBox() {
		Vector3f bottomRightPosition = new Vector3f(position.x + CHUNK_UNIT_SIZE, position.y
				+ CHUNK_UNIT_SIZE, position.z + CHUNK_UNIT_SIZE);
		return BoundingBox.fromTwoPoints(position, bottomRightPosition);
	}

	public void addAllVisibleEntities(Frustum frustum, Map<Material, List<Model>> visibleBlockSides) {
		octree.getVisibleObjects(frustum, visibleBlockSides);
	}

	public IntVector getIntPositionInRange(IntVector position) {
		int neighbourBlockXX = getPositionInChunkRange(position.x, getIntPosition().x);
		int neighbourBlockYY = getPositionInChunkRange(position.y, getIntPosition().y);
		int neighbourBlockZZ = getPositionInChunkRange(position.z, getIntPosition().z);

		return new IntVector(neighbourBlockXX, neighbourBlockYY, neighbourBlockZZ);
	}

	private int getPositionInChunkRange(int neighbourCoord, int chunkIntPos) {

		if (neighbourCoord >= 0 && neighbourCoord < chunkIntPos * Chunk.CHUNK_BLOCK_SIZE) {
			return neighbourCoord;
		}

		int result = Math.abs(Math.abs(neighbourCoord)
				- Math.abs(chunkIntPos * Chunk.CHUNK_BLOCK_SIZE));

		if (result >= Chunk.CHUNK_BLOCK_SIZE)
			result = Chunk.CHUNK_BLOCK_SIZE - 1;

		return result;
	}

	public void setBlockAt(IntVector blockPosition, int id) {
		blocks[blockPosition.x][blockPosition.y][blockPosition.z] = new RenderableBlock(id);
	}

	public void setBlockAt(int x, int y, int z, int id) {
		blocks[x][y][z] = new RenderableBlock(id);
	}

	public void removeBlockFromVisible(int x, int y, int z, BoundingBox boundingBox) {
		IntVector intVector = new IntVector(x, y, z);
		removeBlockFromVisible(intVector, boundingBox);
	}

	private void removeBlockFromVisible(IntVector intVector, BoundingBox boundingBox) {
		octree.removeVisibleBlock(intVector, boundingBox);
	}

	public byte[] serialize() {
		byte[] result = new byte[SERIALIZED_SIZE];
		int resultIndex = 0;
		resultIndex += SerializationHelpers.serializeIntegerIntoArrayAtPosition(result,
				resultIndex, blockPosition.x);
		resultIndex += SerializationHelpers.serializeIntegerIntoArrayAtPosition(result,
				resultIndex, blockPosition.y);
		resultIndex += SerializationHelpers.serializeIntegerIntoArrayAtPosition(result,
				resultIndex, blockPosition.z);
		for (int i = 0; i < CHUNK_BLOCK_SIZE; ++i) {
			for (int j = 0; j < CHUNK_BLOCK_SIZE; ++j) {
				for (int k = 0; k < CHUNK_BLOCK_SIZE; ++k) {
					resultIndex += SerializationHelpers.serializeIntegerIntoArrayAtPosition(result,
							resultIndex, blocks[i][j][k].id);
				}
			}
		}
		return result;
	}

	public static Chunk deserialize(byte[] data) {
		int resultIndex = 0;
		int x = SerializationHelpers.deserializeIntegerFromArray(data, resultIndex);
		resultIndex += Integer.BYTES;
		int y = SerializationHelpers.deserializeIntegerFromArray(data, resultIndex);
		resultIndex += Integer.BYTES;
		int z = SerializationHelpers.deserializeIntegerFromArray(data, resultIndex);
		resultIndex += Integer.BYTES;
		Chunk resultChunk = new Chunk(x, y, z);
		for (int i = 0; i < CHUNK_BLOCK_SIZE; ++i) {
			for (int j = 0; j < CHUNK_BLOCK_SIZE; ++j) {
				for (int k = 0; k < CHUNK_BLOCK_SIZE; ++k) {
					int blockId = SerializationHelpers.deserializeIntegerFromArray(data,
							resultIndex);
					resultIndex += Integer.BYTES;
					resultChunk.setBlockAt(i, j, k, blockId);
				}
			}
		}

		return resultChunk;
	}

	public void clearAllLoadedData() {
		blocks = null;
		visibleBlocks.clear();
		visibleBlocks = null;
		octree = null;
	}

	public OctreeChunk getOctree() {
		return octree;
	}

	public void addLightSource(LightSource lightSource) {
		lightSources.add(lightSource);
	}

	public List<LightSource> getLightSources() {
		return lightSources;
	}

	public BlockSide[] getVisibleBlockSides(IntVector blockPosition, BoundingBox boundingBox) {
		List<BlockSide> result = new ArrayList<>();
		octree.getVisibleBlockSides(blockPosition, boundingBox, result);
		return result.toArray(new BlockSide[0]);
	}

	public void removeLightSourceAtPositionIfContains(IntVector position) {
		for (int i = 0; i < lightSources.size(); ++i) {
			if (lightSources.get(i).getPosition().equals(position)) {
				lightSources.remove(i);
				return;
			}
		}
	}

	public LightSource getLightSourceAtPosition(IntVector position2) {
		for (int i = 0; i < lightSources.size(); ++i) {
			if (lightSources.get(i).getPosition().equals(position2)) {
				return lightSources.get(i);
			}
		}
		return null;
	}
}
