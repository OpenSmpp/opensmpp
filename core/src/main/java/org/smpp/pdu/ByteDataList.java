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

import java.util.Vector;

import org.smpp.util.*;

/**
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.1 $
 */
public abstract class ByteDataList extends ByteData {

	public final static byte BYTE_SIZE = 1;
	public final static byte SHORT_SIZE = 2;
	public final static byte INT_SIZE = 4;

	public abstract ByteData createValue();

	private Vector<ByteData> values = new Vector<ByteData>();
	private int maxSize = 0;

	/**
	 * Length of the size field when reading from/storing to ByteBuffer.
	 * Can be only 1, 2 or 4.
	 */
	private int lengthOfSize = INT_SIZE;

	public ByteDataList() {
	}

	public ByteDataList(int max, int lengthOfSize) {
		maxSize = max;
		if ((lengthOfSize != 1) && (lengthOfSize != 2) && (lengthOfSize != 4)) {
			throw new Error("Length of the size field is invalid. " + "Expected 1, 2 or 4 and got " + lengthOfSize);
		}
		this.lengthOfSize = lengthOfSize;
	}

	private void resetValues() {
		values.removeAllElements();
	}

	public void setData(ByteBuffer buffer)
		throws PDUException, NotEnoughDataInByteBufferException, TerminatingZeroNotFoundException, TooManyValuesException {
		resetValues();
		int nrValues = 0;
		switch (lengthOfSize) {
			case BYTE_SIZE :
				nrValues = decodeUnsigned(buffer.removeByte());
				break;
			case SHORT_SIZE :
				nrValues = decodeUnsigned(buffer.removeShort());
				break;
			case INT_SIZE :
				// won't convert, maybe some other day (does anyone expect
				// correct values > 2147483648?)
				nrValues = buffer.removeInt();
				break;
		}
		ByteData value;
		for (int i = 0; i < nrValues; i++) {
			value = createValue();
			value.setData(buffer);
			addValue(value);
		}

	}

	public ByteBuffer getData() throws ValueNotSetException // PDUException
	{
		ByteBuffer buffer = new ByteBuffer();
		int nrValues = getCount();
		switch (lengthOfSize) {
			case BYTE_SIZE :
				buffer.appendByte(encodeUnsigned((short) nrValues));
				break;
			case SHORT_SIZE :
				buffer.appendShort(encodeUnsigned((int) nrValues));
				break;
			case INT_SIZE :
				buffer.appendInt(nrValues);
				break;
		}
		ByteData value;
		for (int i = 0; i < nrValues; i++) {
			value = getValue(i);
			buffer.appendBuffer(value.getData());
		}
		return buffer;
	}

	public int getCount() {
		return values.size();
	}

	public void addValue(ByteData value) throws TooManyValuesException {
		if (getCount() >= maxSize) {
			throw new TooManyValuesException();
		}
		values.add(value);
	}

	public ByteData getValue(int i) {
		if (i < getCount()) {
			return (ByteData) values.get(i);
		} else {
			return null;
		}
	}

	public String debugString() {
		String dbgs = "(bdlist: ";
		dbgs += super.debugString();
		int count = getCount();
		dbgs += "(count: " + count + ") ";
		for (int i = 0; i < count; i++) {
			ByteData value = getValue(i);
			dbgs += (i + 1) + ": " + value.debugString();
			dbgs += " ";
		}
		dbgs += ") ";
		return dbgs;
	}
}
/*
 * $Log: not supported by cvs2svn $
 *
 * Old changelog:
 * 01-11-01 ticp@logica.com better handling of lengths, now can specify size of
 *						    length field, fixed bug related to the unsigned value
 *						    stoed in signed Java integral variable
 */
