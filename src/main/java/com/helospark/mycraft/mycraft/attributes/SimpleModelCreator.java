package com.helospark.mycraft.mycraft.attributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.springframework.stereotype.Service;

import com.helospark.mycraft.mycraft.game.Block;
import com.helospark.mycraft.mycraft.game.RenderableBlock;
import com.helospark.mycraft.mycraft.mathutils.IntVector;

@Service
public class SimpleModelCreator {

	private static final float DELTA = 0.00001f;

	public class ModelParameters {
		boolean generateNormals;
		boolean generateTangents;
		boolean generateColors;
		boolean generateUvs;

		public ModelParameters() {

		}

		public void setGenerateNormals(boolean generateNormals) {
			this.generateNormals = generateNormals;
		}

		public void setGenerateTangents(boolean generateTangents) {
			this.generateTangents = generateTangents;
		}

		public void setGenerateColors(boolean generateColors) {
			this.generateColors = generateColors;
		}

		public void setGenerateUvs(boolean generateUvs) {
			this.generateUvs = generateUvs;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + (generateColors ? 1231 : 1237);
			result = prime * result + (generateNormals ? 1231 : 1237);
			result = prime * result + (generateTangents ? 1231 : 1237);
			result = prime * result + (generateUvs ? 1231 : 1237);
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
			ModelParameters other = (ModelParameters) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (generateColors != other.generateColors)
				return false;
			if (generateNormals != other.generateNormals)
				return false;
			if (generateTangents != other.generateTangents)
				return false;
			if (generateUvs != other.generateUvs)
				return false;
			return true;
		}

		private SimpleModelCreator getOuterType() {
			return SimpleModelCreator.this;
		}

	}

	Map<String, Set<ModelParameters>> createObjectList = new HashMap<>();

	public static Model getTriangle(ModelParameters parameters) {
		// Set<ModelParameters> foundParameters =
		// createObjectList.get("triangle");
		if (parameters == null) {
			Model model = new Model();
			model.createPositionAttribute();
			model.createNormalAttribute();
			model.createColorAttribute();
			model.addPosition(new Vector3f(-1.0f, -1.0f, -1.0f));
			model.addNormal(new Vector3f(0.0f, 0.0f, 1.0f));
			model.addColor(new Vector3f(1.0f, 0.0f, 0.0f));

			model.addPosition(new Vector3f(1.0f, -1.0f, -1.0f));
			model.addNormal(new Vector3f(0.0f, 0.0f, 1.0f));
			model.addColor(new Vector3f(1.0f, 0.0f, 0.0f));

			model.addPosition(new Vector3f(0.0f, 1.0f, -1.0f));
			model.addNormal(new Vector3f(0.0f, 0.0f, 1.0f));
			model.addColor(new Vector3f(1.0f, 0.0f, 0.0f));

			model.addIndices(0, 1, 2);

			model.prepare();

			return model;
		} else {
			throw new RuntimeException("Not implemented");
		}
	}

	public Model getQuad() {
		throw new RuntimeException("Not implemented");
	}

	public Model getCube() {
		throw new RuntimeException("Not implemented");
	}

	public static Model getRightCubeSide() {
		Model model = new Model();
		model.createPositionAttribute();
		model.createUvAttribute();
		model.createLight();

		model.addPosition(new Vector3f(1.0f, 1.0f, 1.0f));
		model.addPosition(new Vector3f(1.0f, 0.0f, 1.0f));
		model.addPosition(new Vector3f(1.0f, 0.0f, 0.0f));
		model.addPosition(new Vector3f(1.0f, 1.0f, 0.0f));

		model.addUv(new Vector2f(0.0f, 0.0f));
		model.addUv(new Vector2f(1.0f, 0.0f));
		model.addUv(new Vector2f(1.0f, 1.0f));
		model.addUv(new Vector2f(0.0f, 1.0f));

		model.addLight(1.0f);
		model.addLight(1.0f);
		model.addLight(1.0f);
		model.addLight(1.0f);

		model.addIndices(0, 1, 2, 0, 2, 3);

		return model;
	}

