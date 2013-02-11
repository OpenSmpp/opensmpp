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

import org.smpp.pdu.PDU;
import org.smpp.pdu.HeaderIncompleteException;
import org.smpp.pdu.MessageIncompleteException;
import org.smpp.pdu.PDUException;
import org.smpp.pdu.UnknownCommandIdException;
import org.smpp.util.ByteBuffer;
import org.smpp.util.ProcessingThread;
import org.smpp.util.Unprocessed;
import org.smpp.util.NotEnoughDataInByteBufferException;

/**
 * Abstract base class for classes which can receive PDUs from connection.
 * The receiving of PDUs can be be performed on background within a separate
 * thread using the <code>ProcessingThread</code> class.
 * 
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.4 $
 * @see Receiver
 * @see OutbindReceiver
 * @see Connection
 * @see Transmitter
 * @see Session
 */
public abstract class ReceiverBase extends ProcessingThread {

	/**
	 * Timeout for receiving the rest of PDU from the connection.
	 * If the rest of PDU isn't receive in time, <code>TimeoutException</code>
	 * is thrown.
	 *
	 * @see TimeoutException
	 */
	private long receiveTimeout = Data.RECEIVER_TIMEOUT;


	private byte messageIncompleteRetryCount = 0;


	/**
	 * Method repeatedly called from <code>process</code> method.
	 * It's expected that derived classes implement atomic receive of
	 * one PDU in this method using ReceiverBase's
	 * <code>tryReceivePDUWithTimeout</code> and 
	 * <code>receivePDUFromConnection</code> methods.
	 *
	 * @see #tryReceivePDUWithTimeout(Connection,PDU,long)
	 * @see ProcessingThread#process()
	 * @see ProcessingThread#run()
	 */
	protected abstract void receiveAsync();

	/**
	 * This method should try to receive one PDU from the connection.
	 * It is called in cycle from <code>tryReceivePDUWithTimeout</code> until
	 * timeout expires. The method should check if the actualy received
	 * PDU is equal to the <code>expectedPDU</code>.
	 *
	 * @param connection  the connection from which the PDU should be received
	 * @param expectedPDU the command id and sequence id of the received PDU
	 *                    should be equal to those of expectedPDU
	 * @return the received PDU if any or null if none received
	 *
	 * @exception IOException exception during communication
	 * @exception PDUException incorrect format of PDU
	 * @exception TimeoutException rest of data not received for too long time
	 * @exception UnknownCommandIdException PDU with unknown id was received
	 * @see #tryReceivePDUWithTimeout(Connection,PDU,long)
	 * @see #receiveAsync()
	 * @see #run()
	 * @see PDU#equals(Object)
	 */
	protected abstract PDU tryReceivePDU(Connection connection, PDU expectedPDU)
		throws UnknownCommandIdException, TimeoutException, PDUException, IOException;

	/**
	 * This is an implementation of <code>ProcessingThread</code>'s 
	 * <code>process</code> method, which is method called in loop from
	 * the <code>run</code> method.<br>
	 * This simply calls <code>receiveAsync</code>.
	 */
	public void process() {
		receiveAsync();
	}

	/**
	 * Calls <code>tryReceivePDUWithTimeout(Connection,PDU,long)</code> with
	 * timeout set by <code>setReceiveTimeout</code>.
	 *
	 * @param connection  the connection from which the PDU should be received
	 * @param expectedPDU the command id and sequence id of the received PDU
	 *                    should be equal to those of expectedPDU
	 * @return the received PDU if any or null if none received
	 *
	 * @exception IOException exception during communication
	 * @exception PDUException incorrect format of PDU
	 * @exception TimeoutException rest of data not received for too long time
	 * @exception UnknownCommandIdException PDU with unknown id was received
	 * @see #tryReceivePDUWithTimeout(Connection,PDU,long)
	 */
	final protected PDU tryReceivePDUWithTimeout(Connection connection, PDU expectedPDU)
		throws UnknownCommandIdException, TimeoutException, PDUException, IOException {
		return tryReceivePDUWithTimeout(connection, expectedPDU, getReceiveTimeout());
	}

