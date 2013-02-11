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
 * Simple implementation of <code>Debug</code> interface which writes
 * the trace lines to the <code>System.out</code> and provides simple
 * indentation. This is the class whose instance is assigned as a default
 * debug class to the <code>SmppObject</code>'s debug object.
 *
 * @see Debug
 * @see org.smpp.SmppObject
 *
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.1 $
 */
public class DefaultDebug implements Debug {
	private int indent = 0;
	private boolean active = false;

	public void enter(int group, Object from, String name) {
		enter(from, name);
	}

	public void enter(Object from, String name) {
		if (active) {
			System.out.println(getDelimiter(true, from, name));
			indent++;
		}
	}

	public void write(int group, String msg) {
		write(msg);
	}

	public void write(String msg) {
		if (active) {
			System.out.println(getIndent() + " " + msg);
		}
	}

	public void exit(int group, Object from) {
		exit(from);
	}

	public void exit(Object from) {
		if (active) {
			indent--;
			if (indent < 0) {
				// it's your fault :-)
				indent = 0;
			}
			System.out.println(getDelimiter(false, from, ""));
		}
	}

	public void activate() {
		active = true;
	}
	public void activate(int group) {
	}
	public void deactivate() {
		active = false;
	}
	public void deactivate(int group) {
	}

	public boolean active(int group) {
		return true;
	}

	private String getDelimiter(boolean start, Object from, String name) {
		String indentStr = getIndent();
		if (start) {
			indentStr += "-> ";
		} else {
			indentStr += "<- ";
		}
		return indentStr + from.toString() + (name == "" ? "" : " " + name);
	}

	private String getIndent() {
		String result = new String("");
		for (int i = 0; i < indent; i++) {
			result += "  ";
		}
		return result;
	}
}
/*
 * $Log: not supported by cvs2svn $
 *
 * Old changelog:
 * 25-09-01 ticp@logica.com added functions for grouping althoug grouping not
 *						    supported
 * 16-10-01 ticp@logica.com added method active(group)
 */
