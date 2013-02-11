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
public class WrongDestFlagException extends PDUException {
	private static final long serialVersionUID = 6266749651012701472L;

	public WrongDestFlagException() {
		setErrorCode(Data.ESME_RINVPARAM);
	}

	public WrongDestFlagException(PDU pdu) {
		super(pdu);
		setErrorCode(Data.ESME_RINVPARAM);
	}

	public WrongDestFlagException(String s) {
		super(s);
		setErrorCode(Data.ESME_RINVPARAM);
	}

	public WrongDestFlagException(PDU pdu, String s) {
		super(pdu, s);
		setErrorCode(Data.ESME_RINVPARAM);
	}
}
