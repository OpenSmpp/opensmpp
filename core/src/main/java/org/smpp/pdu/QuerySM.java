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
public class QuerySM extends Request {
	private String messageId = Data.DFLT_MSGID;
	private Address sourceAddr = new Address();

	public QuerySM() {
		super(Data.QUERY_SM);
	}

	protected Response createResponse() {
		return new QuerySMResp();
	}

	public void setBody(ByteBuffer buffer)
		throws NotEnoughDataInByteBufferException, TerminatingZeroNotFoundException, PDUException {
		setMessageId(buffer.removeCString());
		sourceAddr.setData(buffer); // ?
	}

	public ByteBuffer getBody() {
		ByteBuffer buffer = new ByteBuffer();
		buffer.appendCString(messageId);
		buffer.appendBuffer(getSourceAddr().getData());
		return buffer;
	}
	public void setMessageId(String value) throws WrongLengthOfStringException {
		checkString(value, Data.SM_MSGID_LEN);
		messageId = value;
	}

	public void setSourceAddr(Address value) {
		sourceAddr = value;
	}
	public void setSourceAddr(String address) throws WrongLengthOfStringException {
		setSourceAddr(new Address(address));
	}

	public void setSourceAddr(byte ton, byte npi, String address) throws WrongLengthOfStringException {
		setSourceAddr(new Address(ton, npi, address));
	}

	public String getMessageId() {
		return messageId;
	}
	public Address getSourceAddr() {
		return sourceAddr;
	}

	public String debugString() {
		String dbgs = "(query: ";
		dbgs += super.debugString();
		dbgs += getMessageId();
		dbgs += " ";
		dbgs += getSourceAddr().debugString();
		dbgs += " ";
		dbgs += debugStringOptional();
		dbgs += ") ";
		return dbgs;
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
