package com.helospark.mycraft.mycraft.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.helospark.mycraft.mycraft.boundaries.DefaultJavaFileHandler;
import com.helospark.mycraft.mycraft.game.Chunk;
import com.helospark.mycraft.mycraft.mathutils.IntVector;

@Service
public class ChunkFileHandlerService {
	private static final int NUMBER_OF_COORDINATES = 3;
	private static final String FILE_NAME = "chunks.chu";
	private static final String POINTER_TABLE_NAME = "pointer.poi";

	// hashCode,
	private static final int CHUNK_SIZE = Integer.BYTES + NUMBER_OF_COORDINATES * Integer.BYTES
			+ Chunk.CHUNK_BLOCK_SIZE * Chunk.CHUNK_BLOCK_SIZE * Chunk.CHUNK_BLOCK_SIZE;
	private DefaultJavaFileHandler fileHandler;
	private int chunkFileId;
	private int pointerFileId;
	private static final int START_TABLE_POSITION = 0;

	HashMap<IntVector, Long> positionToPointer = new HashMap<>();

	@Autowired
	public ChunkFileHandlerService(DefaultJavaFileHandler fileHandler) {
		this.fileHandler = fileHandler;
		// fileHandler.deleteFile(FILE_NAME);
		// fileHandler.deleteFile(POINTER_TABLE_NAME);
		chunkFileId = fileHandler.openFileForBinaryAccess(FILE_NAME);

		readPointerFileToMemory();

	}

	private void readPointerFileToMemory() {
		pointerFileId = fileHandler.openFileForBinaryAccess(POINTER_TABLE_NAME);
		while (!fileHandler.eof(pointerFileId)) {
			readPointerEntry();
		}
		fileHandler.closeFile(pointerFileId);
	}

	private void readPointerEntry() {
		int x = fileHandler.readBinaryInt(pointerFileId);
		int y = fileHandler.readBinaryInt(pointerFileId);
		int z = fileHandler.readBinaryInt(pointerFileId);
		long pointer = fileHandler.readBinaryLong(pointerFileId);
		positionToPointer.put(new IntVector(x, y, z), pointer);
	}

	public void savePointerFile() {
		pointerFileId = fileHandler.openFileForBinaryAccess(POINTER_TABLE_NAME);
		for (Map.Entry<IntVector, Long> entry : positionToPointer.entrySet()) {
			fileHandler.writeBinaryInt(pointerFileId, entry.getKey().x);
			fileHandler.writeBinaryInt(pointerFileId, entry.getKey().y);
			fileHandler.writeBinaryInt(pointerFileId, entry.getKey().z);
			fileHandler.writeBinaryLong(pointerFileId, entry.getValue());
		}
		fileHandler.closeFile(pointerFileId);
	}

	public void writeChunk(Chunk chunk) {
		IntVector position = chunk.getIntPosition();
		Long pointer = positionToPointer.get(position);
		System.out.println("SAVING " + position);
		byte[] chunkData = chunk.serialize();
		if (pointer == null) {
			fileHandler.positionPointer(chunkFileId, FilePosition.END_OF_FILE);
		} else {
			fileHandler.positionPointer(chunkFileId, pointer);
		}
		pointer = fileHandler.writeBinaryData(chunkFileId, chunkData);
		positionToPointer.put(new IntVector(position), pointer);
		savePointerFile();
	}

	public Chunk readChunk(IntVector position) {
		System.out.println("LOADING " + position);
		Long pointer = positionToPointer.get(position);
		if (pointer == null) {
			return null;
		}
		fileHandler.positionPointer(chunkFileId, pointer);
		byte[] data = fileHandler.readData(chunkFileId, Chunk.SERIALIZED_SIZE);
		return Chunk.deserialize(data);
	}

	public Chunk readChunk(int x, int y, int z) {
		return readChunk(new IntVector(x, y, z));
	}

	public boolean hasChunk(IntVector position) {
		Long pointer = positionToPointer.get(position);
		return pointer != null;
	}

	public boolean hasChunk(int x, int y, int z) {
		Long pointer = positionToPointer.get(new IntVector(x, y, z));
		return pointer != null;
	}
}