	/**
	 * For specified time tries to receive a PDU from given connection by 
	 * calling method <code>tryReceivePDU</code>.
	 * The method <code>tryReceivePDU</code> must be implemented in the derived
	 * class.
	 * <p>
	 * The timeout can be either value > 0, then it means for
	 * how many milliseconds will be repeatedly tried to receive a PDU.
	 * If the timeout is = 0 then there is only one attempt to receive a PDU.
	 * If the timeout is equal to Data.RECEIVE_BLOCKING, then the this method
	 * tries receive a PDU until it is received.
	 *
	 * @param connection  the connection from which the PDU should be received
	 * @param expectedPDU the command id and sequence id of the received PDU
	 *                    should be equal to those of expectedPDU
	 * @param timeout     the timeout indication
	 * @return the received PDU if any or null if none received
	 * 
	 * @exception IOException exception during communication
	 * @exception PDUException incorrect format of PDU
	 * @exception TimeoutException rest of data not received for too long time
	 * @exception UnknownCommandIdException PDU with unknown id was received
	 * @see #tryReceivePDU(Connection,PDU)
	 * @see PDU#equals(Object)
	 */
	final protected PDU tryReceivePDUWithTimeout(Connection connection, PDU expectedPDU, long timeout)
		throws UnknownCommandIdException, TimeoutException, PDUException, IOException {
		debug.write(DRXTX, "receivePDU: Going to receive response.");
		long startTime = Data.getCurrentTime();
		PDU pdu = null;
		if (timeout == 0) {
			// with no timeout try just once
			pdu = tryReceivePDU(connection, expectedPDU);
		} else {
			// with timeout keep trying until get some or timeout expires
			while ((pdu == null) && canContinueReceiving(startTime, timeout)) {
				pdu = tryReceivePDU(connection, expectedPDU);
			}
		}
		if (pdu != null) {
			debug.write(DRXTX, "Got pdu " + pdu.debugString());
		}
		return pdu;
	}

	/**
	 * Elementary method receiving data from connection, trying to create
	 * PDU from them and buffering data in case the PDU
	 * isn't still complete. It has timeout checking for incomplete
	 * messages: if the message isn't received completly for certain time
	 * and no new data are received for this time, then exception is thrown
	 * as this could indicate communication problem.
	 *
	 * @param connection the connection to receive the data from
	 * @return either PDU, if complete received or null
	 *
	 * @exception IOException exception during communication
	 * @exception PDUException incorrect format of PDU
	 * @throws TimeoutException rest of data not received for too long time
	 * @throws UnknownCommandIdException PDU with unknown id was received
	 * @see Connection
	 * @see Unprocessed
	 */
	final protected PDU receivePDUFromConnection(Connection connection, Unprocessed unprocessed)
		throws UnknownCommandIdException, TimeoutException, PDUException, IOException {
		debug.write(DRXTXD2, "ReceiverBase.receivePDUFromConnection start");
		PDU pdu = null;
		ByteBuffer buffer;
		ByteBuffer unprocBuffer;
		
		try {
			// first check if there is something left from the last time
			if (unprocessed.getHasUnprocessed()) {
				unprocBuffer = unprocessed.getUnprocessed();
				debug.write(DRXTX, "have unprocessed " + unprocBuffer.length() + " bytes from previous try");
				pdu = tryGetUnprocessedPDU(unprocessed);
			}
			if (pdu == null) { // only if we didn't manage to get pdu from unproc
				buffer = connection.receive();
				unprocBuffer = unprocessed.getUnprocessed();
				// if received something now or have something from the last receive
				if (buffer.length() != 0) {
					unprocBuffer.appendBuffer(buffer);
					unprocessed.setLastTimeReceived();
					pdu = tryGetUnprocessedPDU(unprocessed);
				} else {
					debug.write(DRXTXD2, "no data received this time.");
					// check if it's not too long since we received any data
					long timeout = getReceiveTimeout();
					if ((unprocBuffer.length() > 0)
						&& ((unprocessed.getLastTimeReceived() + timeout) < Data.getCurrentTime())) {
						debug.write(DRXTX, "and it's been very long time.");
						unprocessed.reset();
						throw new TimeoutException(timeout, unprocessed.getExpected(), unprocBuffer.length());
					}
				}
			}
		}
		catch (UnknownCommandIdException e) {
			// paolo@bulksms.com: if we got an UnknownCommandIdException here, the
			// chances are excellent that some trailing garbage is hanging around in
			// the unprocessed buffer. Given that it's unlikely that it contained a
			// valid PDU, we don't rethrow the error - nothing to respond to with a
			// gnack. Was originally just checking this if the unprocessed buffer had
			// content, but that's not enough.
			event.write(e,"There is _probably_ garbage in the unprocessed buffer - flushing unprocessed buffer now.");
			unprocessed.reset();
		}

		debug.write(DRXTXD2, "ReceiverBase.receivePDUFromConnection finished");
		return pdu;
	}

