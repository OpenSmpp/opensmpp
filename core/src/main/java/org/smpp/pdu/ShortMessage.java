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
package org.smpp.pdu;

import java.io.UnsupportedEncodingException;

import org.smpp.Data;
import org.smpp.util.ByteBuffer;
import org.smpp.util.NotEnoughDataInByteBufferException;
import org.smpp.util.TerminatingZeroNotFoundException;

/**
 * Provides encapsulation of message data with optional message encoding.
 * Can contain an ordinary data message or a message containing data encoded
 * in one of the Java supported encodings, including multibyte.
 * On Java encodings see <a href="http://java.sun.com/j2se/1.3/docs/guide/intl/encoding.doc.html">Supported encodings</a>
 * 
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.4 $
 */
public class ShortMessage extends ByteData {
	/**
	 * Minimal size of the message in bytes. For multibyte encoded messages
	 * it means size after converting to sequence of octets.
	 */
	int minLength = 0;

	/**
	 * Max size of the message in octets. For multibyte encoded messages
	 * it means size after converting to sequence of octets.
	 */
	int maxLength = 0;

	/**
	 * The actual message encoded with the provided encoding.
	 * @see #encoding
	 */
	String message = null;

	/**
	 * The encoding of the message
	 */
	String encoding = null;

	/**
	 * The length of the message data.
	 */
	int length = 0;

	/**
	 * The message data after conversion to the sequence of octets,
	 * i.e. the octets.
	 */
	byte[] messageData = null;

	/**
	 * Construct the short message with max data length -- the max count
	 * of octets carried by the massege. It's not count of chars when interpreted
	 * with certain encoding.
	 * @param maxLength the max length of the message
	 */
	public ShortMessage(int maxLength) {
		this.maxLength = maxLength;
	}

	/**
	 * Construct the short message with mina nd max data length --
	 * the min and max count of octets carried by the massege.
	 * It's not count of chars when interpreted with certain encoding.
	 * @param minLength the min length of the message
	 * @param maxLength the max length of the message
	 */
	public ShortMessage(int minLength, int maxLength) {
		this.minLength = minLength;
		this.maxLength = maxLength;
	}

	/**
	 * Reads data from the buffer and stores them into <code>messageData</code>.
	 * The data can be later fetched using one of the <code>getMessage</code>
	 * methods.
	 * @param buffer the buffer containing the message data; must contain exactly
	 *               the data of the message (not zero terminated nor length tagged)
	 * @see #getMessage()
	 * @see #getMessage(String)
	 */
	public void setData(ByteBuffer buffer)
		throws PDUException, NotEnoughDataInByteBufferException, TerminatingZeroNotFoundException {
		byte[] messageData = null;
		int length = 0;
		if (buffer != null) {
			messageData = buffer.getBuffer();
			length = messageData == null ? 0 : messageData.length;
			checkString(minLength, length, maxLength);
		}
		this.message = null;
		this.messageData = messageData;
		this.length = length;
	}

	/**
	 * Returns the sequence of octets generated from the message according the encoding
	 * provided.
	 * @return the bytes generated from the message
	 */
	public ByteBuffer getData() {
		ByteBuffer buffer = null;
		buffer = new ByteBuffer(messageData);
		return buffer;
	}

	/**
	 * Sets the message a new value. Default encoding <code>Data.ENC_GSM7BIT</code>
	 * is used.
	 * @param message the message
	 * @exception WrongLengthOfStringException thrown when the message
	 *            too short or long
	 */
	public void setMessage(String message) throws WrongLengthOfStringException {
		try {
			setMessage(message, Data.ENC_GSM7BIT);
		} catch (UnsupportedEncodingException e) {
			try {
				setMessage(message, Data.ENC_ASCII);
			} catch (UnsupportedEncodingException uee) {
				// ascii always supported
			}
		}
	}

