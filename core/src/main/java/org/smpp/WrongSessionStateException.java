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

/**
 * Exception <code>WrongSessionStateException</code> is thrown if
 * a <code>Session</code>'s method which requires certain state of the session
 * but the session is not in the state.
 * Examples are submitting a message when the session is bound as a receiver
 * or if method for receiving is called when the session is bound as transmitter.
 *
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.2 $
 */
public class WrongSessionStateException extends SmppException {
	private static final long serialVersionUID = 7296414687928430713L;

	/** If the excpetion was initialised with details about the session. */
	boolean hasSessionDetails;

	/**
	 * The type of session, e.g. Session.TYPE_ESME.
	 * @see Session#setType(int)
	 */
	int sessionType;

	/**
	 * The state the session was expected to be in for the operation.
	 */
	int expectedState;

	/**
	 * The state the session actually was when the operation was invoked.
	 */
	int currentState;

	/** Initializes the exception with default message. */
	public WrongSessionStateException() {
		super("The operation required is not possible in the current session state.");
		hasSessionDetails = false;
	}

	/**
	 * Initializes the exception with details about the session.
	 * @param sessionType the type of the session i.e. if it's session used
	 *                    in ESME or in MC
	 * @param expectedState the state expected for the requested session operation
	 * @param currentState the state the sesion was in when the state was checked
	 */
	public WrongSessionStateException(int sessionType, int expectedState, int currentState) {
		this();
		hasSessionDetails = true;
		this.sessionType = sessionType;
		this.expectedState = expectedState;
		this.currentState = currentState;
	}

	/**
	 * Construct the message carried by the excpetion according the details provided
	 * to the exception's constructor.
	 * @return the exception's message string describing the reason for the exception
	 */
	public String getMessage() {
		if (hasSessionDetails) {
			String typeDescription = "";
			switch (sessionType) {
				case Session.TYPE_ESME :
					typeDescription = "ESME";
					break;
				case Session.TYPE_MC :
					typeDescription = "MC";
					break;
				default :
					typeDescription = "UNKNOWN";
					break;
			}

			String msg;
			if (expectedState != Session.STATE_NOT_ALLOWED) {
				msg =
					"The operation is not allowed in the current "
						+ typeDescription
						+ " session state. "
						+ "Current state is "
						+ getStateDescription(currentState)
						+ " required state(s) is "
						+ getStateDescription(expectedState)
						+ ".";
			} else {
				msg = "The operation is not allowed in " + typeDescription + " session. ";
			}
			return msg;
		} else {
			return super.getMessage();
		}
	}

	/**
	 * Returns the textual representation of the passed state.
	 * Can handle multiple states, e.g. for value "STATE_OPENED | STATE_CLOSED"
	 * returns string "closed, opened".
	 * @param state the state whose description has to be returned
	 * @return string containing list of textual representations of the state
	 */
	public static String getStateDescription(int state) {
		String descr = "";
		descr += getStateDescription(state, Session.STATE_CLOSED, descr, "closed");
		descr += getStateDescription(state, Session.STATE_OPENED, descr, "opened");
		descr += getStateDescription(state, Session.STATE_TRANSMITTER, descr, "transmitter");
		descr += getStateDescription(state, Session.STATE_RECEIVER, descr, "receiver");
		descr += getStateDescription(state, Session.STATE_TRANSCEIVER, descr, "transceiver");
		descr += getStateDescription(state, Session.STATE_ALWAYS, descr, "any");
		if (descr.equals("")) {
			descr = "unknown";
		}
		return descr;
	}

	/**
	 * Returns value of <code>descr</code> if the <code>state</code> and
	 * <code>testState</code> have at least one of the bits equaly set to 1.
	 * Uses <code>currentDescr</code> for checking if the returned string
	 * should be prefixed with comma.
	 * @param state the state value you probe
	 * @param testState the state constant for which you provide <code>descr</code>
	 * @param currentDescr if you are concatenating descriptions, pass the
	 *                     previously gained description here
	 * @param descr the textual description of the state
	 * @return <code>descr</code> if the <code>state</code> contains
	 *         <code>testState</code>, empty string otherwise
	 */
	public static String getStateDescription(int state, int testState, String currentDescr, String descr) {
		if ((state & testState) == testState) {
			if (currentDescr.length() > 0) {
				return ", " + descr;
			} else {
				return descr;
			}
		} else {
			return "";
		}
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.1  2003/07/23 00:28:39  sverkera
 * Imported
 *
 */