	public static Model getFarCubeSide() {
		Model model = new Model();
		model.createPositionAttribute();
		model.createUvAttribute();
		model.createLight();

		model.addPosition(new Vector3f(1.0f, 1.0f, 1.0f));
		model.addPosition(new Vector3f(0.0f, 1.0f, 1.0f));
		model.addPosition(new Vector3f(0.0f, 0.0f, 1.0f));
		model.addPosition(new Vector3f(1.0f, 0.0f, 1.0f));

		model.addUv(new Vector2f(0.0f, 0.0f));
		model.addUv(new Vector2f(1.0f, 0.0f));
		model.addUv(new Vector2f(1.0f, 1.0f));
		model.addUv(new Vector2f(0.0f, 1.0f));

		model.addLight(1.0f);
		model.addLight(1.0f);
		model.addLight(1.0f);
		model.addLight(1.0f);

		model.addIndices(0, 1, 2, 0, 2, 3);

		return model;
	}

	public static Model getLeftCubeSide() {
		Model model = new Model();
		model.createPositionAttribute();
		model.createUvAttribute();
		model.createLight();

		model.addPosition(new Vector3f(0.0f, 1.0f, 1.0f));
		model.addPosition(new Vector3f(0.0f, 0.0f, 1.0f));
		model.addPosition(new Vector3f(0.0f, 0.0f, 0.0f));
		model.addPosition(new Vector3f(0.0f, 1.0f, 0.0f));

		model.addUv(new Vector2f(0.0f, 0.0f));
		model.addUv(new Vector2f(1.0f, 0.0f));
		model.addUv(new Vector2f(1.0f, 1.0f));
		model.addUv(new Vector2f(0.0f, 1.0f));

		model.addLight(1.0f);
		model.addLight(1.0f);
		model.addLight(1.0f);
		model.addLight(1.0f);

		model.addIndices(0, 2, 1, 2, 0, 3);

		return model;
	}

	public static Model getBottomCubeSide() {
		Model model = new Model();
		model.createPositionAttribute();
		model.createUvAttribute();
		model.createLight();

		model.addPosition(new Vector3f(1.0f, 0.0f, 1.0f));
		model.addPosition(new Vector3f(0.0f, 0.0f, 1.0f));
		model.addPosition(new Vector3f(0.0f, 0.0f, 0.0f));
		model.addPosition(new Vector3f(1.0f, 0.0f, 0.0f));

		model.addUv(new Vector2f(0.0f, 0.0f));
		model.addUv(new Vector2f(1.0f, 0.0f));
		model.addUv(new Vector2f(1.0f, 1.0f));
		model.addUv(new Vector2f(0.0f, 1.0f));

		model.addLight(1.0f);
		model.addLight(1.0f);
		model.addLight(1.0f);
		model.addLight(1.0f);

		model.addIndices(0, 1, 2, 0, 2, 3);

		return model;
	}

	public static Model getNearCubeSide() {
		Model model = new Model();
		model.createPositionAttribute();
		model.createUvAttribute();
		model.createLight();

		model.addPosition(new Vector3f(1.0f, 1.0f, 0.0f));
		model.addPosition(new Vector3f(0.0f, 1.0f, 0.0f));
		model.addPosition(new Vector3f(0.0f, 0.0f, 0.0f));
		model.addPosition(new Vector3f(1.0f, 0.0f, 0.0f));

		model.addUv(new Vector2f(0.0f, 0.0f));
		model.addUv(new Vector2f(1.0f, 0.0f));
		model.addUv(new Vector2f(1.0f, 1.0f));
		model.addUv(new Vector2f(0.0f, 1.0f));

		model.addLight(1.0f);
		model.addLight(1.0f);
		model.addLight(1.0f);
		model.addLight(1.0f);

		model.addIndices(0, 2, 1, 2, 0, 3);

		return model;
	}

