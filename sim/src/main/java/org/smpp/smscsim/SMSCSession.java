package org.smpp.smscsim;

import java.io.IOException;

import org.smpp.Connection;
import org.smpp.pdu.PDU;
import org.smpp.pdu.PDUException;

/* Created on 2003-jul-28 */

/**
 * Please enter a description of this class here
 * 
 * @author Sverker Abrahamsson
 *
 * @version $Id: SMSCSession.java 85 2010-04-26 13:39:31Z sverkera $
 *
 */
public interface SMSCSession extends Runnable {
	/**
	 * Signals the session's thread that it should stop.
	 * Doesn't wait for the thread to be completly finished.
	 * Note that it can take some time before the thread is completly
	 * stopped.
	 * @see #run()
	 */
	public abstract void stop();
	/**
	 * Implements the logic of receiving of the PDUs from client and passing
	 * them to PDU processor. First starts receiver, then in cycle
	 * receives PDUs and passes them to the proper PDU processor's
	 * methods. After the function <code>stop</code> is called (externally)
	 * stops the receiver, exits the PDU processor and closes the connection,
	 * so no extry tidy-up routines are necessary.
	 * @see #stop()
	 * @see PDUProcessor#clientRequest(Request)
	 * @see PDUProcessor#clientResponse(Response)
	 */
	public abstract void run();
	/**
	 * Sends a PDU to the client.
	 * @param pdu the PDU to send
	 */
	public abstract void send(PDU pdu) throws IOException, PDUException;
	/**
	 * Sets new PDU processor.
	 * @param pduProcessor the new PDU processor
	 */
	public abstract void setPDUProcessor(PDUProcessor pduProcessor);
	/**
	 * Sets PDU processor factory, used instead of setPDUProcessor in case the session creates
	 * the PDU processor.
	 * @param pduProcessorFactory the PDU processor factory
	 * @see #setPDUProcessor()
	 */
	public abstract void setPDUProcessorFactory(PDUProcessorFactory pduProcessorFactory);
	/**
	 * Sets the timeout for receiving the complete message.
	 * @param timeout the new timeout value
	 */
	public abstract void setReceiveTimeout(long timeout);
	/**
	 * Returns the current setting of receiving timeout.
	 * @return the current timeout value
	 */
	public abstract long getReceiveTimeout();

	/**
	 * Returns the details about the account that is logged in to this session
	 * @return An object representing the account. It is casted to the correct type by the implementation
	 */
	public abstract Object getAccount();

	/**
	 * Set details about the account that is logged in to this session 
	 * @param account An object representing the account. It is casted to the correct type by the implementation
	 */
	public abstract void setAccount(Object account);
	
	public abstract Connection getConnection();
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.2  2003/09/30 09:17:49  sverkera
 * Created an interface for SMSCListener and SMSCSession and implementations of them  so that it is possible to provide other implementations of these classes.
 *
 */