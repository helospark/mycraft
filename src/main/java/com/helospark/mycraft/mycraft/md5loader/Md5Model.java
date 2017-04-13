package com.helospark.mycraft.mycraft.md5loader;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL15;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.springframework.context.ApplicationContext;

import com.helospark.mycraft.mycraft.attributes.Model;
import com.helospark.mycraft.mycraft.boundaries.DefaultJavaFileHandler;
import com.helospark.mycraft.mycraft.mathutils.VectorMathUtils;
import com.helospark.mycraft.mycraft.render.Material;
import com.helospark.mycraft.mycraft.render.RenderableModelNode;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.xml.MaterialLoader;

public class Md5Model {
	class Bone {
		String name = "";
		Vector3f position = new Vector3f();
		Quaternion orientation = new Quaternion();
		int parentId;

		int flags;
		int startPosition;
	};

	class Weight {
		int boneId;
		float effectStrength;
		Vector3f position = new Vector3f();
	};

	class Vertex {
		float u, v;
		int startWeight;
		int numWeights;
		Vector3f normal = new Vector3f();
		Vector3f tangent = new Vector3f();
	};

	class Mesh {
		String shaderName;

		boolean hasTexture;
		Material material;
		List<Integer> triangleIndices = new ArrayList<>();
		List<Weight> weights = new ArrayList<>();
		List<Vertex> vertices = new ArrayList<>();

		Mesh() {
			hasTexture = false;
		}
	};

	class BoneFrameData {
		Quaternion orientation = new Quaternion();
		Vector3f position = new Vector3f();
	};

	class BoundingBox {
		Vector3f min = new Vector3f();
		Vector3f max = new Vector3f();
	};

	class Frame {
		List<BoneFrameData> boneFrameDatas = new ArrayList<>();
		BoundingBox boundingBox = new BoundingBox();
	};

	class AnimationListEntry {
		String name;
		boolean looping;

		AnimationListEntry(String pName, boolean pLooping) {
			this.name = pName;
			this.looping = pLooping;
		}

	};

	class IntPair {
		int first;
		int second;

		public IntPair(int first, int second) {
			this.first = first;
			this.second = second;
		}

	}

	class VertexDataFullTangent {
		public VertexDataFullTangent(Vector3f position, Vector3f normal, Vector3f color, float u,
				float v, Vector3f tangent) {
			this.position = position;
			this.normal = normal;
			this.color = color;
			this.tangent = tangent;
			this.u = u;
			this.v = v;
		}

		Vector3f position;
		Vector3f normal;
		Vector3f tangent;
		Vector3f color;
		float u, v;
	}

	class AnimationData {
		Frame baseFrame = new Frame();
		List<Frame> frames = new ArrayList<>();
		int frameRate;
		int numFrames;
		float frameTime;
		float time;

		String currentAnimationName;
		float currentAnimationStartTime;
		float currentAnimationEndTime;
		boolean currentAnimationLooping;

		boolean hasFromInterpolation;
		float fromInterpolationTime;
		float fromInterpolationElapsedTime;
		int interpolationFromFrame;

		List<AnimationListEntry> nextAnimations = new ArrayList<>();

		Map<String, IntPair> animationNameInterval = new HashMap<>();

		public AnimationData() {
			numFrames = 0;
			frameTime = 0.0f;
			time = 0.0f;
			currentAnimationStartTime = 0.0f;
			currentAnimationEndTime = 0.0f;
			currentAnimationName = "";
			currentAnimationLooping = true;
			hasFromInterpolation = false;
			fromInterpolationTime = 0.0f;
			interpolationFromFrame = 0;
			fromInterpolationElapsedTime = 0.0f;
		}
	};

	List<Mesh> meshes = new ArrayList<>();
	List<Bone> bones = new ArrayList<>();

	List<Model> objects = new ArrayList<>();

	boolean hasAnimation;
	int version;

	boolean staticModelPrepared;
	AnimationData animationData;

