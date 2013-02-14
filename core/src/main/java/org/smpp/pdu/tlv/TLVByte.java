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

import org.smpp.pdu.ValueNotSetException;
import org.smpp.util.ByteBuffer;
import org.smpp.util.NotEnoughDataInByteBufferException;

/**
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.1 $
 */
public class TLVByte extends TLV {
	private byte value = 0;

	public TLVByte() {
		super(1, 1);
	}

	public TLVByte(short p_tag) {
		super(p_tag, 1, 1);
	}

	public TLVByte(short p_tag, byte p_value) {
		super(p_tag, 1, 1);
		value = p_value;
		markValueSet();
	}

	protected void setValueData(ByteBuffer buffer) throws TLVException {
		checkLength(buffer);
		try {
			value = buffer.removeByte();
		} catch (NotEnoughDataInByteBufferException e) {
			// can't happen as the size is already checked by checkLength()
		}
		markValueSet();
	}

	protected ByteBuffer getValueData() throws ValueNotSetException {
		ByteBuffer valueBuf = new ByteBuffer();
		valueBuf.appendByte(getValue());
		return valueBuf;
	}

	public void setValue(byte p_value) {
		value = p_value;
		markValueSet();
	}

	public byte getValue() throws ValueNotSetException {
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
