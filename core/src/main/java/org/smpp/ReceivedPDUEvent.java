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

import java.util.EventObject;
import org.smpp.pdu.PDU;

/**
 * The base class for events representing receiving a pdu by
 * receiver.
 *
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.2 $
 */
public class ReceivedPDUEvent extends EventObject {
	private static final long serialVersionUID = 2888578757849035826L;

	/**
	 * The connection over which was the pdu received.
	 */
	private transient Connection connection = null;

	/**
	 * The received pdu.
	 */
	private transient PDU pdu = null;

	/**
	 * Construct event for pdu received over connection belonging
	 * to the receiver.
	 */
	public ReceivedPDUEvent(ReceiverBase source, Connection connection, PDU pdu) {
		super(source);
		this.connection = connection;
		this.pdu = pdu;
	}

	/**
	 * Return the connection over which the pdu was received.
	 */
	public Connection getConnection() {
		return connection;
	}

	/**
	 * Return the received pdu.
	 */
	public PDU getPDU() {
		return pdu;
	}

}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.1  2003/07/23 00:28:39  sverkera
 * Imported
 *
 * 
 * Old changelog:
 * 10-10-01 ticp@logica.com connection and pdu carried by the event made transient
 *                          (they are non-serializable)
 */