	MaterialLoader materialLoader;

	DefaultJavaFileHandler fileHandler;

	String fileName;

	float playbackSpeed;

	ByteBuffer byteBuffer = null;

	RenderableModelNode[] nodes;

	public boolean hasAnimation() {
		return hasAnimation;
	}

	public void addToScene() {
		uploadVertexBuffers();
	}

	public void setPlaybackSpeed(float speed) {
		playbackSpeed = speed;
	}

	public void StopCurrentAnimationAfterFinished() {
		animationData.currentAnimationLooping = false;
	}

	public String getCurrentAnimationName() {
		return animationData.currentAnimationName;
	}

	public Md5Model(String fileName) {
		animationData = null;
		staticModelPrepared = false;
		this.fileName = fileName;
		ApplicationContext context = Singleton.getInstance().getContext();
		materialLoader = context.getBean(MaterialLoader.class);
		fileHandler = context.getBean(DefaultJavaFileHandler.class);
	}

	public void readNextString(InputFileStream in, String expected) {
		String tmp = in.getNext();
		if (!tmp.equals(expected)) {
			String error = expected + " expected but " + tmp + " is found";
			tmp = in.getNext();
			error += " The next is " + tmp;
			throw new RuntimeException(error);
		}
	}

	public String StripQuotes(String str) {
		int first = str.indexOf("\"");
		int last = str.lastIndexOf("\"");
		return str.substring(first + 1, last);
	}

	public void ReadVector3(InputFileStream in, Vector3f vec) {
		readNextString(in, "(");
		vec.x = Float.valueOf(in.getNext());
		vec.y = Float.valueOf(in.getNext());
		vec.z = Float.valueOf(in.getNext());
		readNextString(in, ")");
	}

	public void ReadVector2(InputFileStream in, Vector3f vec) {
		readNextString(in, "(");
		vec.x = Float.valueOf(in.getNext());
		vec.y = Float.valueOf(in.getNext());
		readNextString(in, ")");
	}

	public Quaternion CreateQuaternion(Vector3f orientation) {
		float w = 1.0f - (orientation.x * orientation.x) - (orientation.y * orientation.y)
				- (orientation.z * orientation.z);
		if (w < 0.0f)
			w = 0.0f;
		else
			w = (float) -Math.sqrt(w);
		Quaternion q = new Quaternion();
		q.x = orientation.x;
		q.y = orientation.y;
		q.z = orientation.z;
		q.w = w;
		return q;
	}

	public Bone findBoneByName(String name) {
		for (int i = 0; i < bones.size(); ++i) {
			if (bones.get(i).name.equals(name)) {
				return bones.get(i);
			}
		}
		return null;
	}

	public boolean fileExists(String fileName) {
		File file = new File(fileName);
		return file.exists();
	}

	public Material loadMaterial(String fileName) {
		int dotPosition = fileName.lastIndexOf(".");
		int slashPosition = fileName.lastIndexOf("/");
		if (slashPosition == -1) {
			slashPosition = fileName.lastIndexOf("\\");
		}

		if (dotPosition != -1) {
			fileName = fileName.substring(0, dotPosition);
		}
		fileName += ".xml";

		if (fileExists(fileName)) {
			materialLoader.readFromXml(fileName);
		} else if (fileExists(fileName.substring(slashPosition + 1))) {
			materialLoader.readFromXml(fileName.substring(slashPosition + 1));
		}
		if (dotPosition == -1) {
			dotPosition = fileName.length();
		}
		String shaderName = fileName.substring(slashPosition + 1, dotPosition);
		dotPosition = shaderName.lastIndexOf(".");
		shaderName = shaderName.substring(0, dotPosition);
		Material material = materialLoader.getMaterialFromName(shaderName);
		if (material == null) {
			material = materialLoader.getMaterialFromName("Default");
		}
		return material;
	}

