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

import org.smpp.pdu.GenericNack;
import org.smpp.pdu.PDU;
import org.smpp.pdu.InvalidPDUException;
import org.smpp.pdu.PDUException;
import org.smpp.pdu.UnknownCommandIdException;
import org.smpp.util.Queue;
import org.smpp.util.Unprocessed;

/**
 * <code>Receiver</code> is class used for receiving PDUs from SMSC.
 * It can be used two ways: it has methods for synchronous (blocking)
 * receiving of PDUs and as it is derived from <code>ReceiverBase</code>
 * whic on turn is derived from <code>ProcessingThread</code> class,
 * it can also receive PDUs on background and puts them into a queue.
 * 
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.2 $
 * @see ReceiverBase
 * @see Connection
 * @see Session
 * @see Queue
 */

public class Receiver extends ReceiverBase {
	/**
	 * Name of the thread created when starting
	 * the <code>ProcessingThread</code>.
	 * @see org.smpp.util.ProcessingThread#start()
	 * @see org.smpp.util.ProcessingThread#generateIndexedThreadName()
	 */
	private static final String RECEIVER_THREAD_NAME = "Receiver";

	/**
	 * The correspondent transmitter for transmitting PDUs.
	 * It's used for sending of generic negative acknowledges, if necessary.
	 * It is passed to the receiver as a parameter during construction.
	 * 
	 * @see #receiveAsync()
	 */
	private Transmitter transmitter = null;

	/**
	 * The network connection which is used for receiving data.
	 * It is passed to the receiver as a parameter during construction.
	 */
	private Connection connection = null;

	/**
	 * The queue which holds the received PDUs.
	 * As the PDUs are received in asynchronnous manner, they are stored
	 * to a queue from which they can be get using method
	 * <code>receive</code>. PDUs are stored to the queue if and only if the
	 * <code>Receiver</code> is started as a separate thread using method
	 * <code>start</code>.
	 *
	 * @see #receive(long)
	 * @see #receive(PDU)
	 * @see #start()
	 * @see ReceiverBase#start()
	 */
	private Queue pduQueue = new Queue();

	/**
	 * This timeout specifies for how long will go the receiving into wait
	 * if the PDU (expected or any) isn't in the <code>pduQueue</code> yet.
	 * After that the queue is probed again (etc.) until receiving timeout 
	 * expires or the PDU is received.
	 *
	 * @see #tryReceivePDU(Connection,PDU)
	 */
	private long queueWaitTimeout = Data.QUEUE_TIMEOUT;

	/**
	 * Indication if the <code>Receiver</code> is receiving on background as an
	 * extra thread.
	 *
	 * @see #start()
	 * @see #tryReceivePDU(Connection,PDU)
	 */
	private boolean receiver = false;

	/**
	 * This object holds data received from connection which aren't complete
	 * PDU yet. If this situation occurs, it's likely that the data
	 * will be received the next time when another attempt
	 * to receive data from the connection occurs. Its used in
	 * <code>ReceiverBase</code>'s <code>receivePDUFromConnection</code>
	 * method.
	 *
	 * @see ReceiverBase#receivePDUFromConnection(Connection,Unprocessed)
	 */
	private Unprocessed unprocessed = new Unprocessed();

	/**
	 * If the receiving is asynchronous, <code>pduListener</code> must
	 * contain the callback object used for processing of PDUs received
	 * from the SMSC. <code>Receiver</code> after receiving a PDU passes
	 * the received PDU to apropriate member function of the processor.
	 * @see #asynchronous
	 * @see #setServerPDUEventListener(ServerPDUEventListener)
	 */
	private ServerPDUEventListener pduListener = null;

	/**
	 * Indicates that the sending of PDUs to the SMSC is asynchronous, i.e.
	 * the session doesn't wait for a response to the sent request as well as
	 * the <code>receive</code> functions will return null as all received 
	 * PDUs are passed to the <code>pduListener</code> object in
	 * the <code>receiver</code>
	 * @see #pduListener
	 * @see #setServerPDUEventListener(ServerPDUEventListener)
	 */
	private boolean asynchronous = false;

	/**
	 * If true then GenericNack messages will be sent automatically if message can't be parsed
	 * If false then the pdu will be submitted to listener for processing and submission to peer
	 */
	private boolean automaticNack = true;

	/**
	 * This constructor sets the connection to receive the messages from.
	 *
	 * @param connection the connection to use for receiving
	 * @see Connection
	 */
	public Receiver(Connection connection) {
		this.connection = connection;
	}

	
	/**
	 * This constructor sets the connection to receive the messages from
	 * and a transmitter for sending generic negative acknowledges
	 * if necessary.
	 *
	 * @param transmitter the transmitter to use for sending
	 *                    <code>GenericNack</code>
	 * @param connection  the connection to use for receiving and transmitting
	 * @see Transmitter
	 * @see Connection
	 * @see GenericNack
	 */
	public Receiver(Transmitter transmitter, Connection connection) {
		this.transmitter = transmitter;
		this.connection = connection;
	}

