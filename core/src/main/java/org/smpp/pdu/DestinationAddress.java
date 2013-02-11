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

import org.smpp.Data;
import org.smpp.util.*;

/**
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.1 $
 */
public class DestinationAddress extends ByteData {
	private byte destFlag = Data.DFLT_DEST_FLAG;
	private ByteData theAddress = null; // either Address or DistributionList

	public DestinationAddress() {
	}

	public DestinationAddress(Address address) {
		setAddress(address);
	}

	// it's more likely that this will be used with device address
	// than with distribution list
	public DestinationAddress(String address) throws WrongLengthOfStringException {
		setAddress(new Address(address));
	}

	public DestinationAddress(byte ton, byte npi, String address) throws WrongLengthOfStringException {
		setAddress(new Address(ton, npi, address));
	}

	public DestinationAddress(DistributionList dl) {
		setDistributionList(dl);
	}

	public void setData(ByteBuffer buffer)
		throws
			NotEnoughDataInByteBufferException,
			TerminatingZeroNotFoundException,
			WrongLengthOfStringException,
			WrongDestFlagException {
		destFlag = buffer.removeByte();
		switch (destFlag) {
			case Data.SM_DEST_SME_ADDRESS :
				{
					Address address = new Address();
					address.setData(buffer);
					setAddress(address);
					break;
				}
			case Data.SM_DEST_DL_NAME :
				{
					DistributionList dl = new DistributionList();
					dl.setData(buffer);
					setDistributionList(dl);
					break;
				}
			default :
				{
					throw new WrongDestFlagException("" + destFlag);
				}
		}
	}

	public ByteBuffer getData() throws ValueNotSetException {
		if (hasValue()) {
			ByteBuffer buffer = new ByteBuffer();
			buffer.appendByte(getDestFlag());
			if (isAddress()) {
				Address address = getAddress();
				buffer.appendBuffer(address.getData());
			} else if (isDistributionList()) {
				DistributionList dl = getDistributionList();
				buffer.appendBuffer(dl.getData());
			}
			return buffer;
		} else {
			throw new ValueNotSetException();
		}
	}

	public void setAddress(Address address) {
		destFlag = Data.SM_DEST_SME_ADDRESS;
		theAddress = address;
	}

	public void setDistributionList(DistributionList dl) {
		destFlag = Data.SM_DEST_DL_NAME;
		theAddress = dl;
	}

	public byte getDestFlag() {
		return destFlag;
	}
	public ByteData getTheAddress() {
		return theAddress;
	}
	public Address getAddress() {
		if (isAddress()) {
			return (Address) theAddress;
		} else {
			return null;
		}
	}

	public DistributionList getDistributionList() {
		if (isDistributionList()) {
			return (DistributionList) theAddress;
		} else {
			return null;
		}
	}

	public boolean hasValue() {
		return destFlag != Data.DFLT_DEST_FLAG;
	}
	public boolean isAddress() {
		return destFlag == Data.SM_DEST_SME_ADDRESS;
	}
	public boolean isDistributionList() {
		return destFlag == Data.SM_DEST_DL_NAME;
	}

	public String debugString() {
		String dbgs = "(destaddr: ";
		dbgs += super.debugString();
		dbgs += Integer.toString(getDestFlag());
		dbgs += " ";
		dbgs += getTheAddress().debugString();
		dbgs += ") ";
		return dbgs;
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