	public boolean loadModelFile(String fileName) {
		String tmpString;
		String path = "";
		int lastSlashPosition = fileName.lastIndexOf("/");
		if (lastSlashPosition != -1) {
			path = fileName.substring(0, lastSlashPosition + 1);
		}
		InputFileStream in = new InputFileStream(fileName);
		if (!in.is_open())
			return false;

		readNextString(in, "MD5Version");
		version = Integer.parseInt(in.getNext());

		readNextString(in, "commandline");
		in.skipLine();

		int numJoints;
		readNextString(in, "numJoints");
		numJoints = Integer.parseInt(in.getNext());
		for (int i = 0; i < numJoints; ++i) {
			bones.add(new Bone());
		}

		int numMeshes;
		readNextString(in, "numMeshes");
		numMeshes = Integer.parseInt(in.getNext());
		for (int i = 0; i < numMeshes; ++i) {
			meshes.add(new Mesh());
		}

		readNextString(in, "joints");
		readNextString(in, "{");
		for (int i = 0; i < numJoints; ++i) {
			tmpString = in.getNext();
			bones.get(i).name = StripQuotes(tmpString);

			bones.get(i).parentId = Integer.parseInt(in.getNext());
			Vector3f orientation = new Vector3f();
			ReadVector3(in, bones.get(i).position);
			ReadVector3(in, orientation);
			bones.get(i).orientation = CreateQuaternion(orientation);

			in.skipLine();
		}
		readNextString(in, "}");

		for (int i = 0; i < numMeshes; ++i) {
			Mesh currentMesh = meshes.get(i);
			readNextString(in, "mesh");
			readNextString(in, "{");

			readNextString(in, "shader");
			tmpString = in.getNext();
			meshes.get(i).shaderName = path + StripQuotes(tmpString);

			Material material;
			if ((material = loadMaterial(meshes.get(i).shaderName)) != null) {
				meshes.get(i).hasTexture = true;
				meshes.get(i).material = material;
			}
			loadAnimations(meshes.get(i).shaderName);

			readNextString(in, "numverts");
			int numVerts;
			numVerts = Integer.parseInt(in.getNext());

			for (int k = 0; k < numVerts; ++k) {
				currentMesh.vertices.add(new Vertex());
			}

			for (int j = 0; j < numVerts; ++j) {
				Vertex currentVertex = currentMesh.vertices.get(j);
				readNextString(in, "vert");
				int vertexId = Integer.parseInt(in.getNext());
				if (vertexId != j) {
					throw new RuntimeException("VertexId != j");
				}
				Vector3f uv = new Vector3f();
				ReadVector2(in, uv);
				currentVertex.u = uv.x;
				currentVertex.v = uv.y;

				currentVertex.startWeight = Integer.parseInt(in.getNext());
				currentVertex.numWeights = Integer.parseInt(in.getNext());
			}

			readNextString(in, "numtris");
			int numTriangles;
			numTriangles = Integer.parseInt(in.getNext());
			for (int k = 0; k < numTriangles * 3; ++k) {
				currentMesh.triangleIndices.add(new Integer(0));
			}

			for (int j = 0; j < numTriangles; ++j) {
				readNextString(in, "tri");
				int triangleIndex = Integer.parseInt(in.getNext());

				if (triangleIndex != j) {
					throw new RuntimeException("triangleIndx != j");
				}
				currentMesh.triangleIndices.set(j * 3 + 0, Integer.parseInt(in.getNext()));
				currentMesh.triangleIndices.set(j * 3 + 1, Integer.parseInt(in.getNext()));
				currentMesh.triangleIndices.set(j * 3 + 2, Integer.parseInt(in.getNext()));
			}

			readNextString(in, "numweights");
			int numWeights = Integer.parseInt(in.getNext());
			for (int k = 0; k < numWeights; ++k) {
				currentMesh.weights.add(new Weight());
			}

			for (int j = 0; j < numWeights; ++j) {
				Weight currentWeight = currentMesh.weights.get(j);
				readNextString(in, "weight");
				int weightIndex = Integer.parseInt(in.getNext());
				if (weightIndex != j) {
					throw new RuntimeException("weightIndex != j");
				}
				currentWeight.boneId = Integer.parseInt(in.getNext());
				currentWeight.effectStrength = Float.parseFloat(in.getNext());
				ReadVector3(in, currentWeight.position);
			}

			readNextString(in, "}");
		}
		return true;
	}

