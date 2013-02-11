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

/**
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.2 $
 */
public class InvalidPDUException extends PDUException {
	private static final long serialVersionUID = -6985061862208729984L;
	private Exception underlyingException = null;

	public InvalidPDUException(PDU pdu, Exception e) {
		super(pdu, e);
		underlyingException = e;
		setErrorCode(Data.ESME_RINVMSGLEN);
	}

	public InvalidPDUException(PDU pdu, String s) {
		super(pdu, s);
		setErrorCode(Data.ESME_RINVMSGLEN);
	}

	public String toString() {
		String s = super.toString();
		if (underlyingException != null) {
			s += "\nUnderlying exception: " + underlyingException.toString();
		}
		return s;
	}

	public Exception getException() {
		return underlyingException;
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.1  2003/07/23 00:28:39  sverkera
 * Imported
 *
 */
