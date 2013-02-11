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
package org.smpp.debug;

/**
 * The interface <code>Event</code> is an interface to application specific
 * event trace facility. It is used in the library to notify about
 * like exception. Implementors are expected either to use one of the
 * predefined implementations <code>DefaultEvent</code> or 
 * <code>FileEvent</code> or that the will write an implementation
 * which will adapt this interface to their legacy tracing facility.
 *
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.1 $
 */
public interface Event {
	/** Sends a message to the event object. */
	public void write(String msg);

	/** Sends an information about exception to the event object. */
	public void write(Exception e, String msg);

	/** Activates the event tracing. */
	public void activate();

	/** Deactivates the event tracing. */
	public void deactivate();
}
/*
 * $Log: not supported by cvs2svn $
 *
 * Old changelog:
 * 02-10-01 ticp@logica.com comments added, indentation changed -> spaces
 */