	private void loadAnimations(String fileName) {
		fileName += "_anim.dat";
		if (fileExists(fileName)) {
			List<String> animationLines = fileHandler.getLineListFromFile(fileName);
			for (int i = 0; i < animationLines.size(); ++i) {
				String line = animationLines.get(i).trim();
				if (!line.startsWith("#")) {
					String[] splittedLine = line.split("\\s+");
					String animationName = splittedLine[0];
					int startFrame = Integer.parseInt(splittedLine[1]);
					int endFrame = Integer.parseInt(splittedLine[2]);
					addAnimationInterval(animationName, startFrame, endFrame);
				}
			}
		}
	}

	public boolean LoadAnimationFile(String fileName) {
		String tmpString;
		InputFileStream in = new InputFileStream(fileName);

		if (!in.is_open())
			return false;
		if (animationData == null) {
			animationData = new AnimationData();
		}

		readNextString(in, "MD5Version");
		int version = Integer.parseInt(in.getNext());
		readNextString(in, "commandline");
		in.skipLine();

		readNextString(in, "numFrames");
		animationData.numFrames = Integer.parseInt(in.getNext());
		for (int k = 0; k < animationData.numFrames; ++k) {
			animationData.frames.add(new Frame());
		}

		readNextString(in, "numJoints");
		int numJoints = Integer.parseInt(in.getNext());

		if (numJoints > bones.size()) {
			return false;
		}

		for (int i = 0; i < animationData.numFrames; ++i) {
			for (int k = 0; k < numJoints; ++k) {
				animationData.frames.get(i).boneFrameDatas.add(new BoneFrameData());
			}
		}

		readNextString(in, "frameRate");
		animationData.frameRate = Integer.parseInt(in.getNext());
		animationData.frameTime = 1.0f / animationData.frameRate;
		animationData.currentAnimationEndTime = animationData.numFrames * animationData.frameTime;

		readNextString(in, "numAnimatedComponents");
		int numAnimatedCompontens = Integer.parseInt(in.getNext());

		readNextString(in, "hierarchy");
		readNextString(in, "{");

		for (int i = 0; i < numJoints; ++i) {
			tmpString = in.getNext();
			tmpString = StripQuotes(tmpString);
			Bone currentBone = null;
			if ((currentBone = findBoneByName(tmpString)) == null) {
				return false;
			}

			int parentId = Integer.parseInt(in.getNext());

			if (currentBone.parentId != parentId) {
				return false;
			}
			currentBone.flags = Integer.parseInt(in.getNext());
			currentBone.startPosition = Integer.parseInt(in.getNext());
			in.skipLine();
		}
		readNextString(in, "}");

		readNextString(in, "bounds");
		readNextString(in, "{");

		for (int i = 0; i < animationData.numFrames; ++i) {
			BoundingBox currentBoundingBox = animationData.frames.get(i).boundingBox;
			ReadVector3(in, currentBoundingBox.min);
			ReadVector3(in, currentBoundingBox.max);
		}
		readNextString(in, "}");

		readNextString(in, "baseframe");
		readNextString(in, "{");

		Frame baseFrame = animationData.baseFrame;
		for (int k = 0; k < numJoints; ++k) {
			baseFrame.boneFrameDatas.add(new BoneFrameData());
		}
		for (int i = 0; i < numJoints; ++i) {
			ReadVector3(in, baseFrame.boneFrameDatas.get(i).position);
			Vector3f orientation = new Vector3f();
			ReadVector3(in, orientation);
			baseFrame.boneFrameDatas.get(i).orientation = CreateQuaternion(orientation);
		}
		readNextString(in, "}");

		for (int i = 0; i < animationData.numFrames; ++i) {
			readNextString(in, "frame");
			int frameId = Integer.parseInt(in.getNext());
			readNextString(in, "{");
			for (int k = 0; k < numJoints; ++k) {
				Vector3f position = baseFrame.boneFrameDatas.get(k).position;
				Vector3f orientation = new Vector3f();
				for (int j = 0; j < 6; ++j) {
					float component;
					component = Float.parseFloat(in.getNext());
					if (j == 0)
						position.x = component;
					if (j == 1)
						position.y = component;
					if (j == 2)
						position.z = component;
					if (j == 3)
						orientation.x = component;
					if (j == 4)
						orientation.y = component;
					if (j == 5)
						orientation.z = component;
				}
				animationData.frames.get(i).boneFrameDatas.get(k).position.set(position);
				animationData.frames.get(i).boneFrameDatas.get(k).orientation
						.set(CreateQuaternion(orientation));
			}
			readNextString(in, "}");
		}

		return true;
	}

