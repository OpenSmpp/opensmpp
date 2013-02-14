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

import java.util.EventListener;

/**
 * The interface <code>ServerPDUEventListener</code> defines method for processing
 * of PDUs received by the <code>Receiver</code> from the SMSC.
 * Implementation of this interface is used when the communication with
 * the SMSC is <i>asynchronous</i>. The asynchronous communication means that
 * the <code>Session</code> after sending a request to the SMSC doesn't wait for
 * a response to the request sent, instead it returns null. All PDUs received from the SMSC,
 * i.e  both responses to the sent requests and requests sent on behalf of
 * the SMSC, are passed to the instance of <code>ServerPDUEventListener</code>
 * implementation class. Users of the library are expected to implement
 * the listener.
 * <emp>Important:</emp>The <code>handleEvent</code> method is called
 * from the receiver's thread context, so the implementation of the listener
 * should ensure that there is no deadloock, or at least not too much
 * time spent in the method.
 *
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.2 $
 * @see Receiver#setServerPDUEventListener(ServerPDUEventListener)
 * @see Session#setServerPDUEventListener(ServerPDUEventListener)
 */
public interface ServerPDUEventListener extends EventListener {
	/**
	 * Meant to process PDUs received from the SMSC.
	 * This method is called by the <code>Receiver</code> whenever a
	 * PDU is received from the SMSC.
	 * @param event the event received from the SMSC
	 */
	public abstract void handleEvent(ServerPDUEvent event);

}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.1  2003/07/23 00:28:39  sverkera
 * Imported
 *
 */