	public static Model getXYPlaneNormal() {
		Model model = new Model();
		model.createPositionAttribute();
		model.createUvAttribute();
		model.createLight();

		model.addPosition(new Vector3f(1.0f, 1.0f, 0.5f));
		model.addPosition(new Vector3f(0.0f, 1.0f, 0.5f));
		model.addPosition(new Vector3f(0.0f, 0.0f, 0.5f));
		model.addPosition(new Vector3f(1.0f, 0.0f, 0.5f));

		model.addUv(new Vector2f(0.0f, 0.0f));
		model.addUv(new Vector2f(1.0f, 0.0f));
		model.addUv(new Vector2f(1.0f, 1.0f));
		model.addUv(new Vector2f(0.0f, 1.0f));

		model.addLight(1.0f);
		model.addLight(1.0f);
		model.addLight(1.0f);
		model.addLight(1.0f);

		model.addIndices(0, 2, 1, 2, 0, 3);

		return model;
	}

	public static Model getXYPlaneInverted() {
		Model model = new Model();
		model.createPositionAttribute();
		model.createUvAttribute();
		model.createLight();

		model.addPosition(new Vector3f(1.0f, 1.0f, 0.5f));
		model.addPosition(new Vector3f(0.0f, 1.0f, 0.5f));
		model.addPosition(new Vector3f(0.0f, 0.0f, 0.5f));
		model.addPosition(new Vector3f(1.0f, 0.0f, 0.5f));

		model.addUv(new Vector2f(0.0f, 0.0f));
		model.addUv(new Vector2f(1.0f, 0.0f));
		model.addUv(new Vector2f(1.0f, 1.0f));
		model.addUv(new Vector2f(0.0f, 1.0f));

		model.addLight(1.0f);
		model.addLight(1.0f);
		model.addLight(1.0f);
		model.addLight(1.0f);

		model.addIndices(0, 1, 2, 2, 3, 0);

		return model;
	}

	public static Model getTopCubeSide() {
		Model model = new Model();
		model.createPositionAttribute();
		model.createUvAttribute();
		model.createLight();

		model.addPosition(new Vector3f(1.0f, 1.0f, 1.0f));
		model.addPosition(new Vector3f(0.0f, 1.0f, 1.0f));
		model.addPosition(new Vector3f(0.0f, 1.0f, 0.0f));
		model.addPosition(new Vector3f(1.0f, 1.0f, 0.0f));

		model.addUv(new Vector2f(0.0f, 0.0f));
		model.addUv(new Vector2f(1.0f, 0.0f));
		model.addUv(new Vector2f(1.0f, 1.0f));
		model.addUv(new Vector2f(0.0f, 1.0f));

		model.addLight(1.0f);
		model.addLight(1.0f);
		model.addLight(1.0f);
		model.addLight(1.0f);

		model.addIndices(0, 2, 1, 2, 0, 3);

		return model;
	}

	public static Model getUnitQuad() {
		Model model = new Model();
		model.createPositionAttribute();
		model.createColorAttribute();
		model.createUvAttribute();

		model.addPosition(new Vector3f(1.0f, 1.0f, 0.0f));
		model.addPosition(new Vector3f(0.0f, 1.0f, 0.0f));
		model.addPosition(new Vector3f(0.0f, 0.0f, 0.0f));
		model.addPosition(new Vector3f(1.0f, 0.0f, 0.0f));

		model.addUv(new Vector2f(0.0f, 0.0f));
		model.addUv(new Vector2f(1.0f, 0.0f));
		model.addUv(new Vector2f(1.0f, 1.0f));
		model.addUv(new Vector2f(0.0f, 1.0f));

		model.addColor(new Vector3f(1.0f, 0.0f, 0.0f));
		model.addColor(new Vector3f(0.0f, 1.0f, 0.0f));
		model.addColor(new Vector3f(0.0f, 0.0f, 1.0f));
		model.addColor(new Vector3f(1.0f, 0.0f, 1.0f));

		model.addIndices(0, 2, 1, 2, 0, 3);

		return model;
	}