	/**
	 * Returns if the receiver receives PDUs on background as an extra thread.
	 *
	 * @see #receiver
	 */
	public boolean isReceiver() {
		return receiver;
	}

	/**
	 * Sets the event listener for asynchronous <code>Receiver</code>.
	 * In case there are unprocessed PDUs in the queue,
	 * they are removed from the queue and passed to the newly set listener.
	 */
	public synchronized void setServerPDUEventListener(ServerPDUEventListener pduListener) {
		this.pduListener = pduListener;
		this.asynchronous = pduListener != null;
		if (asynchronous) {
			// let's remove all pdu's from the queue as since now all
			// processing should be asynchronous -- it's not wise to
			// expect that the programmer will try AFTER setting the listener
			// to call receive() which when in sync mode removes the pdus from
			// the queue
			PDU pdu;
			int queueSize;
			synchronized (pduQueue) {
				queueSize = pduQueue.size();
				for (int i = 0; i < queueSize; i++) {
					pdu = (PDU) pduQueue.dequeue();
					process(pdu);
				}
			}
		}
	}

	/**
	 * Resets unprocessed data and starts receiving on the background.
	 *
	 * @see ReceiverBase#start()
	 */
	public void start() {
		debug.write(DRXTX, "Receiver starting");
		receiver = true;
		unprocessed.reset();
		super.start();
		debug.write(DRXTX, "Receiver started");
	}

	/**
	 * Stops receiving on the background.
	 *
	 * @see ReceiverBase#stop()
	 */
	public void stop() {
		debug.write(DRXTX, "Receiver stoping");
		if (isReceiver()) {
			super.stop();
			receiver = false;
		}
		debug.write(DRXTX, "Receiver stoped");
	}

	/**
	 * This method receives a PDU or returns PDU received on background,
	 * if there is any. It tries to receive a PDU for the specified timeout.
	 * If the receiver is asynchronous, then no attempt to receive a PDU
	 * and <code>null</code> is returned.
	 * The function calls are nested as follows:<br>
	 * <ul>
	 *   <li>No background receiver thread<br><code>
	 *       Receiver.receive(long)<br>
	 *       ReceiverBase.tryReceivePDUWithTimeout(Connection,PDU,long)<br>
	 *       Receiver.tryReceivePDU(Connection,PDU)<br>
	 *       ReceiverBase.receivePDUFromConnection<br>
	 *       Connection.receive()</code>
	 *   <li>Has background receiver thread<br><code>
	 *       Receiver.receive(long)<br>
	 *       ReceiverBase.tryReceivePDUWithTimeout(Connection,PDU,long)<br>
	 *       Receiver.tryReceivePDU(Connection,PDU)<br>
	 *       Queue.dequeue(PDU)</code><br>
	 *       and the ReceiverBase.run() function which actually receives the
	 *       PDUs and stores them to a queue looks as follows:<br><code>
	 *       ReceiverBase.run()<br>
	 *       Receiver.receiveAsync()<br>
	 *       ReceiverBase.receivePDUFromConnection<br>
	 *       Connection.receive()</code>
	 *
	 * @param timeout for how long is tried to receive a PDU
	 * @return the received PDU or null if none received for the spec. timeout
	 * 
	 * @exception IOException exception during communication
	 * @exception PDUException incorrect format of PDU
	 * @exception TimeoutException rest of PDU not received for too long time
	 * @exception UnknownCommandIdException PDU with unknown id was received
	 * @see ReceiverBase#tryReceivePDUWithTimeout(Connection,PDU,long)
	 */
	public synchronized PDU receive(long timeout)
		throws UnknownCommandIdException, TimeoutException, NotSynchronousException, PDUException, IOException {
		PDU pdu = null;
		if (!asynchronous) {
			pdu = tryReceivePDUWithTimeout(connection, null, timeout);
		}
		return pdu;
	}

	/**
	 * Called from session to receive a response for previously sent request.
	 *
	 * @param expectedPDU the template for expected PDU; the PDU returned
	 *                    must have the same sequence number
	 * @return the received PDU or null if none
	 * @see ReceiverBase#tryReceivePDUWithTimeout(Connection,PDU,long)
	 */
	public synchronized PDU receive(PDU expectedPDU)
		throws UnknownCommandIdException, TimeoutException, NotSynchronousException, PDUException, IOException {
		PDU pdu = null;
		if (!asynchronous) {
			pdu = tryReceivePDUWithTimeout(connection, expectedPDU);
		}
		return pdu;
	}

