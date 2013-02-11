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

import org.smpp.ServerPDUEvent;
import org.smpp.ServerPDUEventListener;
import org.smpp.SmppObject;
import org.smpp.pdu.PDU;

/**
 * Simple listener processing PDUs received from SMSC by the
 * <code>Receiver</code> in asynchronous mode.
 *
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.1 $
 * @see ServerPDUEventListener
 */
public class DefaultServerPDUEventListener extends SmppObject implements ServerPDUEventListener {
	/**
	 * "Handles" the event generated for received PDUs -- just logs
	 * the event and throws it away.
	 */
	public void handleEvent(ServerPDUEvent event) {
		PDU pdu = event.getPDU();
		if (pdu != null) {
			if (pdu.isRequest()) {
				debug.write(DUTL, "receiver listener: handling request " + pdu.debugString());
			} else if (pdu.isResponse()) {
				debug.write(DUTL, "receiver listener: handling response " + pdu.debugString());
			} else {
				debug.write(DUTL, "receiver listener: handling strange pdu " + pdu.debugString());
			}
		}
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