	public Model createModelFromPatternAndMap(int[][] pattern,
			Map<Integer, Vector3f> colors, float size, IntVector center) {
		Model model = new Model();

		model.createPositionAttribute();
		model.createColorAttribute();

		IntVector intPosition = new IntVector();
		int addedVertices = 0;
		for (int y = 0; y < pattern.length; ++y) {
			for (int x = 0; x < pattern[y].length; ++x) {
				if (pattern[y][x] != 0) {

					intPosition.x = x - center.x;
					intPosition.y = y - center.y;
					for (int side = 0; side < Block.NUM_SIDES; ++side) {
						IntVector sideVector = RenderableBlock
								.intVectorFromSide(side);
						if (shouldPlaceFace(pattern, x, y, sideVector)) {
							Vector3f color = colors.get(pattern[y][x]);
							addedVertices += addFace(model, size, color,
									intPosition, sideVector, addedVertices);
						}
					}
				}
			}
		}
		return model;

	}

	private int addFace(Model model, float size, Vector3f color,
			IntVector intPosition, IntVector sideVector, int addedVertices) {
		Vector3f[] vectors = generateQuadVectors(sideVector);

		for (int i = 0; i < vectors.length; ++i) {
			vectors[i].scale(size);
			vectors[i].translate(intPosition.x * size, intPosition.y * size,
					intPosition.z * size);
			model.addPosition(vectors[i]);
			model.addColor(color);
		}
		model.addIndices(addedVertices, addedVertices + 2, addedVertices + 1,
				addedVertices + 2, addedVertices + 0, addedVertices + 3);

		return vectors.length;
	}

	private Vector3f[] generateQuadVectors(IntVector sideVector) {
		Vector3f[] vectors = new Vector3f[4];

		float nonZeroComponent = sideVector.x;
		int positionOfNonZeroComponent = 0;
		if (Math.abs(sideVector.y) > DELTA) {
			positionOfNonZeroComponent = 1;
			nonZeroComponent = sideVector.y;
		}
		if (Math.abs(sideVector.z) > DELTA) {
			positionOfNonZeroComponent = 2;
			nonZeroComponent = sideVector.z;
		}
		if (nonZeroComponent < 0.0f) {
			nonZeroComponent = 0.0f;
		}

		vectors[0] = generateVectorWithZeroAtNonZero(0, 0,
				positionOfNonZeroComponent, nonZeroComponent);
		vectors[1] = generateVectorWithZeroAtNonZero(0, 1,
				positionOfNonZeroComponent, nonZeroComponent);
		vectors[2] = generateVectorWithZeroAtNonZero(1, 1,
				positionOfNonZeroComponent, nonZeroComponent);
		vectors[3] = generateVectorWithZeroAtNonZero(1, 0,
				positionOfNonZeroComponent, nonZeroComponent);
		return vectors;
	}

	private Vector3f generateVectorWithZeroAtNonZero(int i, int j,
			int positionOfNonZeroComponent, float nonZeroComponent) {
		Vector3f vector = new Vector3f();
		if (positionOfNonZeroComponent == 0) {
			vector.x = nonZeroComponent;
			vector.y = i;
			vector.z = j;
		} else if (positionOfNonZeroComponent == 1) {
			vector.x = i;
			vector.y = nonZeroComponent;
			vector.z = j;
		} else if (positionOfNonZeroComponent == 2) {
			vector.x = i;
			vector.y = j;
			vector.z = nonZeroComponent;
		}
		return vector;
	}

	private boolean shouldPlaceFace(int[][] pattern, int x, int y,
			IntVector sideVector) {
		return true;
		// if (Math.abs(sideVector.z) > DELTA) {
		// return true;
		// }
		// int newX = x + sideVector.x;
		// int newY = y + sideVector.y;
		// if (newX < 0 || newX >= pattern[0].length || newY < 0
		// || newY >= pattern.length) {
		// return true;
		// }
		//
		// return pattern[newY][newX] != 0;
	}
}
