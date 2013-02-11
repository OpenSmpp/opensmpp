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
 * Callback interface for <code>Outbind</code> requests received from SMSC.
 * The only method of this interface, <code>handleOutbind</code>, is called
 * whenever an outbind request is received from SMSC.<br>
 * <emp>Important:</emp>The <code>handleOutbind</code> method is called
 * from the receiver's thread context, so the implementation of the listener
 * should ensure that there is no deadloock, or at least not too much
 * time spent in the method.
 *
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.1 $
 * @see OutbindReceiver
 */

public interface OutbindEventListener extends EventListener {
	public void handleOutbind(OutbindEvent outbind);
}
/*
 * $Log: not supported by cvs2svn $
 *
 * Old changelog:
 * 02-10-01 ticp@logica.com comments added
 */
