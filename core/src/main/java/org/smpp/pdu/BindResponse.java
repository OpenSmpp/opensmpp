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
import org.smpp.pdu.tlv.*;
import org.smpp.util.*;

/**
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.2 $
 */
public abstract class BindResponse extends Response {

	// mandatory parameters
	private String systemId = Data.DFLT_SYSID;

	// optional parameters
	private TLVByte scInterfaceVersion = new TLVByte(Data.OPT_PAR_SC_IF_VER);

	public BindResponse(int commandId) {
		super(commandId);

		registerOptional(scInterfaceVersion);
	}

	public void setBody(ByteBuffer buffer)
		throws NotEnoughDataInByteBufferException, TerminatingZeroNotFoundException, PDUException {
		if (getCommandStatus() == 0) { // ok => have body
			setSystemId(buffer.removeCString());
		}
	}

	public ByteBuffer getBody() {
		ByteBuffer buffer = new ByteBuffer();
		//if (getCommandStatus() == 0) { // ok => append body
			buffer.appendCString(getSystemId());
		//}
		return buffer;
	}

	public void setSystemId(String sysId) throws WrongLengthOfStringException {
		checkString(sysId, Data.SM_SYSID_LEN);
		systemId = sysId;
	}

	public String getSystemId() {
		return systemId;
	}

	public boolean hasScInterfaceVersion() {
		return scInterfaceVersion.hasValue();
	}

	public void setScInterfaceVersion(byte value) {
		scInterfaceVersion.setValue(value);
	}

	public byte getScInterfaceVersion() throws ValueNotSetException {
		return scInterfaceVersion.getValue();
	}

	public String debugString() {
		String dbgs = "(bindresp: ";
		dbgs += super.debugString();
		dbgs += getSystemId();
		if (hasScInterfaceVersion()) {
			dbgs += " ";
			try {
				dbgs += getScInterfaceVersion();
			} catch (Exception e) {
				// don't want to throw exception in debug code!
			}
		}
		dbgs += ") ";
		return dbgs;
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.1  2003/07/23 00:28:39  sverkera
 * Imported
 *
 *
 * Old changelog:
 * 09-10-01 ticp@logica.com ID changed to Id in getSystemID and setSystemID
 */