	public boolean load() {
		int dotPosition = fileName.lastIndexOf(".");
		String fileNameWithoutExtension = fileName.substring(0, dotPosition);
		if (!loadModelFile(fileNameWithoutExtension + ".md5mesh"))
			return false;

		if (!LoadAnimationFile(fileNameWithoutExtension + ".md5anim")) {
			hasAnimation = false;
		} else {
			hasAnimation = true;
		}
		if (hasAnimation) {
			prepareSkeleton();
		}

		return true;
	}

	public void createStaticModel() {
		List<BoneFrameData> frameData = new ArrayList<>();
		for (int i = 0; i < bones.size(); ++i) {
			BoneFrameData boneData = new BoneFrameData();
			boneData.orientation = bones.get(i).orientation;
			boneData.position = bones.get(i).position;
			frameData.add(boneData);
		}
		createModel(frameData);
	}

	public int[] uploadVertexBuffers() {
		if (objects.size() != meshes.size()) {
			for (int i = 0; i < objects.size(); ++i) {
				Model object = objects.get(i);
				object.clearData();
				object.deleteBuffers();
			}
			objects.clear();
			for (int k = 0; k < meshes.size(); ++k) {
				Model model = new Model();
				model.createPositionAttribute();
				model.createUvAttribute();
				objects.add(model);
			}
		} else {
			return new int[0];
		}
		int[] vaos = new int[meshes.size()];
		nodes = new RenderableModelNode[meshes.size()];
		for (int i = 0; i < meshes.size(); ++i) {
			Mesh currentMesh = meshes.get(i);
			Model object = objects.get(i);
			for (int j = 0; j < currentMesh.vertices.size(); ++j) {
				Vertex currentVertex = currentMesh.vertices.get(j);
				Vector3f newPosition = new Vector3f(0, 0, 0);
				for (int k = 0; k < currentVertex.numWeights; ++k) {
					int weightIndex = currentVertex.startWeight + k;
					Weight currentWeight = currentMesh.weights.get(weightIndex);
					Bone effectBone = bones.get(currentWeight.boneId);

					Vector3f rotationPosition = VectorMathUtils.multiply(effectBone.orientation,
							currentWeight.position);
					Vector3f tmp = new Vector3f();
					Vector3f.add(rotationPosition, effectBone.position, tmp);
					VectorMathUtils.mul(tmp, currentWeight.effectStrength);
					Vector3f.add(newPosition, tmp, newPosition);
				}
				object.addPosition(new Vector3f(newPosition));
				object.addUv(new Vector2f(currentVertex.u, currentVertex.v));
			}

			for (int k = 0; k < currentMesh.triangleIndices.size(); ++k) {
				object.addIndices(currentMesh.triangleIndices.get(k));
			}

			vaos[i] = object.getVaoForProgram(currentMesh.material.getShader());
			nodes[i] = new RenderableModelNode(currentMesh.material.shader, object);
			nodes[i].setMaterial(currentMesh.material);
		}
		return vaos;
	}

