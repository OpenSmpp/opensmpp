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
 * Exception <code>NotSynchronousException</code> is thrown when
 * <code>Session</code>'s (synchronous) method <code>receive</code> is called when
 * the <code>Receiver</code> is in asynchronous state, i.e when all PDUs received
 * from the SMSC are passed to an instance of implementation of
 * <code>ServerPDUListener</code> class.

 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.2 $
 */

public class NotSynchronousException extends SmppException {
	private static final long serialVersionUID = -2785891348929001265L;
	private Session session = null;

	public NotSynchronousException() {
	}

	public NotSynchronousException(Session session) {
		this.session = session;
	}

	public Session getSession() {
		return session;
	}

}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.1  2003/07/23 00:28:39  sverkera
 * Imported
 *
 */
