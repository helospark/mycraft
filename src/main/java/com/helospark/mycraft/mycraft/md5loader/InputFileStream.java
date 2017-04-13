package com.helospark.mycraft.mycraft.md5loader;

import java.util.ArrayList;
import java.util.List;

import com.helospark.mycraft.mycraft.boundaries.DefaultJavaFileHandler;
import com.helospark.mycraft.mycraft.singleton.Singleton;

public class InputFileStream {
	List<List<String>> lines = new ArrayList<>();
	int currentLine = 0;
	int currentWord = 0;

	DefaultJavaFileHandler fileHandler;
	boolean isOpen = true;

	public InputFileStream(String fileName) {
		fileHandler = Singleton.getInstance().getContext().getBean(DefaultJavaFileHandler.class);
		List<String> unbrokenLines = null;
		try {
			unbrokenLines = fileHandler.getLineListFromFile(fileName);
		} catch (Exception e) {
			isOpen = false;
		}
		if (isOpen) {
			for (int i = 0; i < unbrokenLines.size(); ++i) {
				String[] brokenLine = unbrokenLines.get(i).split("\\s+");
				List<String> words = new ArrayList<>();
				for (int j = 0; j < brokenLine.length; ++j) {
					brokenLine[j] = brokenLine[j].trim();
					if (brokenLine[j].length() > 0) {
						words.add(brokenLine[j]);
					}
				}
				if (words.size() > 0) {
					lines.add(words);
				}
			}
		}
	}

	public String getNext() {
		if (currentLine >= lines.size()) {
			return "";
		}
		List<String> words = lines.get(currentLine);
		if (currentWord >= words.size()) {
			currentLine++;
			currentWord = 0;
		}
		if (currentLine >= lines.size()) {
			return "";
		}
		words = lines.get(currentLine);
		String word = words.get(currentWord);
		currentWord++;
		return word;
	}

	public void skipLine() {
		++currentLine;
		currentWord = 0;
	}

	public void skipWord() {
		++currentWord;
	}

	public boolean is_open() {
		return isOpen;
	}
}
