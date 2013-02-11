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
 * The interface <code>Debug</code> is an interface to application specific
 * debug information trace facility. Users of the SMPP library can either
 * use one of the two implementations of the <code>Debug</code> interface --
 * <code>DefaultDebug</code> or <code>FileDebug</code> or they can implement
 * their own class or create an adapter to their legacy trace facility.
 * The SMPP library's root class, <code>SmppObject</code> contains
 * object of type <code>Debug</code> so that all descendants of the
 * <code>SmppObject</code> class can write trace lines.
 * <br>
 * The interface contains methods to write lines of trace information
 * and also support functions to structure the written information according
 * function call nesting. Morover, the users can group their tracing
 * code to groups according functional areas and turn on and of these
 * areas.
 *
 * @see DefaultDebug
 * @see FileDebug
 * @see org.smpp.SmppObject
 * @see org.smpp.SmppObject#setDebug(Debug)
 * @see org.smpp.SmppObject#getDebug()
 *
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.1 $
 */
public interface Debug {
	/**
	 * Used to enter tracing in the function in the class if the provided
	 * group is turned on. Entering usually means that the trace lines
	 * indentation increases and the lines get new prefix
	 * based on the name of the class and the name of the function.
	 */
	public void enter(int group, Object from, String name);

	/**
	 * Used to always enter tracing in the function in the class.
	 * Entering usually means that the trace lines
	 * indentation increases and the lines get new prefix
	 * based on the name of the class and the name of the function.
	 */
	public void enter(Object from, String name);

	/**
	 * Used to write a line of trace if the provided group is turned on.
	 */
	public void write(int group, String msg);

	/**
	 * Used to always write a line of trace.
	 */
	public void write(String msg);

	/**
	 * Used to exit tracing in the function in the class if the provided
	 * group is turned on. Exiting usually means that the trace lines
	 * indentation decreases and the prefix of lines is restored to
	 * the value set before call to the corresponding enter.
	 */
	public void exit(int group, Object from);

	/**
	 * Used to exit tracing in the function in the class.
	 * Exiting usually means that the trace lines
	 * indentation decreases and the prefix of lines is restored to
	 * the value set before call to the corresponding enter.
	 */
	public void exit(Object from);

	/**
	 * Activate the tracing, whatever it means.
	 */
	public void activate();

	/**
	 * Activate the tracing in specified group.
	 */
	public void activate(int group);

	/**
	 * Deactivate the tracing, whatever it means.
	 */
	public void deactivate();

	/**
	 * Deactivate the tracing in specified group.
	 */
	public void deactivate(int group);

	/**
	 * Returns if the given group is active.
	 */
	public boolean active(int group);
}
/*
 * $Log: not supported by cvs2svn $
 * 
 * Old changelog:
 * 25-09-01 ticp@logica.com added debug groups
 * 25-09-01 ticp@logica.com added comments
 * 16-10-01 ticp@logica.com added method active(group)
 */
