package com.helospark.mycraft.mycraft.attributes;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.springframework.context.ApplicationContext;

import com.helospark.mycraft.mycraft.mathutils.VectorMathUtils;
import com.helospark.mycraft.mycraft.shader.Shader;
import com.helospark.mycraft.mycraft.singleton.Singleton;

public class Model {

	private static final String IN_COLOR = "inColor";
	private static final String IN_UV = "inUv";
	private static final String IN_TANGENT = "inTangent";
	private static final String IN_NORMAL = "inNormal";
	private static final String IN_POSITION = "inPosition";
	private static final String IN_LIGHT = "inLight";
	private Map<String, AttributeObject> attributes = new HashMap<>();
	private List<Integer> indices = new ArrayList<>();
	protected FloatBuffer buffer;
	protected IntBuffer indexBuffer;

	private int vao = 0;

	protected int bufferId;
	protected int vboIndexBuffer;

	private int vertexCount = 0;
	private boolean generateNormals = false, generateTangents = false, generateColors = false,
			smoothNormals = false;

	protected List<Integer> vertexAttribArrays = new ArrayList<>();

	// for batching reset to original
	private Map<String, AttributeObject> originalAttributes = new HashMap<>();
	private List<Integer> originalIndices = new ArrayList<>();

	Shader shader;

	public Model() {

	}

	public Model(Model initFrom) {
		ApplicationContext context = Singleton.getInstance().getContext();
		this.attributes = new HashMap<String, AttributeObject>();
		for (Map.Entry<String, AttributeObject> attribute : initFrom.attributes.entrySet()) {
			this.attributes.put(attribute.getKey(), attribute.getValue().copy());
		}
		this.indices = new ArrayList<Integer>();
		for (int index : initFrom.indices) {
			indices.add(index);
		}
		this.vertexCount = initFrom.vertexCount;
	}

	public void prepare() {
		if (generateNormals) {
			generateNormals();
		}
		if (generateTangents) {
			generateTangents();
		}
		if (generateColors) {
			generateColors();
		}
		if (smoothNormals) {
			smoothNormals();
		}

		for (Map.Entry<String, AttributeObject> attribute : attributes.entrySet()) {
			this.originalAttributes.put(attribute.getKey(), attribute.getValue().copy());
		}

		for (int index : indices) {
			originalIndices.add(index);
		}
	}

	private void generateNormals() {
		AttributeObject normalObject = attributes.get(IN_NORMAL);
		normalObject.empty();
		AttributeObject positionObject = attributes.get(IN_POSITION);
		Vector3f v1 = new Vector3f();
		Vector3f v2 = new Vector3f();
		Vector3f v3 = new Vector3f();
		for (int i = 0; i < indices.size(); i += 3) {
			extractVectorFromIndex(positionObject, indices.get(i + 0), v1);
			extractVectorFromIndex(positionObject, indices.get(i + 1), v2);
			extractVectorFromIndex(positionObject, indices.get(i + 2), v3);
			Vector3f normal = getNormalForTriangle(v1, v2, v3);
			addNormal(normal);
		}
	}

	private Vector3f getNormalForTriangle(Vector3f v1, Vector3f v2, Vector3f v3) {
		Vector3f direction1 = new Vector3f();
		Vector3f direction2 = new Vector3f();
		Vector3f.sub(v2, v1, direction1);
		Vector3f.sub(v3, v1, direction2);
		Vector3f cross = new Vector3f();
		Vector3f.cross(direction1, direction2, cross);
		return cross;
	}

	private void extractVectorFromIndex(AttributeObject attributeObject, int index, Vector3f vertex) {
		vertex.x = attributeObject.get(index, 0);
		vertex.y = attributeObject.get(index, 1);
		vertex.z = attributeObject.get(index, 2);
	}

