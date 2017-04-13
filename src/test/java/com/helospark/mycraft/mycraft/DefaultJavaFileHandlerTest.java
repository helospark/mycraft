package com.helospark.mycraft.mycraft;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.helospark.mycraft.mycraft.boundaries.DefaultJavaFileHandler;
import com.helospark.mycraft.mycraft.services.FilePosition;

public class DefaultJavaFileHandlerTest {

	private static final String TEST_FILE_NAME = "test.ttt";
	DefaultJavaFileHandler fileHandler = new DefaultJavaFileHandler();
	int fileId;

	@Before
	public void setUp() {
		fileHandler.deleteFile(TEST_FILE_NAME);
		fileId = fileHandler.openFileForBinaryAccess(TEST_FILE_NAME);
	}

	@After
	public void tearDown() {
		fileHandler.closeFile(fileId);
	}

	@Test
	public void testWriteBinaryIntThanReadBackShouldBeEqual() {
		// GIVEN
		fileHandler.writeBinaryInt(fileId, 123);
		// WHEN
		fileHandler.positionPointer(fileId, FilePosition.START_OF_FILE);
		int actual = fileHandler.readBinaryInt(fileId);
		// THEN
		Assert.assertEquals(actual, 123);
	}

	@Test
	public void testWriteBinaryIntThanReadBackShouldBeEqualWhenTheNumberIsLarge() {
		// GIVEN
		fileHandler.writeBinaryInt(fileId, 1234567);
		// WHEN
		fileHandler.positionPointer(fileId, FilePosition.START_OF_FILE);
		int actual = fileHandler.readBinaryInt(fileId);
		// THEN
		Assert.assertEquals(actual, 1234567);
	}

	@Test
	public void testWriteBinaryLongThanReadBackShouldBeEqualWhenTheNumberIsLarge() {
		// GIVEN
		fileHandler.writeBinaryLong(fileId, 1234567891011L);
		// WHEN
		fileHandler.positionPointer(fileId, FilePosition.START_OF_FILE);
		long actual = fileHandler.readBinaryLong(fileId);
		// THEN
		Assert.assertEquals(actual, 1234567891011L);
	}

	@Test
	public void testWriteMultipleIntegerThenReadThemBack() {
		// GIVEN
		fileHandler.writeBinaryInt(fileId, 1);
		fileHandler.writeBinaryInt(fileId, 2);
		fileHandler.writeBinaryInt(fileId, 3);
		fileHandler.writeBinaryInt(fileId, 4);
		// WHEN
		fileHandler.positionPointer(fileId, FilePosition.START_OF_FILE);
		int a = fileHandler.readBinaryInt(fileId);
		int b = fileHandler.readBinaryInt(fileId);
		int c = fileHandler.readBinaryInt(fileId);
		int d = fileHandler.readBinaryInt(fileId);
		// THEN
		Assert.assertEquals(a, 1);
		Assert.assertEquals(b, 2);
		Assert.assertEquals(c, 3);
		Assert.assertEquals(d, 4);
	}

	@Test
	public void testPositionAtEndOfFileAfterWriteAndWriteAgainShouldNotOverridePrevious() {
		// GIVEN
		fileHandler.writeBinaryInt(fileId, 1);
		fileHandler.positionPointer(fileId, FilePosition.END_OF_FILE);
		fileHandler.writeBinaryInt(fileId, 2);
		// WHEN
		fileHandler.positionPointer(fileId, FilePosition.START_OF_FILE);
		int a = fileHandler.readBinaryInt(fileId);
		int b = fileHandler.readBinaryInt(fileId);
		// THEN
		Assert.assertEquals(a, 1);
		Assert.assertEquals(b, 2);
	}

	@Test
	public void testWriteByteArrayShouldReadBackSameArray() {
		// GIVEN
		final int size = 3;
		byte[] data = new byte[3];
		data[0] = 1;
		data[1] = 2;
		data[2] = 3;
		fileHandler.writeBinaryData(fileId, data);
		// WHEN
		fileHandler.positionPointer(fileId, FilePosition.START_OF_FILE);
		byte[] actual = fileHandler.readData(fileId, size);
		// THEN
		Assert.assertEquals(actual[0], data[0]);
		Assert.assertEquals(actual[1], data[1]);
		Assert.assertEquals(actual[2], data[2]);
	}

	@Test
	public void testWriteBinaryDataAtRandomPosition() {
		// GIVEN
		fileHandler.writeBinaryInt(fileId, 1);
		long positionToOverride = fileHandler.writeBinaryInt(fileId, 2);
		fileHandler.writeBinaryInt(fileId, 3);
		fileHandler.positionPointer(fileId, positionToOverride);
		fileHandler.writeBinaryInt(fileId, 4);
		// WHEN
		fileHandler.positionPointer(fileId, FilePosition.START_OF_FILE);
		int a = fileHandler.readBinaryInt(fileId);
		int b = fileHandler.readBinaryInt(fileId);
		int c = fileHandler.readBinaryInt(fileId);
		// THEN
		Assert.assertEquals(a, 1);
		Assert.assertEquals(b, 4);
		Assert.assertEquals(c, 3);
	}
}
