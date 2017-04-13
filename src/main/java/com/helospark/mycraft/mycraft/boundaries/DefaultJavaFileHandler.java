package com.helospark.mycraft.mycraft.boundaries;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.helospark.mycraft.mycraft.services.FilePosition;

@Service
public class DefaultJavaFileHandler {
	private Map<Integer, RandomAccessFile> openedFiles = new HashMap<>();
	private int fileIdGenerator = 0;

	public DefaultJavaFileHandler() {
	}

	public String getStringFromFile(String fileName) {
		try {
			return getLinesFromFile(fileName);
		} catch (IOException e) {
			throw new RuntimeException("Unable to load file " + e);
		}
	}

	private String getLinesFromFile(String fileName) throws IOException {
		List<String> result = getLineListFromFile(fileName);
		StringBuilder stringBuilder = new StringBuilder();
		for (String str : result) {
			stringBuilder.append(str);
			stringBuilder.append("\n");
		}
		return stringBuilder.toString();
	}

	public InputStream toStream(String fileName) {
		FileInputStream stream;
		try {
			stream = new FileInputStream(new File(fileName));
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Unable to open file as stream", e);
		}
		return stream;
	}

	public List<String> getLineListFromFile(String fileName) {
		List<String> result;
		try {
			result = Files.readAllLines(Paths.get(fileName));
		} catch (IOException e) {
			throw new RuntimeException("Unable to open file " + fileName);
		}
		return result;
	}

	public int openFileForBinaryAccess(String pointerTableName) {
		File file = new File(pointerTableName);

		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				throw new RuntimeException("Unable to create file");
			}
		}

		RandomAccessFile randomAccessFile;
		try {
			randomAccessFile = new RandomAccessFile(file, "rw");
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Unable to access file");
		}
		++fileIdGenerator;
		openedFiles.put(fileIdGenerator, randomAccessFile);

		return fileIdGenerator;
	}

	public boolean eof(int fileId) {
		RandomAccessFile randomAccessFile = getFileFromPointer(fileId);
		try {
			return randomAccessFile.getFilePointer() >= randomAccessFile.length();
		} catch (IOException e) {
			throw new RuntimeException("Unable to get eof");
		}
	}

	private RandomAccessFile getFileFromPointer(int fileId) {
		RandomAccessFile randomAccessFile = openedFiles.get(fileId);
		if (randomAccessFile == null) {
			throw new RuntimeException("File not found");
		}
		return randomAccessFile;
	}

	public void closeFile(int fileId) {
		RandomAccessFile randomAccessFile = getFileFromPointer(fileId);
		try {
			randomAccessFile.close();
		} catch (IOException e) {
			throw new RuntimeException("Unable to close file");
		}
	}

	public int readBinaryInt(int fileId) {
		RandomAccessFile randomAccessFile = getFileFromPointer(fileId);
		int data;
		try {
			data = randomAccessFile.readInt();
		} catch (IOException e) {
			throw new RuntimeException("Unable to read integer");
		}

		return data;
	}

	public long writeBinaryInt(int fileId, int data) {
		RandomAccessFile randomAccessFile = getFileFromPointer(fileId);
		long position;
		try {
			position = randomAccessFile.getFilePointer();
			randomAccessFile.writeInt(data);
		} catch (IOException e) {
			throw new RuntimeException("Unable to write integer");
		}
		return position;
	}

	public long writeBinaryLong(int fileId, long data) {
		RandomAccessFile randomAccessFile = getFileFromPointer(fileId);
		long position;
		try {
			position = randomAccessFile.getFilePointer();
			randomAccessFile.writeLong(data);
		} catch (IOException e) {
			throw new RuntimeException("Unable to write integer");
		}
		return position;
	}

	public void positionPointer(int fileId, FilePosition position) {
		RandomAccessFile randomAccessFile = getFileFromPointer(fileId);
		try {
			switch (position) {
			case END_OF_FILE:
				randomAccessFile.seek(randomAccessFile.length());

				break;
			case START_OF_FILE:
				randomAccessFile.seek(0);
				break;
			}
		} catch (IOException e) {
			throw new RuntimeException("Not able to set position");
		}
	}

	public long writeBinaryData(int fileId, byte[] data) {
		RandomAccessFile randomAccessFile = getFileFromPointer(fileId);
		long position;
		try {
			position = randomAccessFile.getFilePointer();
			randomAccessFile.write(data);
		} catch (IOException e) {
			throw new RuntimeException("Unable to write to file");
		}
		return position;
	}

	public long readBinaryLong(int fileId) {
		RandomAccessFile randomAccessFile = getFileFromPointer(fileId);
		long data;
		try {
			data = randomAccessFile.readLong();
		} catch (IOException e) {
			throw new RuntimeException("Unable to read integer");
		}

		return data;
	}

	public void positionPointer(int fileId, long pointer) {
		RandomAccessFile randomAccessFile = getFileFromPointer(fileId);
		try {
			randomAccessFile.seek(pointer);
		} catch (IOException e) {
			throw new RuntimeException("Unable to seek to given position");
		}
	}

	public byte[] readData(int fileId, int serializedSize) {
		RandomAccessFile randomAccessFile = getFileFromPointer(fileId);
		byte[] result = new byte[serializedSize];
		try {
			randomAccessFile.read(result);
		} catch (IOException e) {
			throw new RuntimeException("Unable to read that many bytes");
		}
		return result;
	}

	public void deleteFile(String fileName) {
		File file = new File(fileName);
		if (file.exists()) {
			file.delete();
		}
	}
}
