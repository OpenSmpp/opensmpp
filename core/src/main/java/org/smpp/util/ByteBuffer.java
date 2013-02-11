/*
 * Copyright (c) 1996-2001
 * Logica Mobile Networks Limited
 * All rights reserved.
 *
 * This software is distributed under Logica Open Source License Version 1.0
 * ("Licence Agreement"). You shall use it and distribute only in accordance
 * with the terms of the License Agreement.
 *
 */
package org.smpp.util;

import java.io.UnsupportedEncodingException;

import org.smpp.Data;
import org.smpp.SmppObject;

/**
 * Data buffer for queued writting and reading of binary data.
 * Provides methods for appending several data types to the end of the buffer
 * and removing them from the begenning of the buffer. Used for constructing
 * and parsing binary PDUs.
 *
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.5 $
 */
public class ByteBuffer extends SmppObject {

	private byte[] buffer;

	private static final byte SZ_BYTE = 1;
	private static final byte SZ_SHORT = 2;
	private static final byte SZ_INT = 4;
	//private static final byte SZ_LONG = 8;

	private static byte[] zero;

	static {
		zero = new byte[1];
		zero[0] = 0;
	}

	public ByteBuffer() {
		buffer = null;
	}

	public ByteBuffer(byte[] buffer) {
		this.buffer = buffer;
	}

	public byte[] getBuffer() {
		return buffer;
	}

	public void setBuffer(byte[] buffer) {
		this.buffer = buffer;
	}

	public int length() {
		if (buffer == null) {
			return 0;
		} else {
			return buffer.length;
		}
	}

	/*
	private static int length(byte[] buffer) {
		if (buffer == null) {
			return 0;
		} else {
			return buffer.length;
		}
	}
	*/

	public void appendByte(byte data) {
		byte[] byteBuf = new byte[SZ_BYTE];
		byteBuf[0] = data;
		appendBytes0(byteBuf, SZ_BYTE);
	}

	public void appendShort(short data) {
		byte[] shortBuf = new byte[SZ_SHORT];
		shortBuf[1] = (byte) (data & 0xff);
		shortBuf[0] = (byte) ((data >>> 8) & 0xff);
		appendBytes0(shortBuf, SZ_SHORT);
	}

	public void appendInt(int data) {
		byte[] intBuf = new byte[SZ_INT];
		intBuf[3] = (byte) (data & 0xff);
		intBuf[2] = (byte) ((data >>> 8) & 0xff);
		intBuf[1] = (byte) ((data >>> 16) & 0xff);
		intBuf[0] = (byte) ((data >>> 24) & 0xff);
		appendBytes0(intBuf, SZ_INT);
	}

	public void appendCString(String string) {
		try {
			appendString0(string, true, Data.ENC_ASCII);
		} catch (UnsupportedEncodingException e) {
                        // this can't happen as we use ASCII encoding
                        // whatever is in the buffer it gets interpreted as ascii
		}
	}

	public void appendCString(String string, String encoding) throws UnsupportedEncodingException {
		appendString0(string, true, encoding);
	}

	public void appendString(String string) {
		try {
			appendString(string, Data.ENC_ASCII);
		} catch (UnsupportedEncodingException e) {
                        // this can't happen as we use ASCII encoding
                        // whatever is in the buffer it gets interpreted as ascii
		}
	}

	public void appendString(String string, String encoding) throws UnsupportedEncodingException {
		appendString0(string, false, encoding);
	}

	public void appendString(String string, int count) {
		try {
			appendString(string, count, Data.ENC_ASCII);
		} catch (UnsupportedEncodingException e) {
                        // this can't happen as we use ASCII encoding
                        // whatever is in the buffer it gets interpreted as ascii
		}
	}

	public void appendString(String string, int count, String encoding) throws UnsupportedEncodingException {
		String subStr = string.substring(0, count);
		appendString0(subStr, false, encoding);
	}

