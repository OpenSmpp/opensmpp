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

public class CancelSM extends Request {
	private String serviceType = Data.DFLT_SRVTYPE;
	private String messageId = Data.DFLT_MSGID;
	private Address sourceAddr = new Address();
	private Address destAddr = new Address();

	public CancelSM() {
		super(Data.CANCEL_SM);
	}

	protected Response createResponse() {
		return new CancelSMResp();
	}

	public void setBody(ByteBuffer buffer)
		throws NotEnoughDataInByteBufferException, TerminatingZeroNotFoundException, PDUException {
		setServiceType(buffer.removeCString());
		setMessageId(buffer.removeCString());
		sourceAddr.setData(buffer); // ?
		destAddr.setData(buffer); // ?
	}

	public ByteBuffer getBody() {
		ByteBuffer buffer = new ByteBuffer();
		buffer.appendCString(getServiceType());
		buffer.appendCString(messageId);
		buffer.appendBuffer(getSourceAddr().getData());
		buffer.appendBuffer(getDestAddr().getData());
		return buffer;
	}

	public void setServiceType(String value) throws WrongLengthOfStringException {
		checkCString(value, Data.SM_SRVTYPE_LEN);
		serviceType = value;
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

	public void setDestAddr(Address value) {
		destAddr = value;
	}
	public void setDestAddr(String address) throws WrongLengthOfStringException {
		setDestAddr(new Address(address));
	}
	public void setDestAddr(byte ton, byte npi, String address) throws WrongLengthOfStringException {
		setDestAddr(new Address(ton, npi, address));
	}

	public String getServiceType() {
		return serviceType;
	}
	public String getMessageId() {
		return messageId;
	}
	public Address getSourceAddr() {
		return sourceAddr;
	}
	public Address getDestAddr() {
		return destAddr;
	}

	public String debugString() {
		String dbgs = "(cancel: ";
		dbgs += super.debugString();
		dbgs += getServiceType();
		dbgs += " ";
		dbgs += getMessageId();
		dbgs += " ";
		dbgs += getSourceAddr().debugString();
		dbgs += " ";
		dbgs += getDestAddr().debugString();
		dbgs += ") ";
		return dbgs;
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
