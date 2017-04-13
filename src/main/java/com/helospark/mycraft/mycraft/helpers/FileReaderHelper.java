package com.helospark.mycraft.mycraft.helpers;

import java.util.List;

public class FileReaderHelper {
	public static String readLineExpect(List<String> fileContent, int lineNumber, String expected) {
		if (lineNumber >= fileContent.size()) {
			throw new RuntimeException("Unexpected end of line, expected " + expected);
		}
		String line = fileContent.get(lineNumber);
		int position = line.indexOf(" ");
		String result;
		if (position == -1) {
			result = line;
		} else {
			result = line.substring(0, position);
		}
		if (!result.equals(expected)) {
			throw new RuntimeException("Expected '" + expected + "' found '" + result
					+ "' at line " + lineNumber);
		}
		return line.substring(position + 1);
	}

	public static void readNextStringExpect() {

	}
}