	/**
	 * This method tries to receive one PDU from the connection.
	 * It is called in cycle from <code>tryReceivePDUWithTimeout</code> until
	 * timeout expires. <code>tryReceivePDUWithTimeout</code> is called
	 * either from <code>receiveAsync</code> as asynchronous receive on
	 * background or from <code>receive</code> as synchronous receive.
	 * It either gets pdu from the queue or tries to receive it from connection
	 * using <code>receivePDUFromConnection</code> depending on
	 * the value of the <code>receiver</code> flag. The method
	 * checks if the actualy received PUD is equal to <code>expectedPDU</code>.
	 *
	 * @exception IOException exception during communication
	 * @exception PDUException incorrect format of PDU
	 * @exception TimeoutException rest of PDU not received for too long time
	 * @exception UnknownCommandIdException PDU with unknown id was received
	 * @see ReceiverBase#tryReceivePDUWithTimeout(Connection,PDU,long)
	 * @see #receiveAsync()
	 * @see ReceiverBase#run()
	 */
	protected PDU tryReceivePDU(Connection connection, PDU expectedPDU)
		throws UnknownCommandIdException, TimeoutException, PDUException, IOException {
		PDU pdu = null;
		if (receiver) {
			debug.write(DRXTXD2, "Is receiver/transciever => trying to get from queue.");
			synchronized (pduQueue) {
				if (expectedPDU == null) { // i.e. any pdu is acceptable
					if (!pduQueue.isEmpty()) {
						pdu = (PDU) pduQueue.dequeue();
					}
				} else {
					pdu = (PDU) pduQueue.dequeue(expectedPDU);
				}
				if (pdu == null) {
					try {
						pduQueue.wait(getQueueWaitTimeout());
					} catch (InterruptedException e) {
						// we don't care
						debug.write(DRXTX, "tryReceivePDU got interrupt waiting for queue");
					}
				}
			}
		} else {
			debug.write(DRXTX, "Is transmitter only => trying to receive from connection.");
			pdu = receivePDUFromConnection(connection, unprocessed);
			if (pdu != null) {
				if ((expectedPDU == null) || !pdu.equals(expectedPDU)) {
					debug.write(DRXTX, "This is not the pdu we expect, processing" + pdu.debugString());
					enqueue(pdu);
					pdu = null;
				}
			}
		}
		return pdu;
	}

	/**
	 * This method receives a PDU from connection and stores it into
	 * <code>pduQueue</code>. It's called from the <code>ReceiverBase</code>'s
	 * p<code>process</code> method which is called in loop from
	 * <code>ProcessingThread</code>'s <code>run</code> method.
	 * <p>
	 * If an exception occurs during receiving, depending on type
	 * of the exception this method either just reports the exception to
	 * debug & event objects or stops processing to indicate
	 * that it isn't able to process the exception. The function
	 * <code>setTermException</code> is then called with the caught exception.
	 * 
	 * @see ReceiverBase#run()
	 */
	protected void receiveAsync() {
		PDU pdu = null;
		try {
			debug.write(DRXTXD2, "Receiver.receiveAsync() going to receive pdu.");
			pdu = receivePDUFromConnection(connection, unprocessed);
			// we must catch every exception as this is thread running
			// on the background and we don't want the thread to be terminated
		} catch (InvalidPDUException e) {
			// thrown when enough data were received but further parsing
			// required more than indicated by CommandLength, i.e. pdu is
			// corrupted or further parsing didn't find terminating zero
			// of a c-string i.e. pdu is corrupted
			// must send generic nack anyway
			event.write(e, "Receiver.receiveAsync(): received PDU is invalid.");
			PDU expdu = e.getPDU();
			int seqNr = expdu == null ? 0 : expdu.getSequenceNumber();
			if(automaticNack) {
				sendGenericNack(Data.ESME_RINVMSGLEN, seqNr);
			} else {
				pdu = new GenericNack(Data.ESME_RINVMSGLEN, seqNr);
			}
		} catch (UnknownCommandIdException e) {
			// if received unknown pdu, we must send generic nack
			event.write(e, "Receiver.receiveAsync(): Unknown command id.");
			if(automaticNack) {
				sendGenericNack(Data.ESME_RINVCMDID, e.getSequenceNumber());
			} else {
				pdu = new GenericNack(Data.ESME_RINVCMDID, e.getSequenceNumber());
			}
		} catch (TimeoutException e) {
			// too long had unprocessed data
			debug.write(DRXTX, "Receiver.receiveAsync() too long had an uncomplete message.");
		} catch (PDUException e) {
			// something wrong with the PDU
			event.write(e, "Receiver.receiveAsync()");
			PDU expdu = e.getPDU();
			int seqNr = expdu == null ? 0 : expdu.getSequenceNumber();
			if(automaticNack) {
				sendGenericNack(e.getErrorCode(), seqNr);
			} else {
				pdu = new GenericNack(e.getErrorCode(), seqNr);
			}
		} catch (Exception e) {
			// don't know what happen, let's end the show
			event.write(e, "Receiver.receiveAsync()");
			stopProcessing(e);
		}
		if (pdu != null) {
			debug.write(DRXTX, "Receiver.receiveAsync(): PDU received, processing " + pdu.debugString());
			if (asynchronous) {
				process(pdu);
			} else {
				enqueue(pdu);
			}
		}
	}

