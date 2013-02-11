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
public class UnexpectedOptionalParameterException extends PDUException {
	private static final long serialVersionUID = -1284359967986779783L;
	private int tag = 0;

	public UnexpectedOptionalParameterException() {
		super("The optional parameter wasn't expected for the PDU.");
		setErrorCode(Data.ESME_ROPTPARNOTALLWD);
	}

	public UnexpectedOptionalParameterException(short tag) {
		super("The optional parameter wasn't expected for the PDU:" + " tag=" + tag + ".");
		this.tag = tag;
		setErrorCode(Data.ESME_ROPTPARNOTALLWD);
	}

	public void setTag(int tag) {
		this.tag = tag;
	}

	public int getTag() {
		return tag;
	}
}
