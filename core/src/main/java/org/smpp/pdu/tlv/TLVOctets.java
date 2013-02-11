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

import java.lang.Error;

import org.smpp.util.ByteBuffer;
import org.smpp.util.NotEnoughDataInByteBufferException;
import org.smpp.pdu.ValueNotSetException;

/**
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.1 $
 */
public class TLVOctets extends TLV {
	private ByteBuffer value = null;

	public TLVOctets() {
		super();
	}

	public TLVOctets(short p_tag) {
		super(p_tag);
	}

	public TLVOctets(short p_tag, int min, int max) {
		super(p_tag, min, max);
	}

	public TLVOctets(short p_tag, ByteBuffer p_value) throws TLVException {
		super(p_tag);
		setValueData(p_value);
	}

	public TLVOctets(short p_tag, int min, int max, ByteBuffer p_value) throws TLVException {
		super(p_tag, min, max);
		setValueData(p_value);
	}

	protected void setValueData(ByteBuffer buffer) throws TLVException {
		checkLength(buffer);
		if (buffer != null) {
			try {
				value = buffer.removeBuffer(buffer.length());
			} catch (NotEnoughDataInByteBufferException e) {
				throw new Error(
					"Removing buf.length() data from ByteBuffer buf "
						+ "reported too little data in buf, which shouldn't happen.");
			}
		} else {
			value = null;
		}
		markValueSet();
	}

	protected ByteBuffer getValueData() throws ValueNotSetException {
		ByteBuffer valueBuf = new ByteBuffer();
		valueBuf.appendBuffer(getValue());
		return valueBuf;
	}

	public void setValue(ByteBuffer p_value) {
		if (p_value != null) {
			try {
				value = p_value.removeBuffer(p_value.length());
			} catch (NotEnoughDataInByteBufferException e) {
				throw new Error(
					"Removing buf.length() data from ByteBuffer buf "
						+ "reported too little data in buf, which shouldn't happen.");
			}
		} else {
			value = null;
		}
		markValueSet();
	}

	public ByteBuffer getValue() throws ValueNotSetException {
		if (hasValue()) {
			return value;
		} else {
			throw new ValueNotSetException();
		}
	}

	public String debugString() {
		String dbgs = "(oct: ";
		dbgs += super.debugString();
		dbgs += value == null ? "" : value.getHexDump();
		dbgs += ") ";
		return dbgs;
	}

}
/*
 * $Log: not supported by cvs2svn $
 * 
 * Old changelog:
 * 26-09-01 ticp@logica.com debugString() now prints hex dump of the buffer data
 *                          instead of the object reference of it
 */
