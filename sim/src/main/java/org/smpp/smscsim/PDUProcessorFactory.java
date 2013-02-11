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
package org.smpp.smscsim;

/**
 * Descandants of this class are passed to <code>SMSCListener</code> for
 * generating instancies of classes derived from <code>PDUProcessor</code>.
 * User should set up their derived classes so that they contain all
 * information necessary for generating new PDU processors.
 * This way is the logic of the PDU processing isolated from logic of
 * establishing new connections (sessions).
 *
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.1 $
 * @see PDUProcessor
 * @see SMSCListener
 * @see SMSCSession
 */
public interface PDUProcessorFactory {
	/**
	 * Should generate proper PDU processor for processing of PDUs.
	 * @param session the session the PDU processor should work on
	 * @return the new PDU processor for processing reqests and responses
	 */
	public abstract PDUProcessor createPDUProcessor(SMSCSession session);
}
/*
 * $Log: not supported by cvs2svn $
 */
