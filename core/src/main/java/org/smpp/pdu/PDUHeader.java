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

import org.smpp.util.ByteBuffer;
import org.smpp.util.NotEnoughDataInByteBufferException;

/**
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.1 $
 */
public class PDUHeader extends ByteData {
	private int commandLength = 0;
	private int commandId = 0;
	private int commandStatus = 0;
	private int sequenceNumber = 1;

	public ByteBuffer getData() {
		ByteBuffer buffer = new ByteBuffer();
		buffer.appendInt(getCommandLength());
		buffer.appendInt(getCommandId());
		buffer.appendInt(getCommandStatus());
		buffer.appendInt(getSequenceNumber());
		return buffer;
	}

	public void setData(ByteBuffer buffer) throws NotEnoughDataInByteBufferException {
		commandLength = buffer.removeInt();
		commandId = buffer.removeInt();
		commandStatus = buffer.removeInt();
		sequenceNumber = buffer.removeInt();
	}

	public int getCommandLength() {
		return commandLength;
	}

	public int getCommandId() {
		return commandId;
	}

	public int getCommandStatus() {
		return commandStatus;
	}

	public int getSequenceNumber() {
		return sequenceNumber;
	}

	public void setCommandLength(int cmdLen) {
		commandLength = cmdLen;
	}

	public void setCommandId(int cmdId) {
		commandId = cmdId;
	}

	public void setCommandStatus(int cmdStatus) {
		commandStatus = cmdStatus;
	}

	public void setSequenceNumber(int seqNr) {
		sequenceNumber = seqNr;
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
