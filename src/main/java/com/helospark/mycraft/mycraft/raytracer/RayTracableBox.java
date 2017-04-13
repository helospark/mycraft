package com.helospark.mycraft.mycraft.raytracer;

import org.lwjgl.util.vector.Vector3f;

import com.helospark.mycraft.mycraft.game.Block;
import com.helospark.mycraft.mycraft.mathutils.BoundingBox;
import com.helospark.mycraft.mycraft.mathutils.Plane;

public class RayTracableBox {
	private BoundingBox boundingBox;
	private int ownerId;
	private int type;

	public RayTracableBox(BoundingBox boundingBox) {
		this.boundingBox = boundingBox;
	}

	public RayTracableBox(BoundingBox boundingBox, int ownerId, int type) {
		this(boundingBox);
		this.ownerId = ownerId;
	}

	public void rayTracing(Vector3f position, Vector3f direction,
			RayTracerResult rayTraceResult) {
		Plane plane = new Plane();
		RayTracerResult[] rayTraceResults = new RayTracerResult[Block.NUM_SIDES];
		initializeArray(rayTraceResults);
		checkLeftPlane(position, direction, plane, rayTraceResults);
		checkRightPlane(position, direction, plane, rayTraceResults);
		checkTopPlane(position, direction, plane, rayTraceResults);
		checkBottomPlane(position, direction, plane, rayTraceResults);
		checkNearPlane(position, direction, plane, rayTraceResults);
		checkFarPlane(position, direction, plane, rayTraceResults);
		int minimumRayTraceIndex = getMinimumRayTraceResultIndex(rayTraceResults);
		setResult(rayTraceResult, rayTraceResults, minimumRayTraceIndex);
	}

	private void setResult(RayTracerResult rayTraceResult,
			RayTracerResult[] rayTraceResults, int minimumRayTraceIndex) {
		if (minimumRayTraceIndex != -1) {
			rayTraceResult
					.initializeFrom(rayTraceResults[minimumRayTraceIndex]);
			rayTraceResult.setSide(minimumRayTraceIndex);
			rayTraceResult.setHasCollided(true);
		} else {
			rayTraceResult.setHasCollided(false);
		}
	}

	private void initializeArray(RayTracerResult[] rayTraceResults) {
		for (int i = 0; i < rayTraceResults.length; ++i) {
			rayTraceResults[i] = new RayTracerResult();
		}
	}

	private int getMinimumRayTraceResultIndex(RayTracerResult[] rayTraceResults) {
		int minimum = -1;

		for (int i = 0; i < rayTraceResults.length; ++i) {
			if (rayTraceResults[i].hasFound()) {
				if (minimum == -1) {
					minimum = i;
				} else {
					if (rayTraceResults[i].getDistance() < rayTraceResults[minimum]
							.getDistance()) {
						minimum = i;
					}
				}
			}
		}

		return minimum;
	}

	private void checkFarPlane(Vector3f rayPosition, Vector3f rayDirection,
			Plane plane, RayTracerResult[] rayTraceResults) {
		plane.change(new Vector3f(0.0f, 0.0f, 1.0f), boundingBox.getFar());
		plane.traceRays(rayPosition, rayDirection,
				rayTraceResults[Block.FAR_SIDE], boundingBox.getLeft(),
				boundingBox.getRight(), boundingBox.getBottom(),
				boundingBox.getTop());
	}

	private void checkNearPlane(Vector3f rayPosition, Vector3f rayDirection,
			Plane plane, RayTracerResult[] rayTraceResults) {
		plane.change(new Vector3f(0.0f, 0.0f, 1.0f), boundingBox.getNear());
		plane.traceRays(rayPosition, rayDirection,
				rayTraceResults[Block.NEAR_SIDE], boundingBox.getLeft(),
				boundingBox.getRight(), boundingBox.getBottom(),
				boundingBox.getTop());
	}

	private void checkBottomPlane(Vector3f rayPosition, Vector3f rayDirection,
			Plane plane, RayTracerResult[] rayTraceResults) {
		plane.change(new Vector3f(0.0f, 1.0f, 0.0f), boundingBox.getBottom());
		plane.traceRays(rayPosition, rayDirection,
				rayTraceResults[Block.BOTTOM_SIDE], boundingBox.getLeft(),
				boundingBox.getRight(), boundingBox.getNear(),
				boundingBox.getFar());
	}

	private void checkTopPlane(Vector3f rayPosition, Vector3f rayDirection,
			Plane plane, RayTracerResult[] rayTraceResults) {
		plane.change(new Vector3f(0.0f, 1.0f, 0.0f), boundingBox.getTop());
		plane.traceRays(rayPosition, rayDirection,
				rayTraceResults[Block.TOP_SIDE], boundingBox.getLeft(),
				boundingBox.getRight(), boundingBox.getNear(),
				boundingBox.getFar());
	}

	private void checkRightPlane(Vector3f rayPosition, Vector3f rayDirection,
			Plane plane, RayTracerResult[] rayTraceResults) {
		plane.change(new Vector3f(1.0f, 0.0f, 0.0f), boundingBox.getRight());
		plane.traceRays(rayPosition, rayDirection,
				rayTraceResults[Block.RIGHT_SIDE], boundingBox.getBottom(),
				boundingBox.getTop(), boundingBox.getNear(),
				boundingBox.getFar());
	}

	private void checkLeftPlane(Vector3f rayPosition, Vector3f rayDirection,
			Plane plane, RayTracerResult[] rayTraceResults) {
		plane.change(new Vector3f(1.0f, 0.0f, 0.0f), boundingBox.getLeft());
		plane.traceRays(rayPosition, rayDirection,
				rayTraceResults[Block.LEFT_SIDE], boundingBox.getBottom(),
				boundingBox.getTop(), boundingBox.getNear(),
				boundingBox.getFar());
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

}
