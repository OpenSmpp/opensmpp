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
public class WrongDateFormatException extends PDUException {
	private static final long serialVersionUID = 5831937612139037591L;

	public WrongDateFormatException() {
		super("Date must be either null or of format YYMMDDhhmmsstnnp");
		setErrorCode(Data.ESME_RINVPARAM);
	}

	public WrongDateFormatException(String dateStr) {
		super("Date must be either null or of format YYMMDDhhmmsstnnp and not " + dateStr + ".");
		setErrorCode(Data.ESME_RINVPARAM);
	}

	public WrongDateFormatException(String dateStr, String msg) {
		super("Invalid date " + dateStr + ": " + msg);
		setErrorCode(Data.ESME_RINVPARAM);
	}
}
