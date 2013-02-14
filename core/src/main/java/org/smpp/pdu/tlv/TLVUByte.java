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
package org.smpp.pdu.tlv;

import org.smpp.pdu.IntegerOutOfRangeException;
import org.smpp.pdu.ValueNotSetException;
import org.smpp.util.ByteBuffer;
import org.smpp.util.NotEnoughDataInByteBufferException;

/**
 * TLV carrying unsigned byte (octet).
 *
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.1 $
 */
public class TLVUByte extends TLV {
	/** The value of the TLV. Stored as short (two byte) as byte
	 * can't carry full unsigned octet.
	 */
	private short value = 0;

	/**
	 * Initialises the TLV with default values.
	 */
	public TLVUByte() {
		super(1, 1);
	}

	/**
	 * Initialises the TLV with provided tag.
	 * @param p_tag the tag of this TLV
	 */
	public TLVUByte(short p_tag) {
		super(p_tag, 1, 1);
	}

	/**
	 * Reads 1 octet from buffer and interprets it as unsigned byte.
	 * @param buffer the buffer to read the unsigned byte from
	 */
	protected void setValueData(ByteBuffer buffer) throws TLVException {
		checkLength(buffer);
		try {
			value = decodeUnsigned(buffer.removeByte());
		} catch (NotEnoughDataInByteBufferException e) {
			// can't happen as the size is already checked by checkLength()
		}
		markValueSet();
	}

	/**
	 * Creates byt buffer containing one unsigned byte.
	 * @return the byte buffer with one unsingned byte
	 */
	protected ByteBuffer getValueData() throws ValueNotSetException {
		ByteBuffer valueBuf = new ByteBuffer();
		valueBuf.appendByte(encodeUnsigned(getValue()));
		return valueBuf;
	}

	/**
	 * Sets the value of the TLV to the new value.
	 * @param value the new value of the TLV
	 */
	public void setValue(short value) throws IntegerOutOfRangeException {
		checkRange(0, value, 255);
		this.value = value;
		markValueSet();
	}

	/**
	 * Returns the current value of the TLV
	 * @return the curent value of the TLV
	 * @throws ValueNotSetException if value hasn't been set
	 */
	public short getValue() throws ValueNotSetException {
		if (hasValue()) {
			return value;
		} else {
			throw new ValueNotSetException();
		}
	}

	public String debugString() {
		String dbgs = "(byte: ";
		dbgs += super.debugString();
		dbgs += value;
		dbgs += ") ";
		return dbgs;
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
