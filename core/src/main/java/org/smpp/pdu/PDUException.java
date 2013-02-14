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
import org.smpp.SmppException;

/**
 * Incorrect format of PDU passed as a parameter or received from SMSC.
 *
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.2 $
 */
public class PDUException extends SmppException {
	private static final long serialVersionUID = 5174606627714918071L;
	private transient PDU pdu = null;
	private transient int errorCode = Data.ESME_RUNKNOWNERR;
	
	public PDUException() {
	}

	public PDUException(PDU pdu) {
		setPDU(pdu);
	}

	public PDUException(String s) {
		super(s);
	}

	public PDUException(PDU pdu, String s) {
		super(s);
		setPDU(pdu);
	}

	public PDUException(PDU pdu, Exception e) {
		super(e);
		setPDU(pdu);
	}

	public PDUException(PDU pdu, String s, Exception e) {
		super(s, e);
		setPDU(pdu);
	}

	public String toString() {
		String s = super.toString();
		if (pdu != null) {
			s += "\nPDU debug string: " + pdu.debugString();
		}
		return s;
	}

	public void setPDU(PDU pdu) {
		this.pdu = pdu;
	}
	public PDU getPDU() {
		return pdu;
	}
	public boolean hasPDU() {
		return pdu != null;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
	public int getErrorCode() {
		return errorCode;
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.1  2003/07/23 00:28:39  sverkera
 * Imported
 *
 *
 * Old changelog:
 * 10-10-01 ticp@logica.com pdu carried by the exception made transient
 *                          (it is not serializable)
 */
