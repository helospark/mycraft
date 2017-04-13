package com.helospark.mycraft.mycraft;

import junit.framework.Assert;

import org.junit.Test;

import com.helospark.mycraft.mycraft.helpers.SerializationHelpers;

public class SerializationHelpersTest {

	@Test
	public void testSerializeIntegerIntoArrayAtPositionLastByteShouldBeNonZeroWhenNumberCanBeRepresentedInAByte() {
		// GIVEN
		byte[] result = new byte[4];
		// WHEN
		SerializationHelpers.serializeIntegerIntoArrayAtPosition(result, 0, 100);
		// THEN
		Assert.assertEquals(100, result[3]);
	}

	@Test
	public void testSerializedThenDeserializedIntegerShouldReturnTheSame() {
		// GIVEN
		byte[] result = new byte[4];
		// WHEN
		SerializationHelpers.serializeIntegerIntoArrayAtPosition(result, 0, 123);
		int actual = SerializationHelpers.deserializeIntegerFromArray(result, 0);
		// THEN
		Assert.assertEquals(123, actual);
	}

	@Test
	public void testSerializedThenDeserializedIntegerShouldReturnTheSameWhenNumbersAreLarge() {
		// GIVEN
		byte[] result = new byte[4];
		int bigNumber = 12345;
		// WHEN
		SerializationHelpers.serializeIntegerIntoArrayAtPosition(result, 0, bigNumber);
		int actual = SerializationHelpers.deserializeIntegerFromArray(result, 0);
		// THEN
		Assert.assertEquals(bigNumber, actual);
	}
}
