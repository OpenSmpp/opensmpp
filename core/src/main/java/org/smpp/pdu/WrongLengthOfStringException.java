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
 * @version $Revision: 1.1 $
 */
public class WrongLengthOfStringException extends PDUException {
	private static final long serialVersionUID = 8604133584902790266L;

	public WrongLengthOfStringException() {
		super("The string is shorter or longer than required.");
		setErrorCode(Data.ESME_RINVPARAM);
	}

	public WrongLengthOfStringException(int min, int max, int actual) {
		super(
			"The string is shorter or longer than required: "
				+ " min="
				+ min
				+ " max="
				+ max
				+ " actual="
				+ actual
				+ ".");
		setErrorCode(Data.ESME_RINVPARAM);
	}
}
/*
 * $Log: not supported by cvs2svn $
 */