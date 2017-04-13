package com.helospark.mycraft.mycraft.boundaries;

import java.io.InputStream;

public interface FileHandler {
	enum FilePositon {
		END_OF_FILE,
	}

	public String getStringFromFile(String fileName);

	public InputStream toStream(String fileName);
}
