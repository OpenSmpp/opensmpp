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

import java.util.Stack;
import java.util.Hashtable;

/**
 * This implementation of <code>Debug</code> interface writes the trace lines
 * to a file, provides file switching and indentation dependant on the
 * calling thread's context. There is kept call stack and nesting for each
 * thread separately. The threads are recognized in <code>FileDebug</code>
 * and proper nesting and prefix dependent on the thread name and the 
 * actual function is generated. Debug groups are also fully supported.
 *
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.1 $
 */
public class FileDebug implements Debug {
	Hashtable<String, ThreadDebugInfo> threads = new Hashtable<String, ThreadDebugInfo>();

	private FileLog log = new FileLog();

	private static final int DFLT_GROUP_COUNT = 64;
	private int groupCount;
	private boolean groups[] = new boolean[groupCount];

	public FileDebug(String dir, String name) {
		log = new FileLog(dir, name);
		setGroupCount(DFLT_GROUP_COUNT);
		activate();
	}

	public void enter(int group, Object from, String name) {
		if (isActive(group) && isActive()) {
			enter(from, name);
		}
	}

	public void enter(Object from, String name) {
		if (isActive()) {
			ThreadDebugInfo thread = getThreadInfo();
			String className = from.getClass().getName();
			int i = className.lastIndexOf('.');
			if (i != -1) {
				className = className.substring(i + 1, className.length());
			}
			thread.enter(className, name);
			write("entered");
		}
	}

	public void write(int group, String msg) {
		if (isActive(group) && isActive()) {
			write(msg + " (" + group + ")");
		}
	}

	public void write(String msg) {
		if (isActive()) {
			ThreadDebugInfo thread = getThreadInfo();
			log.genericWrite(thread.signature() + " " + msg);
		}
	}

	public void exit(int group, Object from) {
		if (isActive(group) && isActive()) {
			exit(from);
		}
	}

	public void exit(Object from) {
		if (isActive()) {
			write("exited");
			ThreadDebugInfo thread = getThreadInfo();
			thread.exit();
		}
	}

	public void activate() {
		if (log != null)
			log.activate();
	}
	public void activate(int group) {
		if ((0 <= group) && (group < groupCount)) {
			groups[group] = true;
			write("trace group " + group + " activated");
		}
	}

	public void deactivate() {
		if (log != null)
			log.deactivate();
	}
	public void deactivate(int group) {
		if ((0 <= group) && (group < groupCount)) {
			groups[group] = false;
			write("trace group " + group + " deactivated");
		}
	}

	public boolean active(int group) {
		return isActive(group);
	}

	public boolean isActive() {
		return (log != null) ? log.isActive() : false;
	}

	public boolean isActive(int group) {
		return 0 <= group && group < groupCount ? groups[group] : false;
	}

	public void setGroupCount(int groupCount) {
		groups = new boolean[groupCount];
		this.groupCount = groupCount;
		for (int group = 0; group < groupCount; group++) {
			activate(group);
		}
	}

	private String getCurrentThreadName() {
		return Thread.currentThread().getName();
	}

	private ThreadDebugInfo getThreadInfo() {
		String threadName = getCurrentThreadName();
		ThreadDebugInfo thread;
		synchronized (threads) {
			thread = (ThreadDebugInfo) threads.get(threadName);
			if (thread == null) {
				thread = new ThreadDebugInfo(threadName);
				threads.put(threadName, thread);
			}
		}
		return thread;
	}

	/**
	 * Stores nesting and indentation and function and class information for
	 * thread.
	 */
	class ThreadDebugInfo {
		// the name of this thread
		String threadName;
		// the stack of the function calls
		Stack<String> callStack = new Stack<String>();
		// the current nesting within the thread
		int nesting = 0;
		// the current indentation within the thread
		String indent = "";
		// number of spaces for each enter
		static final int INDENT_POSITIONS = 3;

		public ThreadDebugInfo(String threadName) {
			this.threadName = threadName;
			// top level commands must run in some context, but we don't really
			/// want to enter just to have a name for the top level function
			callStack.push("main()");
		}

		public void enter(String className, String functionName) {
			enter(className + "." + functionName);
		}

		public void enter(String signature) {
			callStack.push(signature);
			nesting++;
			indent = generateIndent();
		}

		public void exit() {
			callStack.pop();
			if (nesting > 1) {
				nesting--;
			}
			indent = generateIndent();
		}

		public String signature() {
			return indent + threadName + ": " + (String) callStack.peek();
		}

		private String generateIndent() {
			String indent = "";
			String single = "";
			if (nesting >= 32) { // = 100000
				// rarely so nested, the punishment is the time spent
				// generating the indentation string :-)
				for (int i = 0; i < nesting; i++) {
					single += " ";
				}
			} else {
				// for speeding up construction in logarithmic construction time
				byte nbyte = (byte) (nesting & 0x01f); // = 31 = 11111;
				if ((nbyte & 0x10) != 0)
					single += "                ";
				if ((nbyte & 0x08) != 0)
					single += "        ";
				if ((nbyte & 0x04) != 0)
					single += "    ";
				if ((nbyte & 0x02) != 0)
					single += "  ";
				if ((nbyte & 0x01) != 0)
					single += " ";
			}
			for (int i = 0; i < INDENT_POSITIONS; i++) {
				indent += single;
			}
			return indent;
		}
	}
}
/*
 * $Log: not supported by cvs2svn $
 *
 * Old changelog:
 * 13-07-01 ticp@logica.com in enter(Object,name) new Stack was created even if
 *						    the stackHash already contained the object;
 *						    now the Stack is created only if th object isn't in
 *						    the hash
 * 13-07-01 ticp@logica.com indentation fixed, tabs aren't used anymore
 * 25-09-01 ticp@logica.com implemented groups of functionality
 * 02-10-01 ticp@logica.com rewritten, more readable (streamlined), thread
 *						    specific nesting (indentation)
 * 02-10-01 ticp@logica.com comments added
 * 16-10-01 ticp@logica.com added method active(group)
 */
