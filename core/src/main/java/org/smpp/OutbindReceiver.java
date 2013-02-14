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
import java.io.InterruptedIOException;

import org.smpp.Data;
import org.smpp.pdu.*;
import org.smpp.pdu.tlv.TLVException;
import org.smpp.util.*;

/**
 * This class is for receiving outbind request from SMSC.
 * It listens on server socket for client connection, if the connection
 * is accepted and created, it reads a PDU on the connection and if it is 
 * outbind pdu, it creates an <code>OutbindEvent</code> and passes it to the
 * <code>OutbindEventListener</code>.
 *
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.1 $
 */

public class OutbindReceiver extends ReceiverBase {
	/**
	 * Name of the thread created when starting
	 * the <code>ProcessingThread</code>.
	 */
	private static final String OUTBIND_RECEIVER_THREAD_NAME = "OutbindRcv";

	/**
	 * The connection on which is the outbind receiver listening for
	 * client request.
	 */
	private Connection serverConnection = null;

	/**
	 * The timeout for the single attempt to accept a client connection.
	 */
	private long acceptTimeout = Data.ACCEPT_TIMEOUT;

	/**
	 * The listener which is passed the accepted connection with
	 * the <code>Outbind</code> pdu.
	 */
	private OutbindEventListener listener = null;

	/**
	 * The buffer for unprocessed data received from the connection.
	 * Used when incomplete data are received from the connection
	 * in one try.
	 */
	private Unprocessed unprocessed = new Unprocessed();

	/**
	 * Disable instantiation without connection.
	 */
	@SuppressWarnings("unused")
	private OutbindReceiver() {
	}

	/**
	 * Instantiate with the server connection.
	 */
	public OutbindReceiver(Connection serverConnection) {
		this.serverConnection = serverConnection;
	}

	protected PDU tryReceivePDU(Connection connection, PDU expectedPDU)
		throws UnknownCommandIdException, TimeoutException, PDUException, IOException {
		PDU pdu = null;
		pdu = receivePDUFromConnection(connection, unprocessed);
		if (pdu != null) {
			if (!pdu.equals(expectedPDU)) {
				pdu = null;
			}
		}
		return pdu;
	}

	protected void receiveAsync() {
		PDU pdu;
		Connection connection = null;
		// Outbind is the expected PDU, see Outbind.equals() (doesn't care
		// about sequence number)
		Outbind outbind = new Outbind();
		try {
			serverConnection.setReceiveTimeout(getAcceptTimeout());
			connection = serverConnection.accept();
		} catch (InterruptedIOException e) {
			// thrown when the timeout expires => it's ok, we just didn't
			// receive anything
		} catch (IOException e) {
			// accept can throw this from various reasons
			// and we don't want to continue any more
			stopProcessing(e);
		}
		if (connection != null) {
			unprocessed.reset(); // have new connection => reset old unprocessed
			pdu = null;
			try {
				pdu = tryReceivePDUWithTimeout(connection, outbind);
				// we must catch every exception as this is thread running
				// on the background and we don't want it to terminate
				// in uncontrolled manner
			} catch (InvalidPDUException e) {
				event.write(e, "Buffer didn't contain enough data, continuing.");
			} catch (UnknownCommandIdException e) {
				event.write(e, "Unknown command id, continuing.");
			} catch (TimeoutException e) {
				debug.write("Timeout expired, message not received completly in time.");
			} catch (TLVException e) {
				event.write(e, "Wrong TLV");
			} catch (PDUException e) {
				event.write(e, "Wrong PDU");
			} catch (Exception e) {
				event.write(e, "Unspecified exception");
				stopProcessing(e);
			}
			if (pdu != null) {
				if (pdu.getCommandId() == Data.OUTBIND) {
					debug.write("Got outbind PDU, sending event info. " + pdu.debugString());
					sendOutbindEvent(new OutbindEvent(this, connection, (Outbind) pdu));
				} else {
					debug.write("PDU isn't outbind, throwing away. " + pdu.debugString());
					// not outbind => throw the pdu away
				}
			}
		}
	}

	/**
	 * Sends the outbind event to the listener.
	 */
	private void sendOutbindEvent(OutbindEvent event) {
		if (listener != null) {
			listener.handleOutbind(event);
		}
	}

	/**
	 * Sets the timeout for accepting a connection.
	 * If the timeout expires, the receiver thread loops and tries to accept
	 * the connection again. (Infinite cycles are evil.)
	 */
	public synchronized void setAcceptTimeout(long acceptTimeout) {
		this.acceptTimeout = acceptTimeout;
	}

	/**
	 * Sets the listener for receiving the <code>OutbindEvent</code>.
	 */
	public void setOutbindListener(OutbindEventListener listener) {
		this.listener = listener;
	}

	/**
	 * Returns the current setting of the accept timeout.
	 */
	public synchronized long getAcceptTimeout() {
		return acceptTimeout;
	}

	/**
	 * Returns the current outbind listener.
	 */
	public OutbindEventListener getOutbindListener() {
		return listener;
	}

	// ProcessingThread's getThreadName override
	public String getThreadName() {
		return OUTBIND_RECEIVER_THREAD_NAME;
	}
}
/*
 * $Log: not supported by cvs2svn $
 *
 * Old changelog:
 * 01-10-01 ticp@logica.com added function getThreadName for ProcessingThread
 *						   thread name initialisation -- reflecting the intro 
 *						   of ProcessingThread in the hierarchy
 * 01-10-01 ticp@logica.com some more comments added
 */
