package com.helospark.mycraft.mycraft.shader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;

import com.helospark.mycraft.mycraft.actor.MyIdGenerator;
import com.helospark.mycraft.mycraft.boundaries.DefaultJavaFileHandler;
import com.helospark.mycraft.mycraft.helpers.XmlHelpers;
import com.helospark.mycraft.mycraft.services.GlobalParameters;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.xml.XmlLoader;

@Service
public class ShaderLoader implements XmlLoader {
	private Map<String, Integer> loadedShaders = new HashMap<String, Integer>();
	private Map<String, Shader> loadedPrograms = new HashMap<>();
	private DefaultJavaFileHandler fileHandler = new DefaultJavaFileHandler();
	private MyIdGenerator myIdGenerator;
	@Autowired
	private GlobalParameters globalParameters;

	public ShaderLoader() {
		ApplicationContext context = Singleton.getInstance().getContext();
		myIdGenerator = context.getBean(MyIdGenerator.class);
	}

	public Shader loadStandardShaderFromFile(String vertexShaderFileName,
			String fragmentShaderFileName) {

		String vertexPath = getFilePath(vertexShaderFileName);

		String fragmentPath = getFilePath(fragmentShaderFileName);

		String vertexShaderSource = preProcessShader(
				fileHandler.getLineListFromFile(vertexShaderFileName),
				vertexPath);
		String fragmentShaderSource = preProcessShader(
				fileHandler.getLineListFromFile(fragmentShaderFileName),
				fragmentPath);

		int vertexShaderId = loadShaderFromString(vertexShaderSource,
				GL20.GL_VERTEX_SHADER, vertexShaderFileName);
		int fragmentShaderId = loadShaderFromString(fragmentShaderSource,
				GL20.GL_FRAGMENT_SHADER, fragmentShaderFileName);
		Shader shader = new Shader();
		shader.addShader(GL20.GL_VERTEX_SHADER, vertexShaderId,
				vertexShaderSource);
		shader.addShader(GL20.GL_FRAGMENT_SHADER, fragmentShaderId,
				fragmentShaderSource);
		shader.linkShader();

		setGlobalUniforms(shader);
		loadedShaders.put(vertexShaderFileName, vertexShaderId);
		loadedShaders.put(fragmentShaderFileName, fragmentShaderId);
		loadedPrograms.put("shader_" + myIdGenerator.getNextId().toString(),
				shader);
		return shader;
	}

	private void setGlobalUniforms(Shader shader) {
		shader.addGlobalShaderUniform("fogStart", new FloatShaderUniform(
				globalParameters.fogNearDistance));
		shader.addGlobalShaderUniform("fogEnd", new FloatShaderUniform(
				globalParameters.fogFarDistance));
		shader.addGlobalShaderUniform("fogColor", new Vector3fShaderUniform(
				globalParameters.fogColor));
	}

	private String getFilePath(String fileName) {
		File vertexFile = new File(fileName);
		String vertexPath = vertexFile.getPath().replace("\\", "/");
		int lastIndex = vertexPath.lastIndexOf("/");
		if (lastIndex == -1) {
			return "";
		} else {
			return fileName.substring(0, lastIndex);
		}
	}

	private String preProcessShader(List<String> shaderSource, String path) {
		List<String> shaderSourceCopy = new ArrayList<>();
		shaderSourceCopy.addAll(shaderSource);
		for (int i = 0; i < shaderSourceCopy.size(); ++i) {
			String currentLine = shaderSourceCopy.get(i);
			if (currentLine.startsWith("#")
					&& !currentLine.startsWith("#version")) {
				List<String> result = null;
				currentLine = currentLine.trim();
				if (currentLine.startsWith("#include")) {
					result = processShaderInclude(currentLine, path);
				}

				shaderSourceCopy.remove(i);
				if (result != null) {
					shaderSourceCopy.addAll(i, result);
					--i;
				}
			}
		}

		return createStringFromStringList(shaderSourceCopy);
	}

	private String createStringFromStringList(List<String> shaderSourceCopy) {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < shaderSourceCopy.size(); ++i) {
			stringBuilder.append(shaderSourceCopy.get(i));
			stringBuilder.append("\n");
		}
		return stringBuilder.toString();
	}

	private List<String> processShaderInclude(String currentLine, String path) {
		List<String> result = new ArrayList<>();

		String[] splittedString = currentLine.split(" ");
		if (splittedString.length > 1) {
			String fileName = splittedString[1];
			fileName = fileName.replace("\"", "");
			fileName = path + "/" + fileName;
			result = fileHandler.getLineListFromFile(fileName);
		}

		return result;
	}

	private int loadShaderFromString(String shaderSource, int type,
			String fileName) {
		int shaderId = GL20.glCreateShader(type);
		GL20.glShaderSource(shaderId, shaderSource);
		GL20.glCompileShader(shaderId);

		int isCompiled = GL20.glGetShader(shaderId, GL20.GL_COMPILE_STATUS);
		if (isCompiled == GL11.GL_FALSE) {
			String errorMessage = GL20.glGetShaderInfoLog(shaderId, 1000);
			throw new RuntimeException("Unable to compile shader (" + fileName
					+ "): " + errorMessage);
		}
		return shaderId;
	}

	@Override
	public void parseXml(Element rootElement) {

		String shaderName = rootElement.getAttribute("name");

		Element vertexShaderElement = XmlHelpers.findFirstElement(rootElement
				.getElementsByTagName("vertex-shader"));
		Element fragmentShaderElement = XmlHelpers.findFirstElement(rootElement
				.getElementsByTagName("fragment-shader"));

		String vertexShader = vertexShaderElement.getTextContent();
		String fragmentShader = fragmentShaderElement.getTextContent();

		Shader shader = loadStandardShaderFromFile(vertexShader, fragmentShader);
		loadedPrograms.put(shaderName, shader);
	}

	public Shader getFromName(String name) {
		return loadedPrograms.get(name);
	}
}