	/**
	 * Tries to create a PDU from the buffer provided.
	 * Returns the PDU if successfull or null if not or an exception
	 * if the PDU is incorrect.
	 */
	private final PDU tryGetUnprocessedPDU(Unprocessed unprocessed) throws UnknownCommandIdException, PDUException {
		debug.write(DRXTX, "trying to create pdu from unprocessed buffer");
		PDU pdu = null;
		ByteBuffer unprocBuffer = unprocessed.getUnprocessed();
		try {
			pdu = PDU.createPDU(unprocBuffer);
			unprocessed.check();
			// Reset counter after successful createPDU (as per bug #2138444):
			messageIncompleteRetryCount = 0;
		} catch (HeaderIncompleteException e) {
			// the header wasn't received completly, we will try to
			// receive the rest next time
			debug.write(DRXTXD, "incomplete message header, will wait for the rest.");
			unprocessed.setHasUnprocessed(false); // as it's incomplete - wait for new data
			unprocessed.setExpected(Data.PDU_HEADER_SIZE);
		} catch (MessageIncompleteException e) {
			// paolo@bulksms.com - this number (5) is somewhat arbitrary. Too low
			// a figure could trigger a false positive with fast data rates,
			// busy servers and PDUs split across TCP packets.
			if (messageIncompleteRetryCount > 5) {
				messageIncompleteRetryCount = 0;
				event.write("Giving up on incomplete messages - probably garbage in unprocessed buffer. Flushing unprocessed buffer.");
				unprocessed.reset();
			}

			// the message wasn't received completly, less bytes than command
			// length has been received, will try to receive the rest next time
			debug.write(DRXTXD, "incomplete message, will wait for the rest.");
			unprocessed.setHasUnprocessed(false); // as it's incomplete - wait for new data
			unprocessed.setExpected(Data.PDU_HEADER_SIZE);
			messageIncompleteRetryCount++;
		} catch (UnknownCommandIdException e) {
			// message with invalid id was received, should send generic_nack
			debug.write(DRXTX, "unknown pdu, might remove from unprocessed buffer. CommandId=" + e.getCommandId());
			if (e.getCommandLength() <= unprocBuffer.length()) {
				// have already enough to remove
				try {
					unprocBuffer.removeBytes(e.getCommandLength());
				} catch (NotEnoughDataInByteBufferException e1) {
					// can't happen, we've checked it above
					throw new Error("Not enough data in buffer even if previously checked that there was enough.");
				}
				unprocessed.check();
				throw e; // caller will decide what to do
			}
			// paolo@bulksms.com: added this: see why in caller. Advantage: lets
			// us trap garbage PDUs that break things by being trapped in the
			// unprocessed buffer eternally. Disadvantage: if this was a valid
			// (well-formed) PDU with an unknown command id AND it was not fully
			// read into the buffer in one go, then:
			// 1) we will respond to the part _was_ already read (which we know
			// contains at least a header - see code in PDU) with an error, which
			// is fine.
			// 2) when we receive the second part of the incomplete PDU, it will
			// effectively seem to us to be an invalid PDU itself. It could be
			// processed as follows:
			//     - as an unknown command_id again, which is fine; or
			//     - as an incomplete message, which is a problem, because it will
			//       then have the subsequent PDU tacked to the end of it, and
			//       that PDU will then be discarded as well (and almost certainly
			//       discarded via an UnknownCommandIdException again).
			throw e;
		} catch (PDUException e) {
			// paolo@bulksms.com: safer to catch all other PDU exceptions and force
			// force a check() here - some exception in parsing should not be allowed
			// to leave ghost data in the Unprocessed buffer (even though this is now
			// less likely after improvements to PDU.createPDU()):
			unprocessed.check();
			throw e;
		}
		/* paolo@bulksms.com: concerned that this is too broad, and will
		   stop useful exceptions from being passed back up the call stack,
			so disabling for now:
		} catch (Exception e) {
			debug.write(DRXTX, "Exception catched: " + e.toString());
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);
			e.printStackTrace(printWriter);
			debug.write(DRXTX, stringWriter.toString());
		}
		*/
		if (pdu != null) {
			debug.write(DRXTX, "received complete pdu" + pdu.debugString());
			debug.write(DRXTX, "there is " + unprocBuffer.length() + " bytes left in unprocessed buffer");
		}
		// unprocessed.check();
		return pdu;
	}

