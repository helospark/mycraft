package com.helospark.mycraft.mycraft.game;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import com.helospark.mycraft.mycraft.attributes.Model;
import com.helospark.mycraft.mycraft.mathutils.Frustum;
import com.helospark.mycraft.mycraft.render.Material;

public class OctreeBatchTask implements Callable<Map<Material, List<Model>>> {

	private Map<Material, List<Model>> models = new HashMap<>();
	private Chunk chunk;
	private int octreeIndex;
	private Frustum frustum;

	@Override
	public Map<Material, List<Model>> call() throws Exception {
		// Bug if thread ids are not continous
		// int threadId = (int) (Thread.currentThread().getId())
		// % GlobalParameters.NUMBER_OF_EXECUTOR_THREADS;

		chunk.getOctree().getRenderableModelsAtChildIndex(models, frustum, octreeIndex);

		return models;
	}

	public void setOctreeToCheck(Chunk chunk, int octreeIndex) {
		this.chunk = chunk;
		this.octreeIndex = octreeIndex;
	}

	public void setFrustum(Frustum frustum) {
		this.frustum = frustum;
	}

	public void clearResultList() {
		models.clear();
	}

	public void fillWithRootElements(Frustum frustum) {
		chunk.getOctree().fillWithRootElement(frustum, models);
	}
}