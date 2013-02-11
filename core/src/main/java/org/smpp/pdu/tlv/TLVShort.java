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

import org.smpp.util.ByteBuffer;
import org.smpp.util.NotEnoughDataInByteBufferException;
import org.smpp.pdu.ValueNotSetException;

/**
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.1 $
 */
public class TLVShort extends TLV {
	private short value = 0;

	public TLVShort() {
		super(2, 2);
	}

	public TLVShort(short p_tag) {
		super(p_tag, 2, 2);
	}

	public TLVShort(short p_tag, short p_value) {
		super(p_tag, 2, 2);
		value = p_value;
		markValueSet();
	}

	protected void setValueData(ByteBuffer buffer) throws TLVException {
		checkLength(buffer);
		try {
			value = buffer.removeShort();
		} catch (NotEnoughDataInByteBufferException e) {
			// can't happen as the size is already checked by checkLength()
		}
		markValueSet();
	}

	protected ByteBuffer getValueData() throws ValueNotSetException {
		ByteBuffer valueBuf = new ByteBuffer();
		valueBuf.appendShort(getValue());
		return valueBuf;
	}

	public void setValue(short p_value) {
		value = p_value;
		markValueSet();
	}

	public short getValue() throws ValueNotSetException {
		if (hasValue()) {
			return value;
		} else {
			throw new ValueNotSetException();
		}
	}

	public String debugString() {
		String dbgs = "(short: ";
		dbgs += super.debugString();
		dbgs += value;
		dbgs += ") ";
		return dbgs;
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