	/**
	 * Sets the timeout for receiving the complete message.
	 * If no data are received for time longer then this timeout and there
	 * is still not completly received PDU in the internal buffer,
	 * <code>TimeoutException</code> is thrown.
	 *
	 * @param timeout the new timeout value
	 *
	 * @see #receivePDUFromConnection(Connection,Unprocessed)
	 * @see TimeoutException
	 */
	public void setReceiveTimeout(long timeout) {
		receiveTimeout = timeout;
	}

	/**
	 * Returns the current setting of the receiving timeout.
	 *
	 * @return the current timeout value
	 */
	public long getReceiveTimeout() {
		return receiveTimeout;
	}

	/**
	 * Depending on value of <code>timeout</code> and on <code>startTime</code>
	 * returns if it's still possible to continue in receiving of message.
	 * <code>timeout</code> can indicate either timeout in milliseconds
	 * (if > 0), or that there has to be only one attempt to receive
	 * a message (if = 0) or that the the receiving shuld continue until
	 * a PDU is received (if = Data.RECEIVE_BLOCKING).
	 *
	 * @param startTime when the receiving started
	 * @param timeout timeout indication
	 * @return if it's possible to continue receiving
	 */
	private boolean canContinueReceiving(long startTime, long timeout) {
		return timeout == Data.RECEIVE_BLOCKING ? true : Data.getCurrentTime() <= (startTime + timeout);
	}

}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.3  2004/09/04 09:34:50  paoloc
 * no message
 *
 * Revision 1.2  2004/09/04 09:00:27  paoloc
 * Various changes which deal better with invalid ((non-spec or garbage) data being received. Previously, such data could get a Receiver object stuck in a state in which it stops passing incoming data to the application.
 *
 * Revision 1.1  2003/07/23 00:28:39  sverkera
 * Imported
 *
 * 
 * Old changelog:
 * 13-07-01 ticp@logica.com start(), stop(), setReceiveTimeout(),
 * 						    getReceiveTimeout(), setTermException(),
 *						    getTermException() made not synchronized;
 *						    receive(long) & receive(PDU) made synchronized
 *						    so the receiver no longer locks up
 * 13-07-01 ticp@logica.com the call to receiveAsync() in run() now enclosed
 *						    in try-finally and the status RCV_FINISHED
 *						    is set in finally block so the finished status
 *						    is now reported correctly even in case of exception
 * 13-07-01 ticp@logica.com some debug lines corrected; some added
 * 08-08-01 ticp@logica.com added support for Session's asynchronous processing capability
 * 23-08-01 ticp@logica.com added async capability exhibited that if more than
 *						    one pdu is read from connection at once (in one
 *						    connection.receive(), the additional ones aren't
 *						    processed. this behaviour was corrected.
 * 23-08-01 ticp@logica.com added yield() to run() to give chance to other
 *						    threads
 * 26-09-01 ticp@logica.com debug code categorized to groups
 * 01-10-01 ticp@logica.com now derived from new ProcessingThread which implements
 *						    the thread related issues; this change allows to focus
 *						    only on the receiving related in the ReceiverBase
 *						    and on the thread management in the ProcessingThread;
 *						    function covered by ProcessingThread removed
 */
