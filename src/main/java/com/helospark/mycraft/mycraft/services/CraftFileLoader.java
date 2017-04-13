package com.helospark.mycraft.mycraft.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;

import com.helospark.mycraft.mycraft.actor.InventoryItem;
import com.helospark.mycraft.mycraft.boundaries.DefaultJavaFileHandler;
import com.helospark.mycraft.mycraft.helpers.FileReaderHelper;
import com.helospark.mycraft.mycraft.helpers.XmlHelpers;
import com.helospark.mycraft.mycraft.xml.XmlLoader;

@Service
public class CraftFileLoader implements XmlLoader {
	private static final String SUPPORTED_VERSION = "1.0";
	@Autowired
	private DefaultJavaFileHandler fileLoader;

	@Autowired
	private CraftResolverService craftResolverService;

	private List<String> lines;

	public CraftFileLoader() {

	}

	public void loadCraftFile(String fileName) {
		lines = fileLoader.getLineListFromFile(fileName);
		int currentLine = 0;
		String version = FileReaderHelper.readLineExpect(lines, currentLine++, "version");
		assertVersion(version);

		while (currentLine < lines.size()) {
			FileReaderHelper.readLineExpect(lines, currentLine++, "craft-item:");
			Boolean.valueOf(FileReaderHelper.readLineExpect(lines, currentLine++, "description"));

			boolean canTranslate = Boolean.valueOf(FileReaderHelper.readLineExpect(lines,
					currentLine++, "can-translate"));
			boolean canRotate = Boolean.valueOf(FileReaderHelper.readLineExpect(lines,
					currentLine++, "can-rotate"));
			int width = Integer.parseInt(FileReaderHelper.readLineExpect(lines, currentLine++,
					"width"));
			int height = Integer.parseInt(FileReaderHelper.readLineExpect(lines, currentLine++,
					"height"));

			FileReaderHelper.readLineExpect(lines, currentLine++, "matrix");
			int[][] matrix = new int[height][width];
			for (int i = 0; i < height; ++i) {
				String[] splittedLine = lines.get(currentLine++).split(" ");
				for (int j = 0; j < width; ++j) {
					if (splittedLine[j].equals("x")) {
						matrix[i][j] = CraftResolverService.EMPTY_ELEMENT_ID;
					} else {
						int id = Integer.parseInt(splittedLine[j]);
						matrix[i][j] = id;
					}
				}
			}

			int resultAmount = Integer.parseInt(FileReaderHelper.readLineExpect(lines,
					currentLine++, "result-amount"));
			int resultId = Integer.parseInt(FileReaderHelper.readLineExpect(lines, currentLine++,
					"result-id"));

			craftResolverService.addCraft(matrix, new InventoryItem(resultId, resultAmount),
					canTranslate, canRotate);
		}
	}

	private void assertVersion(String version) {
		if (!version.equals(SUPPORTED_VERSION)) {
			throw new RuntimeException("Unsupported version");
		}
	}

	@Override
	public void parseXml(Element rootElement) {
		List<String> files = XmlHelpers.getStringList(rootElement, "file");
		for (int i = 0; i < files.size(); ++i) {
			loadCraftFile(files.get(i));
		}
	}
}
