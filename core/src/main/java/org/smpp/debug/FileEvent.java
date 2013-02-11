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
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.1 $
 */
public class FileEvent implements Event {
	private FileLog log = null;

	public FileEvent(String dir, String name) {
		log = new FileLog(dir, name);
		activate();
	}

	public void write(String msg) {
		if (isActive()) {
			log.genericWrite(msg == null ? "" : msg);
		}
	}

	public void write(Exception e, String msg) {
		if (isActive()) {
			StringWriter stackOutString = new StringWriter();
			PrintWriter stackOut = new PrintWriter(stackOutString);
			e.printStackTrace(stackOut);
			try {
				if (msg != null) {
					write(msg);
				}
				write("Exception: " + stackOutString.toString());
			} catch (Exception ex) {
				System.err.println("Event log failure " + ex);
			}
		}
	}

	public void activate() {
		if (log != null)
			log.activate();
	}
	public void deactivate() {
		if (log != null)
			log.deactivate();
	}
	public boolean isActive() {
		return (log != null) ? log.isActive() : false;
	}
}
/*
 * $Log: not supported by cvs2svn $
 *
 * Old changelog:
 * 01-10-01 ticp@logica.com message passed to write methods can be null now
 * 09-10-01 ticp@logica.com when logging exception with extra text message
 *						    message is written on separate line now
 */
