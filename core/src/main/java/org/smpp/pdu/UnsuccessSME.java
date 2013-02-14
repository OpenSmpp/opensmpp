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
public class UnsuccessSME extends Address {
	public int errorStatusCode = Data.ESME_ROK;

	public UnsuccessSME() {
	}

	public UnsuccessSME(String address, int err) throws WrongLengthOfStringException {
		super(address);
		setErrorStatusCode(err);
	}

	public UnsuccessSME(byte ton, byte npi, String address, int err) throws WrongLengthOfStringException {
		super(ton, npi, address);
		setErrorStatusCode(err);
	}

	public void setData(ByteBuffer buffer)
		throws NotEnoughDataInByteBufferException, TerminatingZeroNotFoundException, WrongLengthOfStringException {
		super.setData(buffer);
		setErrorStatusCode(buffer.removeInt());
	}

	public ByteBuffer getData() {
		ByteBuffer buffer = super.getData();
		buffer.appendInt(getErrorStatusCode());
		return buffer;
	}

	public void setErrorStatusCode(int sc) {
		errorStatusCode = sc;
	}
	public int getErrorStatusCode() {
		return errorStatusCode;
	}

	public String debugString() {
		String dbgs = "(unsucsme: ";
		dbgs += super.debugString();
		dbgs += Integer.toString(getErrorStatusCode());
		dbgs += ") ";
		return dbgs;
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
