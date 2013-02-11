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
package org.smpp.pdu.tlv;

/**
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.2 $
 */
public class WrongLengthException extends TLVException {
	private static final long serialVersionUID = 7935018427341458286L;

	public WrongLengthException() {
		super("The TLV is shorter or longer than allowed.");
	}

	public WrongLengthException(int min, int max, int actual) {
		super(
			"The TLV is shorter or longer than allowed: " + " min=" + min + " max=" + max + " actual=" + actual + ".");
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.1  2003/07/23 00:28:39  sverkera
 * Imported
 *
 */
