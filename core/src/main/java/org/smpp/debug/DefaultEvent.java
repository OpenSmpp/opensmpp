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

import java.io.StringWriter;
import java.io.PrintWriter;

/**
 * Simple implementation of <code>Event</code> interface which writes
 * the event information to the <code>System.out</code>. This is the class
 * whose instance is assigned as a default event class to the
 * <code>SmppObject</code>'s event object.
 *
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.1 $
 */
public class DefaultEvent implements Event {
	private boolean active = false;

	public void write(String msg) {
		if (active) {
			System.out.println(msg);
		}
	}

	public void write(Exception e, String msg) {
		if (active) {
			StringWriter stackOutString = new StringWriter();
			PrintWriter stackOut = new PrintWriter(stackOutString);
			e.printStackTrace(stackOut);
			try {
				write("Exception: " + stackOutString.toString() + " " + msg);
			} catch (Exception ex) {
				System.err.println("Event log failure " + ex);
			}
		}
	}

	public void activate() {
		active = true;
	}
	public void deactivate() {
		active = false;
	}
}
/*
 * $Log: not supported by cvs2svn $
 *
 * Old changelog:
 * 02-10-01 ticp@logica.com comments added, indentation changed -> spaces
 */
