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

import org.smpp.pdu.ByteData;
import org.smpp.pdu.ValueNotSetException;
import org.smpp.pdu.tlv.WrongLengthException;
import org.smpp.util.ByteBuffer;
import org.smpp.util.NotEnoughDataInByteBufferException;

/**
 * Base class for classes implementing representations of optional parameters
 * of various types. Acronym TLV means Tag-Length-Value. Derived from ByteData
 * this class "knows" how to parse from ByteBuffer and how to create a
 * ByteBuffer from it's data. Descanedants of the class (concrete types)
 * should rewrite <code>setValueData</code> and <code>getValueData</code>
 * methods for setting and getting of the particular data type. The
 * tag and the length is set and get by the <code>TLV</code> class.
 * The class also provides additiona methods for checking various
 * type validity.
 *
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.1 $
 * @see TLVByte
 * @see TLVEmpty
 * @see TLVInt
 * @see TLVOctets
 * @see TLVShort
 * @see TLVString
 */
public abstract class TLV extends ByteData {
	/** The tag of the instance of TLV as defined in SMPP v3.4 */
	private short tag = 0;

	/**
	 * If the value was set to the TLV; if yes, then the optional param
	 * was present in the PDU (for received PDUs) or it was set the value
	 * (for PDUs which will be transmitted)
	 */
	private boolean valueIsSet = false; // must be set by setValueData()

	/**
	 * For checking the min/max length limits that the particular limit
	 * <i>shouldn't</i> be checked.
	 */
	private static int DONT_CHECK_LIMIT = -1;

	/**
	 * The minimal length of the data. If no min length is required,
	 * then set to <code>DONT_CHECK_LIMIT</code>.
	 */
	private int minLength = DONT_CHECK_LIMIT;

	/**
	 * The maximal length of the data. If no max length is required,
	 * then set to <code>DONT_CHECK_LIMIT</code>.
	 */
	private int maxLength = DONT_CHECK_LIMIT;

	/** Everything is default. Not particularly good idea. */
	public TLV() {
		super();
	}

	/** Sets only the tag of the TLV. */
	public TLV(short tag) {
		super();
		this.tag = tag;
	}

	/** Sets minimal an maximal length; no tag set. */
	public TLV(int min, int max) {
		super();
		minLength = min;
		maxLength = max;
	}

	/** Sets all the necessary params of the TLV. */
	public TLV(short tag, int min, int max) {
		super();
		this.tag = tag;
		minLength = min;
		maxLength = max;
	}

	/**
	 * Sets the data of the value carried by the TLV.
	 * Derived classes must override this method to provide their
	 * data related parsing of the value.
	 * @exception TLVException if the buffer contains somehow invalid data
	 */
	protected abstract void setValueData(ByteBuffer buffer) throws TLVException;

	/**
	 * Returns the data of the value carried by the TLV as binary data.
	 * Derived classes must override this method to provide their
	 * data related creation of the buffer based on the value.
	 * @exception ValueNotSetException if the TLV wasn't set any data
	 *            then there is nothing to be returned (the optional parameter
	 *            isn't transmitted then.)
	 */
	protected abstract ByteBuffer getValueData() throws ValueNotSetException;

	/** Sets the tag of this TLV. */
	public void setTag(short tag) {
		this.tag = tag;
	}

	/** Returns the tag of this TLV. */
	public short getTag() {
		return tag;
	}

	/**
	 * Returns the length of the actual binary data representing the
	 * value carried by this TLV.
	 * @exception ValueNotSetException as this method uses
	 *            <code>getValueData</code> for calculation of the length
	 *            this exception can be thrown
	 * @see #getValueData()
	 */
	public int getLength() throws ValueNotSetException {
		if (hasValue()) {
			ByteBuffer valueBuf = getValueData();
			if (valueBuf != null) {
				return valueBuf.length();
			} else {
				return 0;
			}
		} else {
			return 0;
		}
	}

	/**
	 * Overwrites <code>ByteData</code>'s <code>setData</code> and parses
	 * tag, length and the binary data value from the buffer. For parsing the 
	 * data value calls abstract <code>setValueData</code>.
	 */
	public void setData(ByteBuffer buffer) throws NotEnoughDataInByteBufferException, TLVException {
		short newTag = buffer.removeShort();
		int length = buffer.removeShort();
		ByteBuffer valueBuf = buffer.removeBuffer(length);
		setValueData(valueBuf);
		setTag(newTag);
	}

	/**
	 * Returns the binary TLV created from tag, length and binary data value
	 * carried by this TLV.
	 */
	public ByteBuffer getData() throws ValueNotSetException {
		if (hasValue()) {
			ByteBuffer tlvBuf = new ByteBuffer();
			tlvBuf.appendShort(getTag());
			tlvBuf.appendShort(encodeUnsigned(getLength()));
			tlvBuf.appendBuffer(getValueData());
			return tlvBuf;
		} else {
			return null;
		}
	}

	/** 'Remembers' that the value was (somehow) set. */
	protected void markValueSet() {
		valueIsSet = true;
	}

	/** Returns if the value has been set. */
	public boolean hasValue() {
		return valueIsSet;
	}

	/**
	 * Compares this TLV to another TLV. TLV are equal if their tags
	 * are equal.
	 */
	public boolean equals(Object obj) {
		if ((obj != null) && (obj instanceof TLV)) {
			return getTag() == ((TLV) obj).getTag();
		}
		return false;
	}

	/**
	 * Throws exception if the <code>length</code> provided isn't between
	 * provided <code>min</code> and <code>max</code> inclusive.
	 */
	protected static void checkLength(int min, int max, int length) throws WrongLengthException {
		if ((length < min) || (length > max)) {
			throw new WrongLengthException(min, max, length);
		}
	}

	/**
	 * Throws exception if the length provided isn't between min and max
	 * lengths of this TLV.
	 */
	protected void checkLength(int length) throws WrongLengthException {
		int min = 0;
		int max = 0;
		if (minLength != DONT_CHECK_LIMIT) {
			min = minLength;
		} else {
			min = 0;
		}
		if (maxLength != DONT_CHECK_LIMIT) {
			max = maxLength;
		} else {
			max = Integer.MAX_VALUE;
		}
		checkLength(min, max, length);
	}

	/**
	 * Throws exception if the length of the buffer provided isn't between
	 * min and max lengths provided.
	 */
	protected static void checkLength(int min, int max, ByteBuffer buffer) throws WrongLengthException {
		int length;
		if (buffer != null) {
			length = buffer.length();
		} else {
			length = 0;
		}
		checkLength(min, max, length);
	}

	/**
	 * Throws exception if the length of the buffer provided isn't between
	 * min and max lengths of this TLV.
	 */
	protected void checkLength(ByteBuffer buffer) throws WrongLengthException {
		int length;
		if (buffer != null) {
			length = buffer.length();
		} else {
			length = 0;
		}
		checkLength(length);
	}

	/**
	 * Returns more specific debug info about this TLV.
	 * @see ByteData#debugString()
	 */
	public String debugString() {
		String dbgs = "(tlv: ";
		dbgs += tag;
		dbgs += ") ";
		return dbgs;
	}

}
/*
 * $Log: not supported by cvs2svn $
 * 
 * Old changelog:
 * 02-10-01 ticp@logica.com comments added, indentation changed -> spaces
 * 02-10-01 ticp@logica.com removed some historical unused code
 * 01-11-01 ticp@logica.com TLV length now returned as int to
 *						    allow express the lengths > 32767 (they're stored as
 *						    negative values in vars of type short)
 */
