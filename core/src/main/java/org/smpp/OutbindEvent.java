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

import org.smpp.pdu.Outbind;

/**
 * Created when outbind pdu is received from smsc.
 * This event is created and passed to the <code>OutbindEventListener</code> by
 * the <code>OutbindReceiver</code>.
 *
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.2 $
 * @see OutbindReceiver
 * @see OutbindEventListener
 */

/*
  21-08-01 ticp@logica.com now derived from common ReceivedPDUEvent
                           and functionality covered by new parent
                           removed from this class
*/

public class OutbindEvent extends ReceivedPDUEvent {
	private static final long serialVersionUID = 1808913846085130877L;

	/**
	 * Construct event for outbind pdu received over conection
	 * belonging to the outbind receiver.
	 */
	public OutbindEvent(OutbindReceiver source, Connection connection, Outbind outbindPDU) {
		super(source, connection, outbindPDU);
	}

	/**
	 * Returns the outbind receiver thru which was received the outbind pdu
	 * this event relates to.
	 */
	public OutbindReceiver getReceiver() {
		return (OutbindReceiver) getSource();
	}

	/**
	 * Returns the outbind pdu.
	 */
	public Outbind getOutbindPDU() {
		return (Outbind) getPDU();
	}

}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.1  2003/07/23 00:28:39  sverkera
 * Imported
 *
 */