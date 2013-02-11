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
public class DeliverSMResp extends Response {
	private String messageId = Data.DFLT_MSGID;

	public DeliverSMResp() {
		super(Data.DELIVER_SM_RESP);
	}

	public void setBody(ByteBuffer buffer)
		throws NotEnoughDataInByteBufferException, TerminatingZeroNotFoundException, WrongLengthOfStringException {
		setMessageId(buffer.removeCString());
	}

	public ByteBuffer getBody() {
		ByteBuffer buffer = new ByteBuffer();
		buffer.appendCString(messageId);
		return buffer;
	}

	public void setMessageId(String value) throws WrongLengthOfStringException {
		checkString(value, Data.SM_MSGID_LEN);
		messageId = value;
	}

	public String getMessageId() {
		return messageId;
	}

	public String debugString() {
		String dbgs = "(deliver_resp: ";
		dbgs += super.debugString();
		dbgs += getMessageId();
		dbgs += " ";
		dbgs += debugStringOptional();
		dbgs += ") ";
		return dbgs;
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
