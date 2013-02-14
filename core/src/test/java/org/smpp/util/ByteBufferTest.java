package org.smpp.util;

import static org.junit.Assert.*;
import static org.smpp.util.NotEnoughDataInByteBufferExceptionMatcher.*;

import java.io.UnsupportedEncodingException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.smpp.util.ByteBuffer;
import org.smpp.util.NotEnoughDataInByteBufferException;
import org.smpp.util.TerminatingZeroNotFoundException;

public class ByteBufferTest {

	private static final String ABC = "ABC";
	private static final String ASCII = "ASCII";
	private static final String INVALID = "INVALID";
	private static final byte NULL = 0x00;
	private static final byte A = 0x41;
	private static final byte B = 0x42;
	private static final byte C = 0x43;

	private ByteBuffer buffer;
	private byte t_bite = (byte) 0x1f;
	private short t_short = (short) 666;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setup() {
		buffer = new ByteBuffer();
	}

	@Test
	public void testAppendByte0() {
		buffer.appendByte(t_bite);
		assertBufferMatches(t_bite);
	}

	@Test
	public void testAppendByte1() {
		buffer = new ByteBuffer(new byte[] {});
		buffer.appendByte(t_bite);
		assertBufferMatches(t_bite);
	}

	@Test
	public void testAppendShort0() {
		buffer.appendShort(t_short);
		assertBufferMatches((byte) 0x02, (byte) 0x9a);
	}

	@Test
	public void testAppendInt0() {
		buffer.appendInt(666);
		assertBufferMatches(NULL, NULL, (byte) 0x02, (byte) 0x9a);
	}

	@Test
	public void testAppendCString0() {
		buffer.appendCString(ABC);
		assertBufferMatches(A, B, C, NULL);
	}

	@Test
	public void testAppendCStringWithNullAppendsNull() {
		buffer.appendCString(null);
		assertBufferMatches(NULL);
	}

	@Test
	public void testAppendCStringWithInvalidEncodingThrowsException() throws Exception {
		thrown.expect(UnsupportedEncodingException.class);
		buffer.appendCString(ABC, INVALID);
	}

	@Test
	public void testAppendString() {
		buffer.appendString(ABC);
		assertBufferMatches(A, B, C);
	}

	@Test
	public void testAppendStringWithCount() {
		buffer.appendString(ABC, 2);
		assertBufferMatches(A, B);
	}

	@Test
	public void testAppendStringWithZeroCountToNullBuffer() {
		buffer.appendString(ABC, 0);
		assertNull(buffer.getBuffer());
	}

	@Test
	public void testAppendStringWithNullDoesNothing() {
		buffer.appendString(null);
		assertNull(buffer.getBuffer());
	}

	@Test
	public void testAppendStringWithZeroCount() {
		buffer = new ByteBuffer(new byte[] { });
		buffer.appendString(ABC, 0);
		assertBufferMatches(new byte[] { });
	}

	@Test
	public void testAppendStringWithExcessiveCountThrowsException() throws Exception {
		thrown.expect(StringIndexOutOfBoundsException.class);
		buffer.appendString(ABC, 4);
	}

	@Test
	public void testAppenStringWithCountAndInvalidEncodingThrowsException() throws Exception {
		thrown.expect(UnsupportedEncodingException.class);
		buffer.appendString(ABC, 1, INVALID);
	}

	@Test
	public void testAppendBytesWithNullDoesNothing() {
		buffer.appendBytes(null);
		assertNull(buffer.getBuffer());
	}

	@Test
	public void testAppendBytesWithNullAndCountDoesNothing() {
		buffer.appendBytes((byte[]) null, 1234);
		assertNull(buffer.getBuffer());
	}

	@Test
	public void testAppendBytes() {
		buffer.appendBytes(new byte[] { A, B, C });
		assertBufferMatches(A, B, C);
	}

	@Test
	public void testAppendBytesWithCountHonoursCount() {
		buffer.appendBytes(new byte[] { 0x01, 0x02, 0x03 }, 2);
		assertBufferMatches((byte) 0x01, (byte) 0x02);
	}

	@Test
	public void testAppendBytesWithExcessiveCountReducesCount() {
		buffer.appendBytes(new byte[] { t_bite }, 1234);
		assertBufferMatches(t_bite);
	}

	@Test
	public void testAppendByteBufferWithNullThrowsException() throws Exception {
		thrown.expect(NotEnoughDataInByteBufferException.class);
		thrown.expect(notEnoughData(1234, 0));

		buffer.appendBytes((ByteBuffer) null, 1234);
	}

	@Test
	public void testAppendByteBufferWithExcessiveCountThrowsException() throws Exception {
		thrown.expect(NotEnoughDataInByteBufferException.class);
		thrown.expect(notEnoughData(2, 1));

		buffer.appendBytes(bufferOf(t_bite), 2);
	}

