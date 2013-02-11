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
 * optional's parameter's value was requested but the optional parameter
 * wasn't present in the PDU
 *
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.1 $
 */
public class ValueNotSetException extends PDUException {
	private static final long serialVersionUID = -4595064103809398438L;

	public ValueNotSetException() {
		setErrorCode(Data.ESME_RMISSINGOPTPARAM);
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