	private void appendString0(String string, boolean isCString, String encoding) throws UnsupportedEncodingException {
		if ((string != null) && (string.length() > 0)) {
			byte[] stringBuf = null;
			if (encoding != null) {
				stringBuf = string.getBytes(encoding);
			} else {
				stringBuf = string.getBytes();
			}
			if ((stringBuf != null) && (stringBuf.length > 0)) {
				appendBytes0(stringBuf, stringBuf.length);
			}
		}
		if (isCString) {
			appendBytes0(zero, 1); // always append terminating zero
		}
	}

	public void appendBuffer(ByteBuffer buf) {
		if (buf != null) {
			try {
				appendBytes(buf, buf.length());
			} catch (NotEnoughDataInByteBufferException e) {
				// can't happen as appendBytes only complains
				// when count>buf.length
			}
		}
	}

	public void appendBytes(ByteBuffer bytes, int count) throws NotEnoughDataInByteBufferException {
		if (count > 0) {
			if (bytes == null) {
				throw new NotEnoughDataInByteBufferException(0, count);
			}
			if (bytes.length() < count) {
				throw new NotEnoughDataInByteBufferException(bytes.length(), count);
			}
			appendBytes0(bytes.getBuffer(), count);
		}
	}

	public void appendBytes(byte[] bytes, int count) {
		if (bytes != null) {
			if (count > bytes.length) {
				count = bytes.length;
			}
			appendBytes0(bytes, count);
		}
	}

	public void appendBytes(byte[] bytes) {
		if (bytes != null) {
			appendBytes0(bytes, bytes.length);
		}
	}

	public byte removeByte() throws NotEnoughDataInByteBufferException {
		byte result = 0;
		byte[] resBuff = removeBytes(SZ_BYTE).getBuffer();
		result = resBuff[0];
		return result;
	}

	public short removeShort() throws NotEnoughDataInByteBufferException {
		short result = 0;
		byte[] resBuff = removeBytes(SZ_SHORT).getBuffer();
		result |= resBuff[0] & 0xff;
		result <<= 8;
		result |= resBuff[1] & 0xff;
		return result;
	}

	public int removeInt() throws NotEnoughDataInByteBufferException {
		int result = readInt();
		removeBytes0(SZ_INT);
		return result;
	}

	public int readInt() throws NotEnoughDataInByteBufferException {
		int result = 0;
		int len = length();
		if (len >= SZ_INT) {
			result |= buffer[0] & 0xff;
			result <<= 8;
			result |= buffer[1] & 0xff;
			result <<= 8;
			result |= buffer[2] & 0xff;
			result <<= 8;
			result |= buffer[3] & 0xff;
			return result;
		} else {
			throw new NotEnoughDataInByteBufferException(len, 4);
		}
	}

	public String removeCString() throws NotEnoughDataInByteBufferException, TerminatingZeroNotFoundException {
		int len = length();
		int zeroPos = 0;
		if (len == 0) {
			throw new NotEnoughDataInByteBufferException(0, 1);
		}
		while ((zeroPos < len) && (buffer[zeroPos] != 0)) {
			zeroPos++;
		}
		if (zeroPos < len) { // found terminating zero
			String result = null;
			if (zeroPos > 0) {
				try {
					result = new String(buffer, 0, zeroPos, Data.ENC_GSM7BIT);
				} catch (UnsupportedEncodingException e) {
					try {
						result = new String(buffer, 0, zeroPos, Data.ENC_ASCII);
					} catch (UnsupportedEncodingException e2) {
						// this can't happen as we use ASCII encoding
						// whatever is in the buffer it gets interpreted as ascii
					}
				}
			} else {
				result = new String("");
			}
			removeBytes0(zeroPos + 1);
			return result;
		} else {
			throw new TerminatingZeroNotFoundException();
		}
	}

	public String removeString(int size, String encoding)
		throws NotEnoughDataInByteBufferException, UnsupportedEncodingException {
		int len = length();
		if (len < size) {
			throw new NotEnoughDataInByteBufferException(len, size);
		}
		UnsupportedEncodingException encodingException = null;
		String result = null;
		if (len > 0) {
			try {
				if (encoding != null) {
					result = new String(buffer, 0, size, encoding);
				} else {
					result = new String(buffer, 0, size);
				}
			} catch (UnsupportedEncodingException e) {
				debug.write("Unsupported encoding exception " + e);
				event.write(e, null);
				encodingException = e;
			}
			removeBytes0(size);
		} else {
			result = new String("");
		}
		if (encodingException != null) {
			throw encodingException;
		}
		return result;
	}