	@Test
	public void testAppendBufferWithNullDoesNothing() {
		buffer = bufferOf(NULL);
		buffer.appendBuffer(null);
		assertBufferMatches(NULL);
	}

	@Test
	public void testAppendBufferWithEmptyDoesNothing() {
		buffer.appendBuffer(new ByteBuffer(new byte[] {}));
		assertNull(buffer.getBuffer());
	}

	@Test
	public void testAppendBuferAppendsAll() {
		buffer.appendBuffer(bufferOf(A, B, C));
		assertBufferMatches(A, B, C);
	}

	@Test
	public void testReadBytesWithZeroCountReturnsNull() throws Exception {
		assertNull(buffer.readBytes(0));
	}

	@Test
	public void testReadBytesWithCountReadFirst() throws Exception {
		buffer = bufferOf(A, B, C);
		ByteBuffer b = buffer.readBytes(2);
		assertArrayEquals(new byte[] { A, B}, b.getBuffer());
	}

	@Test
	public void testReadBytesWithCountDoesNotReduceBuffer() throws Exception {
		buffer = bufferOf(A, B, C);
		buffer.readBytes(2);
		assertEquals(3, buffer.length());
	}

	@Test
	public void testReadBytesWithExcessiveCountThrowsException() throws Exception {
		thrown.expect(NotEnoughDataInByteBufferException.class);
		thrown.expect(notEnoughData(1, 0));

		buffer.readBytes(1);
	}

	@Test
	public void testRemoveByteFromNullThrowsException() throws Exception {
		thrown.expect(NotEnoughDataInByteBufferException.class);
		thrown.expect(notEnoughData(1, 0));

		buffer.removeByte();
	}

	@Test
	public void testRemoveByteFromEmptyThrowsException() throws Exception {
		thrown.expect(NotEnoughDataInByteBufferException.class);
		thrown.expect(notEnoughData(1, 0));

		buffer = new ByteBuffer(new byte[] { });
		buffer.removeByte();
	}

	@Test
	public void testRemoveByteRemovesFirstByte() throws Exception {
		buffer = bufferOf(A, B, C);
		byte bite = buffer.removeByte();
		assertEquals(A, bite);
	}

	@Test
	public void testRemoveByteReducesBuffer() throws Exception {
		buffer = bufferOf(A, B, C );
		buffer.removeByte();
		assertEquals(2, buffer.length());
	}

	@Test
	public void testRemoveBytes0AssumesNegativeIsAll() throws Exception {
		buffer = bufferOf(A, B, C);
		buffer.removeBytes0(-1);
		assertEquals(0, buffer.length());
	}

	@Test
	public void testRemoveShortFromNullThrowsException() throws Exception {
		thrown.expect(NotEnoughDataInByteBufferException.class);
		thrown.expect(notEnoughData(2, 0));

		buffer.removeShort();
	}

	@Test
	public void testRemoveShortFromSmallBufferThrowsException() throws Exception {
		thrown.expect(NotEnoughDataInByteBufferException.class);
		thrown.expect(notEnoughData(2, 1));

		buffer = bufferOf(NULL);
		buffer.removeShort();
	}

