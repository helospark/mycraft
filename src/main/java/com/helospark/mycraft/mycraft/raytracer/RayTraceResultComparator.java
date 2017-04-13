package com.helospark.mycraft.mycraft.raytracer;

import java.util.Comparator;

public class RayTraceResultComparator implements Comparator<RayTracerResult> {

	@Override
	public int compare(RayTracerResult result1, RayTracerResult result2) {
		double result = result1.getDistance() - result2.getDistance();
		if (Math.abs(result) < 0.000001)
			return 0;

		while ((int) result == 0) {
			result *= 10.0;
		}
		return (int) result;
	}
}
