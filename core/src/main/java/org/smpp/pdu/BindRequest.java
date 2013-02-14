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
public abstract class BindRequest extends Request {
	private String systemId = Data.DFLT_SYSID;
	private String password = Data.DFLT_PASS;
	private String systemType = Data.DFLT_SYSTYPE;
	private AddressRange addressRange = new AddressRange();
	private byte interfaceVersion = Data.SMPP_V34;

	public abstract boolean isTransmitter();
	public abstract boolean isReceiver();

	public BindRequest(int commandId) {
		super(commandId);
	}

	public void setBody(ByteBuffer buffer)
		throws NotEnoughDataInByteBufferException, TerminatingZeroNotFoundException, PDUException {
		setSystemId(buffer.removeCString());
		setPassword(buffer.removeCString());
		setSystemType(buffer.removeCString());
		setInterfaceVersion(buffer.removeByte());
		addressRange.setData(buffer);
	}

	public ByteBuffer getBody() {
		ByteBuffer buffer = new ByteBuffer();
		buffer.appendCString(getSystemId());
		buffer.appendCString(getPassword());
		buffer.appendCString(getSystemType());
		buffer.appendByte(getInterfaceVersion());
		buffer.appendBuffer(getAddressRange().getData());
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

	public void setSystemType(String type) throws WrongLengthOfStringException {
		checkString(type, Data.SM_SYSTYPE_LEN);
		systemType = type;
	}

	public void setInterfaceVersion(byte vers) {
		interfaceVersion = vers;
	}
	public void setAddressRange(AddressRange adr) {
		addressRange = adr;
	}
	public void setAddressRange(String rangeString) throws WrongLengthOfStringException {
		setAddressRange(new AddressRange(rangeString));
	}
	public void setAddressRange(byte ton, byte npi, String rangeString) throws WrongLengthOfStringException {
		setAddressRange(new AddressRange(ton, npi, rangeString));
	}

	public String getSystemId() {
		return systemId;
	}
	public String getPassword() {
		return password;
	}
	public String getSystemType() {
		return systemType;
	}
	public byte getInterfaceVersion() {
		return interfaceVersion;
	}
	public AddressRange getAddressRange() {
		return addressRange;
	}

	public String debugString() {
		String dbgs = "(bindreq: ";
		dbgs += super.debugString();
		dbgs += getSystemId();
		dbgs += " ";
		dbgs += getPassword();
		dbgs += " ";
		dbgs += getSystemType();
		dbgs += " ";
		dbgs += Integer.toString(getInterfaceVersion());
		dbgs += " ";
		dbgs += getAddressRange().debugString();
		dbgs += ") ";
		return dbgs;
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