	@Test
	public void testRemoveShortRemovesFirstShort() throws Exception {
		buffer = bufferOf((byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04);
		short s = buffer.removeShort();
		assertEquals((1 << 8) + 2, s);
	}

	@Test
	public void testRemoveShortReducesBuffer() throws Exception {
		buffer = bufferOf((byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04);
		buffer.removeShort();
		assertEquals(2, buffer.length());
	}

	@Test
	public void testReadIntFromNullThrowsException() throws Exception {
		thrown.expect(NotEnoughDataInByteBufferException.class);
		thrown.expect(notEnoughData(4, 0));

		buffer.readInt();
	}

	@Test
	public void testReadIntReadsFirstInt() throws Exception {
		buffer = bufferOf((byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08);
		int i = buffer.readInt();
		assertEquals((1 << 24) + (2 << 16) + (3 << 8) + 4, i);
	}

	@Test
	public void testReadIntDoesNotReduceBuffer() throws Exception {
		buffer = bufferOf((byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04);
		buffer.readInt();
		assertEquals(4, buffer.length());
	}

	@Test
	public void testReadIntFromSmallBufferThrowsException() throws Exception {
		thrown.expect(NotEnoughDataInByteBufferException.class);
		thrown.expect(notEnoughData(4, 2));

		buffer = bufferOf(NULL, NULL);
		buffer.readInt();
	}

	@Test
	public void testRemoveIntFromNullThrowsException() throws Exception {
		thrown.expect(NotEnoughDataInByteBufferException.class);
		thrown.expect(notEnoughData(4, 0));

		buffer.removeInt();
	}

	@Test
	public void testRemoveIntFromSmallBufferThrowsException() throws Exception {
		thrown.expect(NotEnoughDataInByteBufferException.class);
		thrown.expect(notEnoughData(4, 2));

		buffer = bufferOf(NULL, NULL);
		buffer.removeInt();
	}

	@Test
	public void testRemoveIntReadsFirstInt() throws Exception {
		buffer = bufferOf((byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08);
		int i = buffer.removeInt();
		assertEquals((1 << 24) + (2 << 16) + (3 << 8) + 4, i);
	}

	@Test
	public void testRemoveIntReduceBuffer() throws Exception {
		buffer = bufferOf((byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04);
		buffer.removeInt();
		assertEquals(0, buffer.length());
	}

	@Test
	public void testRemoveCStringFromNullThrowsException() throws Exception {
		thrown.expect(NotEnoughDataInByteBufferException.class);
		thrown.expect(notEnoughData(1, 0));

		buffer.removeCString();
	}

	@Test
	public void testRemoveCStringFromEmptyThrowsException() throws Exception {
		thrown.expect(NotEnoughDataInByteBufferException.class);
		thrown.expect(notEnoughData(1, 0));

		buffer = new ByteBuffer(new byte[] {});
		buffer.removeCString();
	}

	@Test
	public void testRemoveCStringWithSingleNonTerminatorThrowsException() throws Exception {
		thrown.expect(TerminatingZeroNotFoundException.class);

		buffer = bufferOf((byte) 0x01);
		buffer.removeCString();
	}

	@Test
	public void testRemoveCStringWithMultipleNonTerminatorThrowsException() throws Exception {
		thrown.expect(TerminatingZeroNotFoundException.class);

		buffer = bufferOf((byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01);
		buffer.removeCString();
	}

	@Test
	public void testRemoveCStringWithSingleTerminator() throws Exception {
		buffer = bufferOf(NULL);
		assertEquals("", buffer.removeCString());
		assertEquals(0, buffer.length());
	}

	@Test
	public void testRemoveCStringRemovesFirstString() throws Exception {
		buffer = bufferOf(A, B, NULL, C, NULL);
		assertEquals("AB", buffer.removeCString());
	}

	@Test
	public void testRemoveCStringReducesBuffer() throws Exception {
		buffer = bufferOf(A, B, NULL, C, NULL);
		buffer.removeCString();
		assertEquals(2, buffer.length());
	}

	@Test
	public void testRemoveStringWithNullThrowsException() throws Exception {
		thrown.expect(NotEnoughDataInByteBufferException.class);
		thrown.expect(notEnoughData(1, 0));

		buffer.removeString(1, ASCII);
	}

	@Test
	public void testRemoveStringZeroLength() throws Exception {
		assertEquals("", buffer.removeString(0, ASCII));
	}

	@Test
	public void testRemoveStringWithInvalidEncodingThrowsException() throws Exception {
		thrown.expect(UnsupportedEncodingException.class);

		buffer = bufferOf(A, B, C);
		buffer.removeString(3, INVALID);
	}

	@Test
	public void testRemoveStringReducesBuffer() throws Exception {
		buffer = bufferOf(A, B, C);
		buffer.removeString(3, ASCII);
		assertEquals(0, buffer.length());
	}

	@Test
	public void testRemoveStringWithEncodingAscii() throws Exception {
		buffer = bufferOf(A, B, C);
		assertEquals(ABC, buffer.removeString(3, ASCII));
	}

	@Test
	public void testRemoveStringWithNullEncoding() throws Exception {
		buffer = bufferOf(A, B, C);
		assertEquals(ABC, buffer.removeString(3, null));
	}

	@Test
	public void testRemoveBufferFromNullThrowsException() throws Exception {
		thrown.expect(NotEnoughDataInByteBufferException.class);
		thrown.expect(notEnoughData(1, 0));

		buffer.readBytes(1);
	}

	@Test
	public void testRemoveBufferWithExcessiveSizeThrowsException() throws Exception {
		thrown.expect(NotEnoughDataInByteBufferException.class);
		thrown.expect(notEnoughData(10, 1));

		buffer = bufferOf(NULL);
		buffer.removeBuffer(10);
	}

	@Test
	public void testRemoveBufferRemovesFirst() throws Exception {
		buffer = bufferOf(A, B, C);
		ByteBuffer b = buffer.removeBuffer(2);
		assertArrayEquals(new byte[] { A, B }, b.getBuffer());
		assertBufferMatches(C);
	}
	@Test
	public void testRemoveBufferReducesBuffer() throws Exception {
		buffer = bufferOf(A, B, C);
		buffer.removeBuffer(3);
		assertEquals(0, buffer.length());
	}

	@Test
	public void testHexDump() {
		assertEquals("414243", bufferOf(A, B, C).getHexDump());
	}

	@Test
	public void testHexDumpWithNullBuffer() {
		assertEquals("", buffer.getHexDump());
	}

	private static ByteBuffer bufferOf(byte... bytes) {
		return new ByteBuffer(bytes);
	}

	private void assertBufferMatches(byte... expected) {
		assertArrayEquals(expected, this.buffer.getBuffer());
	}
}
