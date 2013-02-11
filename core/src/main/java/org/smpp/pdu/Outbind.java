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
public class Outbind extends Request {
	private String systemId = Data.DFLT_SYSID;
	private String password = Data.DFLT_PASS;

	public Outbind() {
		super(Data.OUTBIND);
	}

	protected Response createResponse() {
		return null;
	}

	public boolean canResponse() {
		return false;
	}

	public void setBody(ByteBuffer buffer)
		throws NotEnoughDataInByteBufferException, TerminatingZeroNotFoundException, PDUException {
		setSystemId(buffer.removeCString());
		setPassword(buffer.removeCString());
	}

	public ByteBuffer getBody() {
		ByteBuffer buffer = new ByteBuffer();
		buffer.appendCString(getSystemId());
		buffer.appendCString(getPassword());
		return buffer;
	}

	public void setSystemId(String sysId) throws WrongLengthOfStringException {
		checkString(sysId, Data.SM_SYSID_LEN);
		systemId = sysId;
	}

	public void setPassword(String pwd) throws WrongLengthOfStringException {
		checkString(pwd, Data.SM_PASS_LEN);
		password = pwd;
	}

	public String getSystemId() {
		return systemId;
	}
	public String getPassword() {
		return password;
	}

	public String debugString() {
		String dbgs = "(outbind: ";
		dbgs += super.debugString();
		dbgs += getSystemId();
		dbgs += " ";
		dbgs += getPassword();
		dbgs += ")";
		return dbgs;
	}

	// special equals() for outbind: as we don't care
	// about outbind's sequence number, any outbind is equal to
	// any other outbind, sort of :-)
	public boolean equals(Object object) {
		if (object == null) {
			return false;
		} else {
			PDU pdu = (PDU) object;
			return pdu.getCommandId() == getCommandId();
		}
	}

}
/*
 * $Log: not supported by cvs2svn $
 */
