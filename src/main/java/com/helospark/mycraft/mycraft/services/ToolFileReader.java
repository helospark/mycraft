package com.helospark.mycraft.mycraft.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector3f;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;

import com.helospark.mycraft.mycraft.attributes.Model;
import com.helospark.mycraft.mycraft.attributes.SimpleModelCreator;
import com.helospark.mycraft.mycraft.boundaries.DefaultJavaFileHandler;
import com.helospark.mycraft.mycraft.helpers.FileReaderHelper;
import com.helospark.mycraft.mycraft.helpers.XmlHelpers;
import com.helospark.mycraft.mycraft.mathutils.IntVector;
import com.helospark.mycraft.mycraft.xml.ModelLoader;
import com.helospark.mycraft.mycraft.xml.XmlLoader;

@Service
public class ToolFileReader implements XmlLoader {
	private static final String SUPPORTED_VERSION = "1.0";
	@Autowired
	private DefaultJavaFileHandler fileLoader;

	@Autowired
	SimpleModelCreator simpleModelCreator;

	@Autowired
	ModelLoader modelLoader;

	public ToolFileReader() {

	}

	public void loadFile(String fileName) {
		List<String> fileContent = fileLoader.getLineListFromFile(fileName);
		int currentLine = 0;

		currentLine = assertVersion(fileContent, currentLine);
		String modelName = FileReaderHelper.readLineExpect(fileContent, currentLine++, "model");
		String sizeString = FileReaderHelper.readLineExpect(fileContent, currentLine++, "size");
		IntVector materixSize = IntVector.valueOf(sizeString);
		String centerString = FileReaderHelper.readLineExpect(fileContent, currentLine++, "center");
		IntVector center = IntVector.valueOf(centerString);
		String blockSizeString = FileReaderHelper.readLineExpect(fileContent, currentLine++,
				"blockSize");
		float blockSize = Float.parseFloat(blockSizeString);

		int pattern[][] = new int[materixSize.y][materixSize.x];
		List<Integer> colors = new ArrayList<>();

		for (int i = 0; i < materixSize.y; ++i) {
			String[] splittedLine = fileContent.get(currentLine + i).split(" ");
			for (int j = 0; j < materixSize.x; ++j) {
				String currentCharacterAsString = splittedLine[j];
				pattern[i][j] = Integer.parseInt(currentCharacterAsString);
				if (!colors.contains(pattern[i][j]) && pattern[i][j] != 0) {
					colors.add(pattern[i][j]);
				}
			}
		}

		Map<String, Map<Integer, Vector3f>> typeColors = new HashMap<>();
		HashMap<Integer, Vector3f> colorForCurrentStage = new HashMap<>();
		typeColors.put("default", colorForCurrentStage);

		currentLine += materixSize.y;
		for (int i = 0; i < colors.size(); ++i) {
			String line = FileReaderHelper.readLineExpect(fileContent, currentLine++, "color");
			parseColorDataAndAddToMap(currentLine, colorForCurrentStage, line);
		}

		while (currentLine < fileContent.size()) {
			String type = FileReaderHelper.readLineExpect(fileContent, currentLine++, "type");
			colorForCurrentStage = new HashMap<>();
			while (canReadColorLine(fileContent, currentLine)) {
				String line = FileReaderHelper.readLineExpect(fileContent, currentLine++, "color");
				parseColorDataAndAddToMap(currentLine, colorForCurrentStage, line);
			}
			Map<Integer, Vector3f> defaultColors = typeColors.get("default");
			for (Map.Entry<Integer, Vector3f> entry : defaultColors.entrySet()) {
				if (!colorForCurrentStage.containsKey(entry.getKey())) {
					colorForCurrentStage.put(entry.getKey(), entry.getValue());
				}
			}
			typeColors.put(type, colorForCurrentStage);
		}

		for (Map.Entry<String, Map<Integer, Vector3f>> entry : typeColors.entrySet()) {
			Model model = simpleModelCreator.createModelFromPatternAndMap(pattern,
					entry.getValue(), blockSize, center);
			modelLoader.addModel(entry.getKey() + " " + modelName, model);
		}

	}

	private int assertVersion(List<String> fileContent, int currentLine) {
		String version = FileReaderHelper.readLineExpect(fileContent, currentLine++, "version");
		if (!version.equals(SUPPORTED_VERSION)) {
			throw new RuntimeException("Unsupported version");
		}
		return currentLine;
	}

	private boolean canReadColorLine(List<String> fileContent, int currentLine) {
		return currentLine < fileContent.size() && fileContent.get(currentLine).startsWith("color");
	}

	private void parseColorDataAndAddToMap(int currentLine,
			HashMap<Integer, Vector3f> colorForCurrentStage, String line) {
		String[] splittedString = line.split(" ");
		if (splittedString.length != 4) {
			throw new RuntimeException("The correct syntax is: color id r g b\nAt line "
					+ currentLine);
		}
		int colorId = Integer.parseInt(splittedString[0]);
		int r = Integer.parseInt(splittedString[1]);
		int g = Integer.parseInt(splittedString[2]);
		int b = Integer.parseInt(splittedString[3]);
		Vector3f color = new Vector3f(r / 255.0f, g / 255.0f, b / 255.0f);

		colorForCurrentStage.put(colorId, color);
	}

	@Override
	public void parseXml(Element rootElement) {
		List<String> files = XmlHelpers.getStringList(rootElement, "file");
		for (int i = 0; i < files.size(); ++i) {
			loadFile(files.get(i));
		}
	}
}