	public List<Model> getSceneNodes() {
		return objects;
	}

	public void createModel(List<BoneFrameData> skeleton) {
		if (objects.size() == 0)
			uploadVertexBuffers();

		for (int i = 0; i < meshes.size(); ++i) {
			Model meshObject = objects.get(i);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, meshObject.getVertexArrayObjectIndex());

			byteBuffer = GL15.glMapBuffer(GL15.GL_ARRAY_BUFFER, GL15.GL_READ_WRITE, byteBuffer);
			FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
			Mesh currentMesh = meshes.get(i);
			for (int j = 0; j < currentMesh.vertices.size(); ++j) {
				Vertex currentVertex = currentMesh.vertices.get(j);
				Vector3f newPosition = new Vector3f(0, 0, 0);
				for (int k = 0; k < currentVertex.numWeights; ++k) {
					int weightIndex = currentVertex.startWeight + k;
					Weight currentWeight = currentMesh.weights.get(weightIndex);
					BoneFrameData effectBone = skeleton.get(currentWeight.boneId);
					Vector3f rotationPosition = VectorMathUtils.multiply(effectBone.orientation,
							currentWeight.position);
					Vector3f tmp = new Vector3f();

					Vector3f.add(rotationPosition, effectBone.position, tmp);
					VectorMathUtils.mul(tmp, currentWeight.effectStrength);
					Vector3f.add(newPosition, tmp, newPosition);
				}

				floatBuffer.put((j * 5) + 0, currentVertex.u);
				floatBuffer.put((j * 5) + 1, currentVertex.v);
				floatBuffer.put((j * 5) + 2, newPosition.x);
				floatBuffer.put((j * 5) + 3, newPosition.y);
				floatBuffer.put((j * 5) + 4, newPosition.z);
			}
			GL15.glUnmapBuffer(GL15.GL_ARRAY_BUFFER);
		}
	}

	public void prepareSkeleton() {
		for (int i = 0; i < animationData.frames.size(); ++i) {
			List<BoneFrameData> boneFrameData = animationData.frames.get(i).boneFrameDatas;
			for (int j = 0; j < animationData.frames.get(i).boneFrameDatas.size(); ++j) {
				BoneFrameData currentBone = animationData.frames.get(i).boneFrameDatas.get(j);
				int parentId = bones.get(j).parentId;
				if (parentId != -1) {

					Vector3f rotpos = VectorMathUtils.multiply(
							boneFrameData.get(parentId).orientation, currentBone.position);

					Quaternion.mul(boneFrameData.get(parentId).orientation,
							currentBone.orientation, currentBone.orientation);
					currentBone.orientation = currentBone.orientation
							.normalise(currentBone.orientation);
					Vector3f.add(rotpos, boneFrameData.get(parentId).position, currentBone.position);
				}
			}
		}
	}

	public void update(double deltaTime) {
		if (animationData == null)
			return;
		animationData.time += deltaTime * playbackSpeed;
		if (animationData.hasFromInterpolation)
			animationData.fromInterpolationElapsedTime += deltaTime * playbackSpeed;

		if (animationData.hasFromInterpolation
				&& animationData.fromInterpolationElapsedTime >= animationData.fromInterpolationTime)
			animationData.hasFromInterpolation = false;

		if (animationData.time >= animationData.currentAnimationEndTime) {
			if (animationData.currentAnimationLooping) {
				animationData.time = animationData.currentAnimationStartTime;
			} else {
				boolean nextAnimationSet = false;
				if (!animationData.nextAnimations.isEmpty()) {
					AnimationListEntry entry = animationData.nextAnimations.get(0);
					animationData.nextAnimations.remove(0);
					nextAnimationSet = playAnimation(entry.name, entry.looping, false, false, 0.0f);
				}

				if (!nextAnimationSet) {
					animationData.time = animationData.currentAnimationEndTime;
				}
			}
		}
		if (animationData.time < animationData.currentAnimationStartTime)
			animationData.time = animationData.currentAnimationStartTime;

		float fFrame = animationData.time / animationData.frameTime;
		int lFrame, uFrame;
		if (animationData.hasFromInterpolation) {
			lFrame = animationData.interpolationFromFrame;
		} else {
			lFrame = (int) (Math.floor(fFrame));
		}
		uFrame = (int) (Math.ceil(fFrame));
		if (uFrame >= animationData.frames.size())
			uFrame = (int) (animationData.currentAnimationStartTime / animationData.frameTime);

		Frame aFrame = animationData.frames.get(lFrame);
		Frame bFrame = animationData.frames.get(uFrame);
		float interpolationValue;
		if (animationData.hasFromInterpolation) {
			interpolationValue = animationData.fromInterpolationElapsedTime
					/ animationData.fromInterpolationTime;
		} else {
			interpolationValue = (animationData.time % animationData.frameTime)
					/ (animationData.frameTime);
		}
		if (interpolationValue > 1.0f)
			interpolationValue = 1.0f;
		List<BoneFrameData> interpolatedBones = new ArrayList<>();

		for (int i = 0; i < aFrame.boneFrameDatas.size(); ++i) {
			BoneFrameData currentBone = new BoneFrameData();
			com.badlogic.gdx.math.Quaternion first = VectorMathUtils
					.toOtherQuaternion(aFrame.boneFrameDatas.get(i).orientation);
			com.badlogic.gdx.math.Quaternion second = VectorMathUtils
					.toOtherQuaternion(bFrame.boneFrameDatas.get(i).orientation);
			com.badlogic.gdx.math.Quaternion result = first.slerp(second, interpolationValue);
			VectorMathUtils.setQuaternion(currentBone.orientation, result);

			currentBone.position = VectorMathUtils.mix(aFrame.boneFrameDatas.get(i).position,
					bFrame.boneFrameDatas.get(i).position, interpolationValue);

			interpolatedBones.add(currentBone);
		}

		createModel(interpolatedBones);

	}

	public void addAnimationInterval(String animationName, int startFrame, int endFrame) {
		if (endFrame < startFrame) {
			int tmp = startFrame;
			startFrame = endFrame;
			endFrame = tmp;
		}
		if (animationData == null) {
			animationData = new AnimationData();
		}
		animationData.animationNameInterval.put(animationName, new IntPair(startFrame, endFrame));
	}

	public boolean playAnimation(String name, boolean repeat, boolean addToList,
			boolean interpolate, float interpolationTime) {
		boolean returnValue = false;
		if (addToList) {
			animationData.nextAnimations.add(new AnimationListEntry(name, repeat));
			returnValue = true;
		} else {

			if (interpolate) {
				int currentFrame = (int) (animationData.time / animationData.frameTime);
				setFromInterpolation(currentFrame, interpolationTime);
			}
			IntPair pair = animationData.animationNameInterval.get(name);
			if (pair != null) {
				animationData.currentAnimationStartTime = pair.first * animationData.frameTime;
				animationData.currentAnimationEndTime = pair.second * animationData.frameTime;
				animationData.currentAnimationName = name;
				animationData.currentAnimationLooping = repeat;
				animationData.time = animationData.currentAnimationStartTime;
				returnValue = true;
			} else
				returnValue = false;
		}

		return returnValue;
	}

	void setFromInterpolation(int frame, float time) {
		animationData.hasFromInterpolation = true;
		animationData.fromInterpolationTime = time;
		animationData.fromInterpolationElapsedTime = 0.0f;
		animationData.interpolationFromFrame = frame;
	}

	public RenderableModelNode[] getRenderableNodes() {
		return nodes;
	}

}
