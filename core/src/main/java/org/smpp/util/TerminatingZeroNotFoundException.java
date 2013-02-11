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
package org.smpp.util;

import org.smpp.SmppException;

/**
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.2 $
 */
public class TerminatingZeroNotFoundException extends SmppException {
	private static final long serialVersionUID = 7028315742573472677L;

	public TerminatingZeroNotFoundException() {
		super("Terminating zero not found in buffer.");
	}

	public TerminatingZeroNotFoundException(String s) {
		super(s);
	}

}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.1  2003/07/23 00:29:08  sverkera
 * Imported
 *
 */
