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
package org.smpp;

/**
 * Class <code>SmppException</code> is the root of all SMPP Library
 * exceptions. Every exception defined in the library <code>SmppException</code>
 * as a superclass -- this way class <code>SmppException</code>
 * provides single class for <code>catch</code> clause.
 * 
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Id: SmppException.java 72 2008-07-15 19:43:00Z sverkera $
 */

public class SmppException extends Exception {
	private static final long serialVersionUID = 3108928509613380097L;

	/**
	 * Constructs a <code>SmppException</code> with no specified detail
	 * message. 
	 */
	public SmppException() {
		super();
	}

	/**
	 * Constructs a <code>SmppException</code> with a nested exception.
	 * 
	 * @param   e   The nested exception
	 */
	public SmppException(Exception e) {
		super(e);
	}

	/**
	 * Constructs a <code>SmppException</code> with the specified detail
	 * message. 
	 *
	 * @param   s   the detail message.
	 */
	public SmppException(String s) {
		super(s);
	}

	/**
	 * Constructs a <code>SmppException</code> with the specified detail
	 * message and a nested exception
	 *
	 * @param   s   the detail message.
	 * @param   e   The nested exception
	 */
	public SmppException(String s, Exception e) {
		super(s, e);
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.2  2003/07/24 14:32:21  sverkera
 * Added support for nested exception
 *
 * Revision 1.1  2003/07/23 00:28:39  sverkera
 * Imported
 *
 */
