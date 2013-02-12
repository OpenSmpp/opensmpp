package org.opensmpp.util;

import static org.junit.Assert.*;
import static org.opensmpp.util.NotEnoughDataInByteBufferExceptionMatcher.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensmpp.util.ByteBuffer;
import org.opensmpp.util.NotEnoughDataInByteBufferException;
import org.opensmpp.util.TerminatingZeroNotFoundException;

public class ByteBufferTest {

	private static final String ASCII = "ASCII";

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
		buffer.appendCString("ABC");
		assertBufferMatches(new byte[] { 0x41, 0x42, 0x43, 0x00 });
	}

	@Test
	public void testAppendString() {
		buffer.appendString("ABC");
		assertBufferMatches(new byte[] { 0x41, 0x42, 0x43 });
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
	public void testReadIntFromNullThrowsException() throws Exception {
		thrown.expect(NotEnoughDataInByteBufferException.class);
		thrown.expect(notEnoughData(4, 0));

		buffer.readInt();
	}

	@Test
	public void testReadInFromSmallBufferThrowsException() throws Exception {
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
