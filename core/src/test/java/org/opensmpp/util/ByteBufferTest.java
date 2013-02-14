package org.opensmpp.util;

import static org.junit.Assert.*;
import static org.opensmpp.util.NotEnoughDataInByteBufferExceptionMatcher.*;

import java.io.UnsupportedEncodingException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensmpp.util.ByteBuffer;
import org.opensmpp.util.NotEnoughDataInByteBufferException;
import org.opensmpp.util.TerminatingZeroNotFoundException;

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
		assertBufferMatches(new byte[] { t_bite });
	}

	@Test
	public void testAppendByte1() {
		buffer = new ByteBuffer(new byte[] {});
		buffer.appendByte(t_bite);
		assertBufferMatches(new byte[] { t_bite } );
	}

	@Test
	public void testAppendShort0() {
		buffer.appendShort(t_short);
		assertBufferMatches(new byte[] { 0x02, (byte) 0x9a});
	}

	@Test
	public void testAppendInt0() {
		buffer.appendInt(666);
		assertBufferMatches(new byte[] { 0x00, 0x00, 0x02, (byte) 0x9a });
	}

	@Test
	public void testAppendCString0() {
		buffer.appendCString(ABC);
		assertBufferMatches(new byte[] { A, B, C, NULL });
	}

	@Test
	public void testAppendCStringWithInvalidEncodingThrowsException() throws Exception {
		thrown.expect(UnsupportedEncodingException.class);
		buffer.appendCString(ABC, INVALID);
	}

	@Test
	public void testAppendString() {
		buffer.appendString(ABC);
		assertBufferMatches(new byte[] { A, B, C });
	}

	@Test
	public void testAppendStringWithCount() {
		buffer.appendString(ABC, 2);
		assertBufferMatches(new byte[] { A, B });
	}

	@Test
	public void testAppendStringWithZeroCountToNullBuffer() {
		buffer.appendString(ABC, 0);
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
	public void testAppendBytesWithCountHonoursCount() {
		buffer.appendBytes(new byte[] { 0x01, 0x02, 0x03 }, 2);
		assertBufferMatches(new byte[] { 0x01, 0x02 });
	}

	@Test
	public void testAppendBytesWithExcessiveCountReducesCount() {
		buffer.appendBytes(new byte[] { t_bite }, 1234);
		assertBufferMatches(new byte[] { t_bite });
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

		buffer.appendBytes(new ByteBuffer(new byte[] { t_bite }), 2);
	}

	@Test
	public void testAppendBufferWithNullDoesNothing() {
		buffer = new ByteBuffer(new byte[] { NULL });
		buffer.appendBuffer(null);
		assertBufferMatches(new byte[] { NULL });
	}

	@Test
	public void testAppendBufferWithEmptyDoesNothing() {
		buffer.appendBuffer(new ByteBuffer(new byte[] {}));
		assertNull(buffer.getBuffer());
	}

	@Test
	public void testAppendBuferAppendsAll() {
		buffer.appendBuffer(new ByteBuffer(new byte[] { A, B, C }));
		assertBufferMatches(new byte[] { A, B, C });
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
		buffer = new ByteBuffer(new byte[] { A, B, C });
		byte bite = buffer.removeByte();
		assertEquals(A, bite);
	}

	@Test
	public void testRemoveByteReducesBuffer() throws Exception {
		buffer = new ByteBuffer(new byte[] { A, B, C });
		buffer.removeByte();
		assertEquals(2, buffer.length());
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

		buffer = new ByteBuffer(new byte[] { 0x00 });
		buffer.removeShort();
	}

	@Test
	public void testRemoveShortRemovesFirstShort() throws Exception {
		buffer = new ByteBuffer(new byte[] { 0x01, 0x02, 0x03, 0x04 });
		short s = buffer.removeShort();
		assertEquals((1 << 8) + 2, s);
	}

	@Test
	public void testRemoveShortReducesBuffer() throws Exception {
		buffer = new ByteBuffer(new byte[] { 0x01, 0x02, 0x03, 0x04 });
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
		buffer = new ByteBuffer(new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 });
		int i = buffer.readInt();
		assertEquals((1 << 24) + (2 << 16) + (3 << 8) + 4, i);
	}

	@Test
	public void testReadIntDoesNotReduceBuffer() throws Exception {
		buffer = new ByteBuffer(new byte[] { 0x01, 0x02, 0x03, 0x04 });
		buffer.readInt();
		assertEquals(4, buffer.length());
	}

	@Test
	public void testReadIntFromSmallBufferThrowsException() throws Exception {
		thrown.expect(NotEnoughDataInByteBufferException.class);
		thrown.expect(notEnoughData(4, 2));

		buffer = new ByteBuffer(new byte[] { 0x00, 0x00 });
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

		buffer = new ByteBuffer(new byte[] { 0x00, 0x00 });
		buffer.removeInt();
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

		buffer = new ByteBuffer(new byte[] { 0x01 });
		buffer.removeCString();
	}

	@Test
	public void testRemoveCStringWithMultipleNonTerminatorThrowsException() throws Exception {
		thrown.expect(TerminatingZeroNotFoundException.class);

		buffer = new ByteBuffer(new byte[] { 0x01, 0x01, 0x01, 0x01 });
		buffer.removeCString();
	}

	@Test
	public void testRemoveCStringWithSingleTerminator() throws Exception {
		buffer = new ByteBuffer(new byte[] { 0x00 });
		assertEquals("", buffer.removeCString());
		assertEquals(0, buffer.length());
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

	private void assertBufferMatches(byte[] expected) {
		assertArrayEquals(expected, this.buffer.getBuffer());
	}
}
