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

import java.io.IOException;

import org.smpp.pdu.*;

/**
 * Class <code>Transmitter</code> transmits PDUs over connection.
 * It is used by <code>Session</code>.
 *
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.2 $
 * @see Connection
 * @see Receiver
 * @see Session
 */
public class Transmitter extends SmppObject {
	/**
	 * The connection object. It is used for transmitting the PDUs. It's
	 * created outside of the <code>Transmitter</code> and passed to
	 * transmitter as a constructor parameter.
	 * @see Connection
	 */
	private Connection connection = null;

	/**
	 * Default constructor made protected as it's not desirable to
	 * allow creation of <code>Transmitter</code> without providing 
	 * <code>Connection</code>.
	 */
	protected Transmitter() {
	}

	/**
	 * Creates <code>Transmitter</code> which uses provided
	 * <code>Connection</code>. Typically the <code>connection</code>
	 * parameter will be an instance of <code>TCPIPConnection</code> class.
	 *
	 * @param c connection used for transmitting the PDUs
	 */
	public Transmitter(Connection c) {
		connection = c;
	}

	/**
	 * Assigns unique sequence number to PDU, if necessary, and sends its
	 * data over connection.
	 *
	 * @param pdu the PDU to send
	 *
	 * @exception IOException exception during communication
	 * @exception ValueNotSetException optional param not set but requested
	 */
	public void send(PDU pdu) throws ValueNotSetException, IOException {
		debug.enter(DCOM, this, "send");
		pdu.assignSequenceNumber();
		try {
			debug.write(DCOM, "going to send pdu's data over connection");
			connection.send(pdu.getData());
			debug.write(DCOM, "successfully sent pdu's data over connection");
		} finally {
			debug.exit(DCOM, this);
		}
	}

}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.1  2003/07/23 00:28:39  sverkera
 * Imported
 *
 * 
 * Old changelog:
 * 28-09-01 ticp@logica.com traces added
 */
