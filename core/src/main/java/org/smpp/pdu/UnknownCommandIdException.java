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

/**
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.1 $
 */
public class UnknownCommandIdException extends PDUException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5091873576710864441L;
	private transient PDUHeader header = null;

	public UnknownCommandIdException() {
	}

	public UnknownCommandIdException(PDUHeader header) {
		this.header = header;
	}

	public int getCommandLength() {
		return header == null ? 0 : header.getCommandLength();
	}

	public int getCommandId() {
		return header == null ? 0 : header.getCommandId();
	}

	public int getCommandStatus() {
		return header == null ? 0 : header.getCommandStatus();
	}

	public int getSequenceNumber() {
		return header == null ? 0 : header.getSequenceNumber();
	}
}
/*
 * $Log: not supported by cvs2svn $
 * 
 * Old changelog:
 * 10-10-01 ticp@logica.com pdu header carried by the exception made transient
 *						    (it is not serializable)
 */