	public ByteBuffer removeBuffer(int count) throws NotEnoughDataInByteBufferException {
		return removeBytes(count);
	}

	public ByteBuffer removeBytes(int count) throws NotEnoughDataInByteBufferException {
		ByteBuffer result = readBytes(count);
		removeBytes0(count);
		return result;
	}

	// just removes bytes from the buffer and doesnt return anything
	public void removeBytes0(int count) throws NotEnoughDataInByteBufferException {
		// paolo@bulksms.com: seeing as how these libs return 32-bit unsigned ints
		// (by using negative values), they should also cater for them in input.
		// However, it's unlikely to be happen often (mostly occurs with garbage
		// input), so I'm not fixing it, but just adding this quickly to stop
		//  ArrayIndexOutOfBoundsExceptions with large values:
		if (count < 0) count = Integer.MAX_VALUE;

		int len = length();
		int lefts = len - count;
		if (lefts > 0) {
			byte[] newBuf = new byte[lefts];
			System.arraycopy(buffer, count, newBuf, 0, lefts);
			setBuffer(newBuf);
		} else {
			setBuffer(null);
		}
	}

	public ByteBuffer readBytes(int count) throws NotEnoughDataInByteBufferException {
		int len = length();
		ByteBuffer result = null;
		if (count > 0) {
			if (len >= count) {
				byte[] resBuf = new byte[count];
				System.arraycopy(buffer, 0, resBuf, 0, count);
				result = new ByteBuffer(resBuf);
				return result;
			} else {
				throw new NotEnoughDataInByteBufferException(len, count);
			}
		} else {
			return result; // just null as wanted count = 0
		}
	}

	// everything must be checked before calling this method
	// and count > 0
	private void appendBytes0(byte[] bytes, int count) {
		int len = length();
		byte[] newBuf = new byte[len + count];
		if (len > 0) {
			System.arraycopy(buffer, 0, newBuf, 0, len);
		}
		System.arraycopy(bytes, 0, newBuf, len, count);
		setBuffer(newBuf);
	}

	public String getHexDump() {
		StringBuffer dump = new StringBuffer();
		try {
			int dataLen = length();
			byte[] buffer = getBuffer();
			for (int i=0; i<dataLen; i++) {
				dump.append(Character.forDigit((buffer[i] >> 4) & 0x0f, 16));
				dump.append(Character.forDigit(buffer[i] & 0x0f, 16));
			}
		} catch (Throwable t) {
			// catch everything as this is for debug
			dump = new StringBuffer("Throwable caught when dumping = ").append(t);
		}
		return dump.toString();
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.4  2004/01/07 21:51:52  sverkera
 * Fallback silently from Gsm Default to Ascii if it's not supported
 *
 * Revision 1.3  2004/01/07 21:37:08  sverkera
 * Removed a debug printout that just adds confusion
 *
 * Revision 1.2  2003/09/30 09:13:02  sverkera
 * Changed to use GSM 7Bit as default
 *
 * Revision 1.1  2003/07/23 00:28:39  sverkera
 * Imported
 *
 * 
 * Old changelog:
 * 13-07-01 ticp@logica.com fixed appendString(String,int) which was appending
 *                          appending one byte less than it had to; it was also
 *                          appending terminationg zero which it hadn't to :-)
 * 13-07-01 ticp@logica.com added getHexDump() function which returns the content
 *                          of the buffer in hex as String
 * 26-09-01 ticp@logica.com fixed getHexDump - hi byte masked with 0x0f to mask-out
 *                          the sign which got extended to the hi byte after shift
 *                          right
 * 27-09-01 ticp@logica.com added method for appending first count bytes
 *                          from byte[]
 * 15-11-01 ticp@logica.com added support for appending strings with explicitly
 *                          stated character encoding
 * 20-11-01 ticp@logica.com fixed bug in appendString0 where if param string
 *                          was null, NullPointerException was thrown
 */