	/**
	 * Passes the <code>pdu</code> to the <code>pduListener</code>.
	 *
	 * @param pdu the PDU to pass to the processor as an
	 *            <code>ServerPDUEvent</code>
	 * @see Queue
	 * @see ServerPDUEventListener
	 */
	private void process(PDU pdu) {
		debug.write(DRXTX, "receiver passing pdu to ServerPDUEventListener");
		if (pduListener != null) {
			ServerPDUEvent pduReceived = new ServerPDUEvent(this, connection, pdu);
			pduListener.handleEvent(pduReceived);
			debug.write(DRXTX, "ServerPDUEventListener received pdu");
		} else {
			debug.write(
				DRXTX,
				"async receiver doesn't have ServerPDUEventListener, " + "discarding " + pdu.debugString());
		}
	}

	/**
	 * Puts the <code>pdu</code> into the <code>pduQueue</code>.
	 *
	 * @param pdu the PDU to put into the queue
	 * @see Queue
	 */
	private void enqueue(PDU pdu) {
		debug.write(DRXTX, "receiver enqueuing pdu.");
		synchronized (pduQueue) {
			pduQueue.enqueue(pdu);
			pduQueue.notifyAll();
		}
		// HNK -- If you just received unbind_resp, don't block on
		// socket in TCPIPConnection.receive(), call stopProcessing()
		if (Data.UNBIND_RESP == pdu.getCommandId()) {
			stopProcessing(null);
		}
	}

	/**
	 * Sends <code>GenericNack</code> PDU via transmitter if there is one.
	 * The <code>GenericNack</code> is sent in case that the message is
	 * corrupted or has unknown command id. If the sending of
	 * <code>GenericNack</code> fails, this method calls
	 * <code>stopProcessing</code> and thus stops the receiving
	 * thread.
	 *
	 * @param commandStatus the error code
	 * @param sequenceNumber the sequence number of received wrong PDU
	 * @see GenericNack
	 * @see Transmitter
	 */
	private void sendGenericNack(int commandStatus, int sequenceNumber) {
		if (transmitter != null) {
			try {
				GenericNack gnack = new GenericNack(commandStatus, sequenceNumber);
				transmitter.send(gnack);
			} catch (IOException gnacke) {
				event.write(gnacke, "Receiver.run(): IOException sending generic_nack.");
			} catch (Exception gnacke) {
				event.write(gnacke, "Receiver.run(): an exception sending generic_nack.");
				stopProcessing(gnacke);
			}
		}
	}

	/**
	 * Sets queue waiting timeout.
	 *
	 * @param timeout the new queue timeout
	 * @see #queueWaitTimeout
	 */
	public void setQueueWaitTimeout(long timeout) {
		queueWaitTimeout = timeout;
	}

	/**
	 * Returns current queue waiting timeout.
	 *
	 * @return the current queue timeout
	 * @see #queueWaitTimeout
	 */
	public long getQueueWaitTimeout() {
		return queueWaitTimeout;
	}

	// ProcessingThread's getThreadName override
	public String getThreadName() {
		return RECEIVER_THREAD_NAME;
	}


	public void setAutomaticNack(boolean automaticNack) {
		this.automaticNack = automaticNack;
	}


	public boolean isAutomaticNack() {
		return automaticNack;
	}

}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.1  2003/07/23 00:28:39  sverkera
 * Imported
 *
 * 
 * Old changelog:
 * 13-07-01 ticp@logica.com start(), stop(), setQueueWaitTimeout() &
 *						    getQueueWaitTimeout() made not synchronized;
 *						    receive(long) & receive(PDU) made synchronized
 *						    so the receiver no longer locks up
 * 13-07-01 ticp@logica.com bug fixed in tryReceivePDU which caused that the PDUs
 *						    were never removed from the queue - now dequeue(expected)
 *						    is now used instead of find(expected)
 * 13-07-01 ticp@logica.com loads of debug lines corrected; some added
 * 08-08-01 ticp@logica.com added support for Session's asynchronous processing capability
 * 26-09-01 ticp@logica.com debug code categorized to groups
 * 01-10-01 ticp@logica.com added function getThreadName for ProcessingThread
 *						    thread name initialisation.
 * 02-10-01 ticp@logica.com instead of importing full packages only the used
 *						    classes are iported
 */
