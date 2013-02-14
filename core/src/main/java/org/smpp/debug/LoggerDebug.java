package org.smpp.debug;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of <code>Debug</code> interface which uses log4j
 * logger
 *
 * @see Debug
 * @see org.smpp.SmppObject
 *
 * @author Sverker Abrahamsson, LimeTransit AB
 * @version $Revision: 1.1 $
 */

public class LoggerDebug implements Debug {
	private Logger logger;
	private boolean active = false;
	private int indent = 0;

	public LoggerDebug(String category) {
		// get logger instance
		logger = Logger.getLogger(category);
	}

	public LoggerDebug(Logger logger) {
		this.logger = logger;
	}

	public void enter(int group, Object from, String name) {
		enter(from, name);
	}

	public void enter(Object from, String name) {
		if (active && logger.isLoggable(Level.FINE)) {
			logger.fine(getDelimiter(true, from, name));
			indent++;
		}
	}

	public void write(int group, String msg) {
		write(msg);
	}

	public void write(String msg) {
		if (active && logger.isLoggable(Level.FINE)) {
			logger.fine(getIndent() + " " + msg);
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
			logger.fine(getDelimiter(false, from, ""));
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
 */