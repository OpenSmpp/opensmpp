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
public class QuerySMResp extends Response {
	private String messageId = Data.DFLT_MSGID;
	private String finalDate = Data.DFLT_DATE; //1 or 17
	private byte messageState = Data.DFLT_MSG_STATE;
	private byte errorCode = Data.DFLT_ERR;

	public QuerySMResp() {
		super(Data.QUERY_SM_RESP);
	}

	public void setBody(ByteBuffer buffer)
		throws NotEnoughDataInByteBufferException, TerminatingZeroNotFoundException, PDUException {
		setMessageId(buffer.removeCString());
		setFinalDate(buffer.removeCString());
		setMessageState(buffer.removeByte());
		setErrorCode(buffer.removeByte());
	}

	public ByteBuffer getBody() {
		ByteBuffer buffer = new ByteBuffer();
		buffer.appendCString(getMessageId());
		buffer.appendCString(getFinalDate());
		buffer.appendByte(getMessageState());
		buffer.appendByte(getErrorCode());
		return buffer;
	}

	public void setMessageId(String value) throws WrongLengthOfStringException {
		checkString(value, Data.SM_MSGID_LEN);
		messageId = value;
	}

	public void setFinalDate(String value) throws WrongDateFormatException {
		checkDate(value);
		finalDate = value;
	}

	public void setMessageState(byte value) {
		messageState = value;
	}
	public void setErrorCode(byte value) {
		errorCode = value;
	}

	public String getMessageId() {
		return messageId;
	}
	public String getFinalDate() {
		return finalDate;
	}
	public byte getMessageState() {
		return messageState;
	}
	public byte getErrorCode() {
		return errorCode;
	}

	public String debugString() {
		String dbgs = "(query_resp: ";
		dbgs += super.debugString();
		dbgs += getMessageId();
		dbgs += " ";
		dbgs += getFinalDate();
		dbgs += " ";
		dbgs += getMessageState();
		dbgs += " ";
		dbgs += getErrorCode();
		dbgs += " ";
		dbgs += debugStringOptional();
		dbgs += ") ";
		return dbgs;
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
