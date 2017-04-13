package com.helospark.mycraft.mycraft.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector3f;
import org.springframework.context.ApplicationContext;

import com.helospark.mycraft.mycraft.attributes.Model;
import com.helospark.mycraft.mycraft.mathutils.BoundingBox;
import com.helospark.mycraft.mycraft.mathutils.CullingResult;
import com.helospark.mycraft.mycraft.mathutils.Frustum;
import com.helospark.mycraft.mycraft.mathutils.IntVector;
import com.helospark.mycraft.mycraft.mathutils.VectorMathUtils;
import com.helospark.mycraft.mycraft.render.Material;
import com.helospark.mycraft.mycraft.services.GlobalParameters;
import com.helospark.mycraft.mycraft.singleton.Singleton;

public class OctreeChunk {
	public static final int OCTREE_CHILDREN_NUMBER = 8;
	public static final int OCTREE_MIN_SIZE = 3;
	List<BlockSide> visibleBlocks = new ArrayList<BlockSide>();
	OctreeChunk[] children;
	BoundingBox boundingBox;
	boolean[] hasChildren;
	GlobalParameters globalParameters;
	// cache
	List<Model> nodes = new ArrayList<>();

	public OctreeChunk(float x, float y, float z, float size) {
		float sizePerTwo = size / 2;
		if (size > Block.SIZE * OCTREE_MIN_SIZE) {
			children = new OctreeChunk[OCTREE_CHILDREN_NUMBER];
			hasChildren = new boolean[OCTREE_CHILDREN_NUMBER];
			children[0] = new OctreeChunk(x + sizePerTwo, y, z, size / 2);
			children[1] = new OctreeChunk(x, y + sizePerTwo, z, size / 2);
			children[2] = new OctreeChunk(x, y, z + sizePerTwo, size / 2);
			children[3] = new OctreeChunk(x + sizePerTwo, y + sizePerTwo, z, size / 2);
			children[4] = new OctreeChunk(x + sizePerTwo, y, z + sizePerTwo, size / 2);
			children[5] = new OctreeChunk(x, y + sizePerTwo, z + sizePerTwo, size / 2);
			children[6] = new OctreeChunk(x + sizePerTwo, y + sizePerTwo, z + sizePerTwo, size / 2);
			children[7] = new OctreeChunk(x, y, z, size / 2);
		}
		ApplicationContext context = Singleton.getInstance().getContext();
		globalParameters = context.getBean(GlobalParameters.class);
		boundingBox = BoundingBox.fromTwoPoints(new Vector3f(x, y, z), new Vector3f(x + size, y + size, z + size));
	}

	public void addBlock(BlockSide side) {
		if (children == null) {
			visibleBlocks.add(side);
		} else {
			boolean added = false;
			for (int i = 0; i < OCTREE_CHILDREN_NUMBER; ++i) {
				if (children[i].getBoundingBox().containsBox(side.getNode().calculateBoundingBox()) != CullingResult.FULLY_OUT) {
					children[i].addBlock(side);
					added = true;
					hasChildren[i] = true;
					break;
				}
			}
			if (!added) {
				visibleBlocks.add(side);
			}
		}
	}

	public void getVisibleObjects(Frustum frustum, Map<Material, List<Model>> visibleBlocksResult) {
		if (visibleBlocks.size() > 0) {
			addVisibleEntities(visibleBlocksResult, frustum.getPosition());
		}
		if (children != null) {
			for (int i = 0; i < OCTREE_CHILDREN_NUMBER; ++i) {
				if (hasChildren[i] && frustum.containsBox(children[i].getBoundingBox()) != CullingResult.FULLY_OUT) {
					children[i].getVisibleObjects(frustum, visibleBlocksResult);
				}
			}
		}
	}

	// Not thread safe
	private void addVisibleEntities(Map<Material, List<Model>> visibleBlockSides, Vector3f cameraPosition) {
		for (BlockSide visibleBlockSide : visibleBlocks) {
			if (VectorMathUtils.distanceSquareBetween(visibleBlockSide.getPosition(), cameraPosition) <= globalParameters.fogFarDistanceSquared) {
				List<Model> modelList = visibleBlockSides.get(visibleBlockSide.getMaterial());
				if (modelList == null) {
					nodes.clear();
					nodes.add(visibleBlockSide.getNode().getModel());
					visibleBlockSides.put(visibleBlockSide.getMaterial(), nodes);
				} else {
					modelList.add(visibleBlockSide.getNode().getModel());
				}
			}
		}
	}

	public BoundingBox getBoundingBox() {
		return boundingBox;
	}

	public boolean removeVisibleBlock(IntVector intPosition, BoundingBox blockBoundingBox) {
		int deletedNodes = 0;
		for (int i = 0; i < visibleBlocks.size(); ++i) {
			if (visibleBlocks.get(i).getIntPosition().equals(intPosition)) {
				visibleBlocks.remove(i);
				--i;
				deletedNodes++;
				if (deletedNodes == Block.NUM_SIDES) {
					// return true;
				}
			}
		}
		if (children != null) {
			for (int i = 0; i < OCTREE_CHILDREN_NUMBER; ++i) {
				if (children[i].getBoundingBox().containsBox(blockBoundingBox) != CullingResult.FULLY_OUT) {
					if (children[i].removeVisibleBlock(intPosition, blockBoundingBox)) {
						// return true;
					}
				}
			}
		}
		return false;
	}

	public void getVisibleBlockSides(IntVector intPosition, BoundingBox blockBoundingBox, List<BlockSide> result) {
		for (int i = 0; i < visibleBlocks.size(); ++i) {
			if (visibleBlocks.get(i).getIntPosition().equals(intPosition)) {
				result.add(visibleBlocks.get(i));
			}
		}
		if (children != null) {
			for (int i = 0; i < OCTREE_CHILDREN_NUMBER; ++i) {
				if (children[i].getBoundingBox().containsBox(blockBoundingBox) != CullingResult.FULLY_OUT) {
					children[i].getVisibleBlockSides(intPosition, blockBoundingBox, result);
				}
			}
		}
	}

	public void fillWithRootElement(Frustum frustum, Map<Material, List<Model>> visibleBlockSides) {
		addVisibleEntities(visibleBlockSides, frustum.getPosition());
	}

	public void getRenderableModelsAtChildIndex(Map<Material, List<Model>> models, Frustum frustum, int octreeIndex) {
		if (children != null) {
			if (hasChildren[octreeIndex] && frustum.containsBox(children[octreeIndex].getBoundingBox()) != CullingResult.FULLY_OUT) {
				children[octreeIndex].getVisibleObjects(frustum, models);
			}
		}
	}
}
