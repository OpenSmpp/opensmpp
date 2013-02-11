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

import java.io.IOException;

import org.smpp.Data;
import org.smpp.util.ByteBuffer;

/**
 * Abstract class defining necessary abstract methods for communication
 * over socket based network communication interface. It defines methods
 * for both client & server connection.<br>
 * <code>Session</code>, <code>Transmitter</code> and <code>Receiver</code>
 * classes use this class for sending and receiving data to and from SMSC.
 * 
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Id: Connection.java 80 2009-10-28 02:13:18Z sverkera $
 * @see TCPIPConnection
 */
public abstract class Connection extends SmppObject {
	/**
	 * Timeout for underlying communication calls.
	 */
	private long commsTimeout = Data.COMMS_TIMEOUT;

	/**
	 * Timeout for receiving data from connection and for accepting
	 * new connection.
	 */
	private long receiveTimeout = Data.CONNECTION_RECEIVE_TIMEOUT;

	/**
	 * The address of the remote end of the connection. 
	 */
	protected String address = null;

	protected Connection() {
	}

	/**
	 * This method should open the connection using communication parameters
	 * set up by constructor of the <code>Connection</code> object.
	 * 
	 * @exception java.io.IOException Opening the connection can cause
	 * IOException in the derived classes.
	 */
	public abstract void open() throws IOException;

	/**
	 * This method should close the connection previously opened by
	 * the <code>open()</code> method.
	 * 
	 * @exception java.io.IOException Closing the connection can cause
	 * IOException in the derived classes.
	 */
	public abstract void close() throws IOException;

	/**
	 * This method returns true if the connection is open.
	 */
	public abstract boolean isOpened();

	/**
	 * This method should send data over the connection.
	 * The timeout for sending is commsTimeout. If the data couldn't
	 * be sent, IOException should be thrown.
	 * 
	 * @param data the data which have to be sent
	 * @exception java.io.IOException Sending a data over connection
	 * can cause IOException in the derived classes.
	 */
	public abstract void send(ByteBuffer data) throws IOException;

	/**
	 * This method should receive data from the connection.
	 * The timeout for receiving is receiveTimeout. If no data are
	 * received within time specified by the timeout, null should be
	 * returned.
	 * 
	 * @return the data received from the connection
	 * @exception java.io.IOException Receiving data over connection
	 * can cause IOException in the derived classes.
	 */
	public abstract ByteBuffer receive() throws IOException;

	/**
	 * This method should wait for connection. If the connection
	 * is requested then this method should create new instance
	 * of connection and return it.
	 * 
	 * @return the new accepted connection
	 * @exception java.io.IOException waiting for connection can cause
	 * an IOException
	 */
	public abstract Connection accept() throws IOException;

	/**
	 * Sets timeout used for calls to underlying communication
	 * functions for an instance of the derived Connection class.
	 *
	 * @param commsTimeout new communication timeout value
	 */
	public synchronized void setCommsTimeout(long commsTimeout) {
		this.commsTimeout = commsTimeout;
	}

	/**
	 * Sets timeout used for receiving data from connection using
	 * <code>receive</code> method for an instance of the derived
	 * Connection class.
	 *
	 * @param receiveTimeout new <code>receive</code> timeout value
	 */
	public synchronized void setReceiveTimeout(long receiveTimeout) {
		this.receiveTimeout = receiveTimeout;
	}

	/**
	 * Returns currently set timeout used for calls to underlying
	 * communication functions for an instance of the derived
	 * Connection class.
	 *
	 * @return value of communication timeout
	 */
	public synchronized long getCommsTimeout() {
		return commsTimeout;
	}

	/**
	 * Returns timeout used for receiving data from connection using
	 * <code>receive</code> method for an instance of the derived
	 * Connection class.
	 *
	 * @return value of timeout for <code>receive</code>
	 */
	public synchronized long getReceiveTimeout() {
		return receiveTimeout;
	}

	public String getAddress() {
		return address;
	}
}

/*
 * $Log: not supported by cvs2svn $
 * Revision 1.2  2004/09/10 23:03:44  sverkera
 * Added isOpened method
 *
 * Revision 1.1  2003/07/23 00:28:39  sverkera
 * Imported
 *
 */
