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
 * Thrown when only a part of PDU was received and the rest of the PDU 
 * hasn't been received for too long time.
 *
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.2 $
 */
public class TimeoutException extends SmppException {
	private static final long serialVersionUID = 4873432724200896611L;

	/** The expired timeout. */
	public long timeout = 0;

	/** The expected bytes. */
	public int expected = 0;

	/** The received bytes. */
	public int received = 0;

	/** Don't allow default constructor */
	@SuppressWarnings("unused")
	private TimeoutException() {
	}

	/**
	 * Construct with provided timeout and expected and received amount
	 * of data.
	 */
	public TimeoutException(long timeout, int expected, int received) {
		super(
			"The rest of pdu not received for "
				+ (timeout / 1000)
				+ " seconds. "
				+ "Expected "
				+ expected
				+ " bytes, received "
				+ received
				+ " bytes.");
		this.timeout = timeout;
		this.expected = expected;
		this.received = received;
	}

}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.1  2003/07/23 00:28:39  sverkera
 * Imported
 *
 * 
 * Old changelog:
 *  01-10-01 ticp@logica.com javadoc wording improved
 */
