package org.smpp.pdu;

import static org.junit.Assert.*;
import static org.powermock.reflect.Whitebox.*;
import static org.smpp.pdu.Matchers.*;

import java.io.UnsupportedEncodingException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.powermock.reflect.Whitebox;

public class ByteDataTest {
	private static Class<?> CLAZZ = ByteData.class;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testCheckStringEqualZero() throws Exception {
		checkString("", 0);
	}
	@Test
	public void testCheckStringNonZero() throws Exception {
		checkString(" ", 1);
	}
	@Test
	public void testCheckStringOneZero() throws Exception {
		thrown.expect(stringLengthException(0, 0, 1));
		checkString(" ", 0);
	}

	@Test
	public void testCheckStringUTF16() throws Exception {
		thrown.expect(stringLengthException(0, 1, 4));
		checkString(" ", 1, "UTF-16");
	}
	@Test
	public void testCheckStringInvalidEncoding() throws Exception {
		thrown.expect(UnsupportedEncodingException.class);
		checkString(" ", 1, "UTF-17");
	}

	@Test
	public void testCheckCString() throws Exception {
		thrown.expect(stringLengthException(0, 0, 1));
		invokeMethod(CLAZZ, "checkCString", (String) null, 0, 0);
	}

	@Test
	public void testCheckRange() throws Exception {
		checkRange(0, 0, 0);
		checkRange(0, 10, 100);
	}
	@Test
	public void testCheckRangeBelow() throws Exception {
		thrown.expect(integerOutOfRange(5, 10, 1));
		checkRange(5, 1, 10);
	}
	@Test
	public void testCheckRangeAbove() throws Exception {
		thrown.expect(integerOutOfRange(5, 10, 11));
		checkRange(5, 11, 10);
	}

	@Test
	public void testDecodeUnsignedByte() throws Exception {
		assertEquals(0, decodeUnsigned((byte) 0x00));
		assertEquals(127, decodeUnsigned((byte) 0x7f));
		assertEquals(255, decodeUnsigned((byte) 0xff));
	}
	@Test
	public void testDecodeUnsignedShort() throws Exception {
		assertEquals(0, decodeUnsigned((short) 0));
		assertEquals(32768, decodeUnsigned((short) 32768));
	}
	@Test
	public void testEncodeUnsignedShort() throws Exception {
		assertEquals((byte) 0x00, encodeUnsigned((short) 0));
		assertEquals((byte) 0xff, encodeUnsigned((short) 255));
	}
	@Test
	public void testEncodeUnsignedInt() throws Exception {
		assertEquals((short) 0, encodeUnsigned((int) 0));
		assertEquals((short) 32768, encodeUnsigned((int) 32768));
	}

	// FIXME: plenty more tests to write here

	// maps to ByteData static methods

	private void checkString(String string, int max) throws Exception {
		invokeMethod(CLAZZ, "checkString", string, max);
	}
	private void checkString(String string, int min, int max) throws Exception {
		invokeMethod(CLAZZ, "checkString", string, min, max);
	}
	private void checkString(String string, int max, String encoding) throws Exception {
		invokeMethod(CLAZZ, "checkString", string, max, encoding);
	}
	private void checkString(String string, int min, int max, String encoding) throws Exception {
		invokeMethod(CLAZZ, "checkString", string, min, max, encoding);
	}
	private void checkString(int min, int length, int max) throws Exception {
		invokeMethod(CLAZZ, "checkString", min, length, max);
	}

	private void checkCString(String string, int max) throws Exception {
		invokeMethod(CLAZZ, "checkCString", string, max);
	}
	private void checkCString(String string, int min, int max) throws Exception {
		invokeMethod(CLAZZ, "checkCString", string, min, max);
	}

	private void checkDate(String string) throws Exception {
		invokeMethod(CLAZZ, "checkDate", string);
	}

	private void checkRange(int min, int value, int max) throws Exception {
		invokeMethod(CLAZZ, "checkRange", min, value, max);
	}

	private short decodeUnsigned(byte bite) throws Exception {
		return Whitebox.<Short> invokeMethod(CLAZZ, "decodeUnsigned", bite);
	}
	private int decodeUnsigned(short value) throws Exception {
		return Whitebox.<Integer> invokeMethod(CLAZZ, "decodeUnsigned", value);
	}
	private byte encodeUnsigned(short value) throws Exception {
		return Whitebox.<Byte> invokeMethod(CLAZZ, "encodeUnsigned", value);
	}
	private short encodeUnsigned(int positive) throws Exception {
		return Whitebox.<Short> invokeMethod(CLAZZ, "encodeUnsigned", positive);
	}
}