	private void generateColors() {
		Random random = new Random();
		for (int i = 0; i < vertexCount; ++i) {
			addColor(new Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat()));
		}
	}

	private void generateTangents() {

	}

	private void smoothNormals() {
		AttributeObject normalObject = attributes.get(IN_NORMAL);
		AttributeObject positionObject = attributes.get(IN_POSITION);
		Vector3f[] normals = new Vector3f[positionObject.getNumberOfDatas()];
		for (Integer index : indices) {
			Vector3f currentNormal = new Vector3f(normalObject.get(index, 0), normalObject.get(
					index, 1), normalObject.get(index, 2));
			Vector3f.add(normals[index], currentNormal, normals[index]);
		}
		for (int i = 0; i < normals.length; ++i) {
			normals[i].normalise();
		}
		normalObject.empty();
		for (Vector3f normal : normals) {
			float[] normalComponents = { (float) normal.x, (float) normal.y, (float) normal.z };
			normalObject.setData(normalComponents);
		}
	}

	public int getVaoForProgram(Shader shader) {
		shader.useProgram();
		vao = GL30.glGenVertexArrays();
		int fullSize = calculateFullSize();
		createVao(fullSize);
		createIbo(indices.size());
		vertexCount = getVertexCount();
		fillUpEmptyPlaces();
		fillBuffer();

		GL30.glBindVertexArray(vao);
		createVbo(buffer);
		fillUpVao(shader);
		GL30.glBindVertexArray(0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

		int errorCode = GL11.glGetError();
		if (errorCode != 0) {
			// System.out.println(GLU.gluErrorString(errorCode));
		}

		return vao;
	}

	protected void createIbo(int size) {
		indexBuffer = BufferUtils.createIntBuffer(size);
	}

	protected void createVao(int fullSize) {
		buffer = BufferUtils.createFloatBuffer(fullSize);
	}

	private int calculateFullSize() {
		// TODO: these can be optimized out
		int stride = calculateNumberOfFloatsPerVertex();
		int vertexCount = getVertexCount();
		return stride * vertexCount;
	}

	private void fillUpEmptyPlaces() {
		for (Map.Entry<String, AttributeObject> entry : attributes.entrySet()) {
			AttributeObject currentAttributeObject = entry.getValue();
			if (currentAttributeObject.getNumberOfDatas() < vertexCount) {
				for (int i = currentAttributeObject.getNumberOfDatas(); i < vertexCount; ++i) {
					float[] tmp = new float[currentAttributeObject.getLength()];
					for (int j = 0; j < tmp.length; ++j) {
						tmp[j] = 0.0f;
					}
					currentAttributeObject.setData(tmp);
				}
			}
		}
	}

	public int getVertexCount() {
		int max = 0;
		for (Map.Entry<String, AttributeObject> entry : attributes.entrySet()) {
			int currentDataCount = entry.getValue().getNumberOfDatas();
			if (currentDataCount > max) {
				max = currentDataCount;
			}
		}
		return max;
	}

	private void fillBuffer() {
		for (int i = 0; i < vertexCount; ++i) {
			for (Map.Entry<String, AttributeObject> entry : attributes.entrySet()) {
				entry.getValue().writeToFloatBuffer(buffer, i);
			}
		}
		for (int i = 0; i < indices.size(); ++i) {
			indexBuffer.put(indices.get(i));
		}
	}

	private void createVbo(FloatBuffer buffer) {
		setupVertexBuffer(buffer);
		setupIndexBuffer();
	}

	protected void setupVertexBuffer(FloatBuffer buffer) {
		buffer.flip();
		bufferId = GL15.glGenBuffers();

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, bufferId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
	}

	protected void setupIndexBuffer() {
		indexBuffer.flip();
		vboIndexBuffer = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboIndexBuffer);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL15.GL_STATIC_DRAW);
	}

	protected void fillUpVao(Shader shader) {
		vertexAttribArrays.clear();
		int stride = calculateStride();
		int position = 0;
		shader.useProgram();
		for (Map.Entry<String, AttributeObject> entry : attributes.entrySet()) {
			int vboId = shader.getAttribLocation(entry.getKey());
			if (vboId != -1) {
				GL20.glEnableVertexAttribArray(vboId);
				vertexAttribArrays.add(vboId);
				GL20.glVertexAttribPointer(vboId, entry.getValue().getLength(), GL11.GL_FLOAT,
						false, stride, position);
			}
			position += entry.getValue().getLength() * Float.BYTES;
		}
	}

	private int calculateStride() {
		if (attributes.size() == 1) {
			return 0;
		}
		int result = 0;
		for (Map.Entry<String, AttributeObject> entry : attributes.entrySet()) {
			result += entry.getValue().getLength() * Float.BYTES;
		}
		return result;
	}

	public int calculateNumberOfFloatsPerVertex() {
		int result = 0;
		for (Map.Entry<String, AttributeObject> entry : attributes.entrySet()) {
			result += entry.getValue().getLength();
		}
		return result;
	}

	public void createNewAttribute(String name, int type, int length) {
		if (attributes.containsKey(name)) {
			throw new RuntimeException("The given attribute name already exists");
		}

		attributes.put(name, new AttributeObject(type, length));
	}

	public void addAttribute(String name, float... data) {
		AttributeObject attributeObject = attributes.get(name);
		if (attributeObject == null) {
			throw new RuntimeException("The given attribute name does not yet exists");
		}
		attributeObject.setData(data);
	}

	public void addPosition(Vector3f vector) {
		addAttribute(IN_POSITION, (float) vector.x, (float) vector.y, (float) vector.z);
	}

	public void addNormal(Vector3f vector) {
		addAttribute(IN_NORMAL, (float) vector.x, (float) vector.y, (float) vector.z);
	}

	public void addTangent(Vector3f vector) {
		addAttribute(IN_TANGENT, (float) vector.x, (float) vector.y, (float) vector.z);
	}

	public void addUv(Vector2f vector) {
		addAttribute(IN_UV, (float) vector.x, (float) vector.y);
	}

	public void addColor(Vector3f vector) {
		addAttribute(IN_COLOR, (float) vector.x, (float) vector.y, (float) vector.z);
	}

	public void addLight(float light) {
		addAttribute(IN_LIGHT, light);
	}

	public void createPositionAttribute() {
		createNewAttribute(IN_POSITION, GL11.GL_FLOAT, 3);
	}

	public void createNormalAttribute() {
		createNewAttribute(IN_NORMAL, GL11.GL_FLOAT, 3);
	}

	public void createUvAttribute() {
		createNewAttribute(IN_UV, GL11.GL_FLOAT, 2);
	}

	public void createLight() {
		createNewAttribute(IN_LIGHT, GL11.GL_FLOAT, 1);
	}

	public void createColorAttribute() {
		createNewAttribute(IN_COLOR, GL11.GL_FLOAT, 3);
	}

	public Shader getShader() {
		return shader;
	}

	public int getIndexCount() {
		return indices.size();
	}

	public void addIndices(Integer... indices) {
		this.indices.addAll(Arrays.asList(indices));
	}

	public void setShader(Shader shader) {
		this.shader = shader;
	}

	public void bindVertexAttribArrays() {
		for (Integer vertexArrayIndex : vertexAttribArrays) {
			GL20.glEnableVertexAttribArray(vertexArrayIndex);
		}
	}

	public List<Integer> getVertexAttribArrays() {
		return vertexAttribArrays;
	}

	public int getIndexBuffer() {
		return vboIndexBuffer;
	}

	public int getVertexSize() {
		return calculateNumberOfFloatsPerVertex();
	}

	public int addVertexDataToArray(float[] buffer, int index) {
		int writtenData = 0;
		for (int i = 0; i < vertexCount; ++i) {
			for (Map.Entry<String, AttributeObject> entry : attributes.entrySet()) {
				int writtenVertexData = entry.getValue().writeToBuffer(buffer, index, i);
				index += writtenVertexData;
				writtenData += writtenVertexData;
			}
		}
		return writtenData;
	}

	public int addIndexDataToArray(int[] buffer, int index, int vertexCount) {
		for (int i : indices) {
			buffer[index++] = vertexCount + i;
		}
		return indices.size();
	}

	public void applyTranslation(Vector3f position) {
		AttributeObject attributeObject = attributes.get(IN_POSITION);
		for (int i = 0; i < attributeObject.getNumberOfVertices(); ++i) {
			attributeObject.set(i, 0, attributeObject.get(i, 0) + position.x);
			attributeObject.set(i, 1, attributeObject.get(i, 1) + position.y);
			attributeObject.set(i, 2, attributeObject.get(i, 2) + position.z);
		}
	}

	public void applyColor(Vector3f color) {
		AttributeObject attributeObject = attributes.get(IN_COLOR);
		for (int i = 0; i < attributeObject.getNumberOfVertices(); ++i) {
			attributeObject.set(i, 0, color.x);
			attributeObject.set(i, 1, color.y);
			attributeObject.set(i, 2, color.z);
		}
	}

	public void applyUv(int side, float u, float v) {
		AttributeObject attributeObject = attributes.get(IN_UV);
		attributeObject.set(side, 0, u);
		attributeObject.set(side, 1, v);

	}

	public void applyTranslation(Vector3f position, float scale) {
		AttributeObject attributeObject = attributes.get(IN_POSITION);
		for (int i = 0; i < attributeObject.getNumberOfVertices(); ++i) {
			attributeObject.set(i, 0, attributeObject.get(i, 0) * scale + position.x);
			attributeObject.set(i, 1, attributeObject.get(i, 1) * scale + position.y);
			attributeObject.set(i, 2, attributeObject.get(i, 2) * scale + position.z);
		}
	}

	public void applyTranslation(Vector3f position, Vector3f scale) {
		AttributeObject attributeObject = attributes.get(IN_POSITION);
		for (int i = 0; i < attributeObject.getNumberOfVertices(); ++i) {
			attributeObject.set(i, 0, attributeObject.get(i, 0) * scale.x + position.x);
			attributeObject.set(i, 1, attributeObject.get(i, 1) * scale.y + position.y);
			attributeObject.set(i, 2, attributeObject.get(i, 2) * scale.z + position.z);
		}
	}

	public void applyLight(float light) {
		AttributeObject attributeObject = attributes.get(IN_LIGHT);
		for (int i = 0; i < attributeObject.getNumberOfVertices(); ++i) {
			attributeObject.set(i, 0, light);
		}
	}

	public void copyFrom(Model initFrom) {
		attributes.clear();
		for (Map.Entry<String, AttributeObject> attribute : initFrom.attributes.entrySet()) {
			this.attributes.put(attribute.getKey(), attribute.getValue().copy());
		}
		this.indices.clear();
		for (int index : initFrom.indices) {
			indices.add(index);
		}
		this.vertexCount = initFrom.vertexCount;
	}

	public void resetToOriginal() {

	}

	public boolean isCloserToCamera(Model otherModel, Vector3f cameraPosition) {
		AttributeObject attributeObject = attributes.get(IN_POSITION);
		AttributeObject otherAttributeObject = otherModel.attributes.get(IN_POSITION);

		float x = attributeObject.get(0, 0);
		float y = attributeObject.get(0, 1);
		float z = attributeObject.get(0, 2);

		float ox = otherAttributeObject.get(0, 0);
		float oy = otherAttributeObject.get(0, 1);
		float oz = otherAttributeObject.get(0, 2);

		float distance1 = VectorMathUtils.distanceSquareBetween(cameraPosition, new Vector3f(x, y,
				z));
		float distance2 = VectorMathUtils.distanceSquareBetween(cameraPosition, new Vector3f(ox,
				oy, oz));

		return distance1 < distance2;
	}

	public void addLight(int index, float amount) {
		AttributeObject attributeObject = attributes.get(IN_LIGHT);
		attributeObject.add(index, 0, amount);
	}

	public Vector3f[] getPositionArray() {
		AttributeObject attributeObject = attributes.get(IN_POSITION);
		Vector3f[] result = new Vector3f[attributeObject.getNumberOfVertices()];
		for (int i = 0; i < attributeObject.getNumberOfVertices(); ++i) {
			result[i] = new Vector3f();
			result[i].x = attributeObject.get(i, 0);
			result[i].y = attributeObject.get(i, 1);
			result[i].z = attributeObject.get(i, 2);
		}
		return result;
	}

	public float getLight(int index) {
		AttributeObject attributeObject = attributes.get(IN_LIGHT);
		return attributeObject.get(index, 0);
	}

	public void setGenerateNormal(boolean b) {
		generateNormals = b;
	}

	public int getVertexArrayObjectIndex() {
		return bufferId;
	}

	public void clearData() {
		for (Map.Entry<String, AttributeObject> entry : attributes.entrySet()) {
			entry.getValue().clear();
		}
		indices.clear();
		indexBuffer.clear();
		buffer.clear();
	}

	public void deleteBuffers() {
		GL15.glDeleteBuffers(bufferId);
		GL15.glDeleteBuffers(vboIndexBuffer);
		GL30.glDeleteVertexArrays(vao);
	}
}