	/**
	 * Sets the message to a value with given encoding.
	 * @param message the message
	 * @param encoding the encoding of the message provided
	 * @exception WrongLengthOfStringException thrown when the message
	 *            too short or long
	 * @exception UnsupportedEncodingException if the required encoding is not
	 *            available for the Java Runtime system
	 */
	public void setMessage(String message, String encoding)
		throws WrongLengthOfStringException, UnsupportedEncodingException {
		checkString(message, minLength, maxLength, encoding);
		if (message != null) {
			try {
				messageData = message.getBytes(encoding);
			} catch (UnsupportedEncodingException e) {
				debug.write("encoding " + encoding + " not supported. Exception " + e);
				event.write(e, "encoding " + encoding + " not supported");
				throw e; // re-throw
			}
			this.message = message;
			this.length = messageData.length;
			this.encoding = encoding;
		} else {
			this.message = null;
			this.messageData = null;
			this.encoding = encoding;
			this.length = 0;
		}
	}

	/**
	 * Sets the encoding of the messasge.
	 * Handy for message read from <code>ByteBuffer</code> to set the encoding ad hoc.
	 * @param encoding the message encoding
	 * @exception UnsupportedEncodingException if the required encoding is not
	 *            available for the Java Runtime system
	 */
	public void setEncoding(String encoding) throws UnsupportedEncodingException {
		message = new String(messageData, encoding);
		this.encoding = encoding;
	}

	/**
	 * Returns the message. If the message was read from <code>ByteBuffer</code>
	 * and no explicit encoding is set, the <code>Data.ENC_GSM7BIT</code> encoding
	 * is used. Otherwise the encoding set is used.
	 */
	public String getMessage() {
		String useEncoding = encoding != null ? encoding : Data.ENC_GSM7BIT;
		String theMessage = null;
		try {
			theMessage = getMessage(useEncoding);
		} catch (UnsupportedEncodingException e) {
			// fall back to ascii
			try {
				theMessage = getMessage(Data.ENC_ASCII);
			} catch (UnsupportedEncodingException uee) {
				// ascii is always supported
			}
		}
		return theMessage;
	}

	/**
	 * Returns the message applying the provided encoding to convert
	 * the sequence of octets.
	 * @param encoding the required encoding of the resulting (String) message
	 * @exception UnsupportedEncodingException if the required encoding is not
	 *            available for the Java Runtime system
	 */
	public String getMessage(String encoding) throws UnsupportedEncodingException {
		String message = null;
		if (messageData != null) {
			if ((encoding != null) && (this.encoding != null) && (encoding.equals(this.encoding))) {
				// if the required encoding is the same as current encoding
				// or if the encoding haven't been set yet
				if (this.message == null) {
					this.message = new String(messageData, encoding);
				}
				message = this.message;
			} else {
				if (encoding != null) {
					message = new String(messageData, encoding);
				} else {
					message = new String(messageData);
				}
			}
		}
		return message;
	}

	/** Returns the length of the message in octets. */
	public int getLength() {
		return messageData.length;
	}

	/** Returns the encoding of the message. */
	public String getEncoding() {
		return encoding;
	}

	/** Returns if the encoding provided is supported by the Java Runtime system. */
	public static boolean encodingSupported(String encoding) {
		boolean supported = true;
		try {
			"SMPP".getBytes(encoding);
		} catch (UnsupportedEncodingException e) {
			supported = false;
		}
		return supported;
	}

	public String debugString() {
		String dbgs = "(sm: ";
		if (encoding != null) {
			dbgs += "enc: ";
			dbgs += encoding;
			dbgs += " ";
		}
		dbgs += "msg: ";
		if(encoding != null) {
			try {
				dbgs += getMessage(encoding);
			} catch(UnsupportedEncodingException e) {
				dbgs += getMessage();
			}
		} else {
			dbgs += getMessage();
		}
		dbgs += ") ";
		return dbgs;
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.3  2003/09/30 10:24:45  sverkera
 * Corrected typo as described in bug id 792803
 *
 * Revision 1.2  2003/09/30 09:05:22  sverkera
 * Use GSM 7Bit encoding as default but fall back to Ascii if there is any problem
 *
 * Revision 1.1  2003/07/23 00:28:39  sverkera
 * Imported
 *
 */
