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
import java.util.Hashtable;

import org.smpp.util.*;
import org.smpp.pdu.*;

/**
 * Class <code>Session</code> provides all methods necessary for communication
 * with SMSC using SMPP protocol, i.e. methods for sending PDUs as defined
 * in SMPP specification as well as receiving responses for sent PDUs
 * and waiting for PDUs whose sending was initiated by SMSC.<br>
 * Instance of <code>Session</code> represents one connection of ESME
 * to a SMSC. Multiple connections can be established using
 * multiple <code>Sessions</code>.
 * <p>
 * <code>Session</code> uses <code>Connection</code> object which is
 * instantiated outside of the <code>Session</code>. This way is
 * the <code>Session</code> made independent on the communication protocol
 * and <code>Session</code>'s code isn't populated by protocol dependent
 * initialisation.
 * <p>
 * Code example of binding, sending one message and unbinding:
 * <br><blockquote><pre>
 *   Connection conn = new TCPIPConnection("123.123.123.123", 6543);
 *   Session session = new Session(conn);
 *   BindRequest breq = new BindTransmitter();
 *   breq.setSystemId("MYNAME");
 *   breq.setPassword("my_pswdx");
 *   Response resp = session.bind(breq);
 *   if (resp.getCommandStatus() == Data.ESME_ROK) {
 *      SubmitSM msg = new SubmitSM();
 *      msg.setSourceAddr("3538998765432");
 *      msg.setDestAddr("3538619283746");
 *      msg.setShortMessage("Hello, world!");
 *      resp = session.submit(msg);
 *      if (resp.getCommandStatus() == Data.ESME_ROK) {
 *         System.out.println("Message submitted. Status=" + resp.getCommandStatus());
 *      } else {
 *         System.out.println("Message submission failed. Status=" + resp.getCommandStatus());
 *      }
 *      session.unbind();
 *   } else {
 *      System.out.println("Couldn't bind. Status=" + resp.getCommandStatus());
 *   }
 * </pre></blockquote>
 * Note that the cycle bind - send PDU's - unbind can be called
 * several times for once created session.
 * <p>
 * Particular methods for sending PDUs to SMSC return responses to the sent
 * PDUs. They return null if no response is received in time specified
 * by the receive timeout in receiver. This means that the methods wait
 * for response corresponding to the request.
 * The corresponding response is recognized using sequence number of the sent
 * PDU and a corresponding response command id.<br>
 * The session can work in assynchronous manner, i.e. it doesn't wait
 * for response for the sent request, instead all responses are handled
 * by instance of callback class <code>ServerPDUEventListener</code>
 * whenever they are received.<br>
 * The <code>Session</code> class checks if operations invoked are valid in
 * the current state of the session. If not, then such operation throws
 * <code>WrongSessionStateException</code> expcetion. For example it's incorrect
 * to try to submit a message if the session is bound as receiver. The checking
 * if the operation is valid in the current state of session is turned off
 * by default.
 * 
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.4 $
 * @see Connection
 * @see Transmitter
 * @see Receiver
 * @see ServerPDUEventListener
 */
public class Session extends SmppObject {
	/**
	 * Status of the connection. It's set by <code>open</code> method,
	 * which is called from <code>bind</code> method.
	 * @see #open()
	 * @see #bind(BindRequest)
	 */
	private boolean opened = false;

	/**
	 * Status of bounding. It's set by <code>bind</code> methdod if
	 * the binding is successfull.
	 * @see #bind(BindRequest)
	 */
	private boolean bound = false;

	/** Special state for actions which are never allowed for the session. */
	public static final int STATE_NOT_ALLOWED = 0x00; // 00000b

	/** The connection is closed (or not opened yet) and session isn't bound. */
	public static final int STATE_CLOSED = 0x01; // 00001b

	/** The connection is opened, but the session isn't bound. */
	public static final int STATE_OPENED = 0x02; // 00010b

	/** The session is bound as transmitter. */
	public static final int STATE_TRANSMITTER = 0x04; // 00100b

	/** The session is bound as receiver. */
	public static final int STATE_RECEIVER = 0x08; // 01000b

	/** The session is bound as transceiver. */
	public static final int STATE_TRANSCEIVER = 0x10; // 10000b

	/** Special state for actions which are always allowed (e.g. generic_nack.) */
		public static final int STATE_ALWAYS = // 11111b
	STATE_OPENED | STATE_TRANSMITTER | STATE_RECEIVER | STATE_TRANSCEIVER;

	/**
	 * Table with command id's and session states in which they can be used.
	 * For session on ESME side.
	 * @see #initialiseStateMatrix()
	 */
	private static Hashtable<Integer, Integer> esmeStateMatrix;

	/**
	 * Table with command id's and session states in which they can be used.
	 * For session on MC side. Note that even if this is from the MC side
	 * the bind states are still entered from the EMSE point of veiw.
	 * E.g. for deliver_sm this table contains states receiver and transceiver,
	 * which means that MC can send PDU with deliver_sm command id only in case
	 * the ESME is bound as receiver or transceiver.<br>
	 * <emp>Note:</emp> The MC state matrix is not completly supported by
	 * the current implementation of session, so it's currently altered
	 * to reflect the impplementation.
	 * @see #initialiseStateMatrix()
	 */
	private static Hashtable<Integer, Integer> mcStateMatrix;

	/**
	 * If PDU with unknown command id is probed this says if this PDU is
	 * allowed or not. Generaly it's not good idea to allow unknown PDUs, but
	 * for backward compatiblity we leave it set to false.
	 */
	private boolean disallowUnknownPDU = false;

	/**
	 * The current state of this Session.
	 * @see #esmeStateMatrix
	 * @see #mcStateMatrix
	 * @see #setState(int)
	 * @see #getState()
	 */
	private int state = STATE_CLOSED;

	/**
	 * If the checking of states is active. It's generaly good idea to leave it
	 * set to true, it'll save you communication bandwidth (MC should reject your
	 * PDU if you are in wrong state) and it'll discover bad logic in your application.
	 * For backward compatibility we set that the state is NOT checked.
	 * @see #enableStateChecking()
	 * @see #disableStateChecking()
	 */
	private boolean stateChecking = false;

	/**
	 * Indicates that the session is session for ESME 'point of view.'
	 * @see #type
	 */
	public static final int TYPE_ESME = 1;

	/**
	 * Indicates that the session is session for MC 'point of view.'
	 * MC is Message Center and it is a term for generic Message Centre such as SMSC.
	 * @see #type
	 */
	public static final int TYPE_MC = 2;

	/**
	 * If this session is ESME session or if it is used in MC (or simulator
	 * or routing entity, simply the other side of normal ESME).
	 * Normally this would be equal to TYPE_ESME as mostly this library will be used
	 * for developing ESME applications and not MCs. If you don't understand
	 * what is this variable for, just leave it as it is.
	 * @see #setType(int)
	 * @see #getType()
	 */
	private int type = TYPE_ESME;

	/**
	 * The connection object. It's created outside of the <code>Session</code>
	 * class and passed as a parameter during the <code>Session</code>
	 * creation. It's then passed to <code>Transmitter</code> and
	 * <code>Receiver</code>.
	 * @see Connection
	 * @see TCPIPConnection
	 * @see Transmitter
	 * @see Receiver
	 */
	private Connection connection;

	/**
	 * Object used for transmitting of PDUs over connection.
	 * @see Transmitter
	 */
	private Transmitter transmitter;

	/**
	 * Object used for receiving of PDU from connection.
	 * @see Receiver
	 */
	private Receiver receiver;

	/**
	 * If the receiving is asynchronous, <code>pduListener</code> must
	 * contain the callback object used for processing of PDUs received
	 * from the SMSC. <code>Receiver</code> after receiving a PDU passes
	 * the received PDU to apropriate member function of the processor.
	 * @see #asynchronous
	 * @see #setServerPDUEventListener(ServerPDUEventListener)
	 * @see #bind(BindRequest)
	 * @see Receiver
	 */
	private ServerPDUEventListener pduListener = null;

	/**
	 * Indicates that the sending of PDUs to the SMSC is asynchronous, i.e.
	 * the session doesn't wait for a response to the sent request as well as
	 * the <code>receive</code> functions will return null as all received
	 * PDUs are passed to the <code>pduListener</code> object in
	 * the <code>receiver</code>.
	 * @see #pduListener
	 * @see #setServerPDUEventListener(ServerPDUEventListener)
	 * @see #bind(BindRequest)
	 * @see Receiver
	 */
	private boolean asynchronous = false;

	/**
	 * Default constructor made protected as it's not desirable to
	 * allow creation of <code>Session</code> without providing 
	 * <code>Connection</code>.
	 */
	protected Session() {
	}

	/**
	 * Creates <code>Session</code> which uses provided <code>Connection</code>.
	 * In most cases the <code>connection</code> parameter will be an instance
	 * of <code>TCPIPConnection</code> class.
	 *
	 * @param   connection   connection used for transmitting and receiving
	 *                       the data
	 */
	public Session(Connection connection) {
		this.connection = connection;
	}

	/**
	 * Opens the connection for communication.
	 * Sets indication that the connection is opened.
	 *
	 * @exception IOException exception during communication
	 */
	public void open() throws IOException, WrongSessionStateException {
		checkState(STATE_CLOSED);
		if (!opened) {
			connection.open();
			opened = true;
			setState(STATE_OPENED);
		}
	}

	/**
	 * Closes the connection for communication.
	 * Sets indication that the connection is not opened.
	 *
	 * @exception IOException exception during communication
	 */
	public void close() throws IOException, WrongSessionStateException {
		checkState(STATE_OPENED);
		if (connection.isOpened()) {
			connection.close();
			opened = false;
			setState(STATE_CLOSED);
		}
	}

	/**
	 * Returns of the connection is opened.
	 * @return current status of connection
	 */
	public boolean isOpened() {
		return opened && connection.isOpened();
	}

	/**
	 * Returns if the session is bound to SMSC.
	 * @return current status of bound
	 */
	public boolean isBound() {
		return bound;
	}

	/**
	 * Sets the listener which is passed all PDUs received by the
	 * <code>Receiver</code> from the SMSC.
	 * Note that this method implicitly sets asynchronous type of
	 * receiving.
	 * @param pduListener the listener which processes the received PDUs
	 */
	private void setServerPDUEventListener(ServerPDUEventListener pduListener) {
		this.pduListener = pduListener;
		receiver.setServerPDUEventListener(pduListener);
		asynchronous = pduListener != null;
	}

	/**
	 * Returns the current <code>ServerPDUEventListener</code> set for
	 * this session.
	 */
	private ServerPDUEventListener getServerPDUEventListener() {
		return pduListener;
	}

	/**
	 * Sets the type of the session. The type can be either ESME or MC viewed
	 * from the side where the instance of the <code>Session</code> is used.
	 * Set the type of the session before opening the session.
	 * @param type the new type of the session
	 * @see #getType()
	 * @see #TYPE_ESME
	 * @see #TYPE_MC
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * Returns the type of the session.
	 * @return the current type of the session.
	 * @see #setType(int)
	 * @see #TYPE_ESME
	 * @see #TYPE_MC
	 */
	public int getType() {
		return type;
	}

	/**
	 * Opens connection and binds to SMSC using the bind method provided
	 * as bindReq parameter. Binds synchronously as no server pdu listener
	 * is provided. On details about bind see
	 * <a href="#bind(BindRequest,ServerPDUEventListener)">bind(BindRequest,ServerPDUEventListener)</a>.
	 * @see #bind(BindRequest,ServerPDUEventListener)
	 */
	final public BindResponse bind(BindRequest bindReq)
		throws ValueNotSetException, TimeoutException, PDUException, IOException, WrongSessionStateException {
		checkState(bindReq);
		return bind(bindReq, null);
	}

	/**
	 * Opens connection and binds to SMSC using the bind method provided
	 * as <code>bindReq</code> parameter. For transmittong PDUs creates instance
	 * of <code>Transmitter</code> class, for receiving PDUs from SMSC
	 * creates instance of <code>Receiver</code> class.<br>
	 * Receiver starts an extra thread
	 * which receives PDUs from connection and puts them into a queue
	 * (if synchronous) or processes them using <code>pduListener</code>.
	 * If the bind to SMSC isn't successfull, the thread is stopped and
	 * the connection is closed.<br>
	 * If the variable <code>pduListener</code> is not <code>null</code>,
	 * the session is asynchronous, otherwise the session is synchronous.
	 * Note that the <code>bind</code> method is <emp>always</emp> synchronous,
	 * regardless of the value of the <code>pduListener</code> variable.
	 * <p>
	 * See "SMPP Protocol Specification 3.4, 4.1 BIND Operation."
	 *
	 * @param   bindReq   the bind request
	 *
	 * @exception IOException exception during communication
	 * @exception PDUException incorrect format of PDU
	 * @exception TimeoutException rest of data not received for too long time
	 * @exception ValueNotSetException optional param not set but requested
	 * @see BindRequest
	 * @see BindResponse
	 * @see BindTransmitter
	 * @see BindReceiver
	 * @see BindTransciever
	 */
	final public BindResponse bind(BindRequest bindReq, ServerPDUEventListener pduListener)
		throws ValueNotSetException, TimeoutException, PDUException, IOException, WrongSessionStateException {
		checkState(bindReq);
		if (bound) {
			// exception? yes, checked above
			return null;
		} else {
			open();
			transmitter = new Transmitter(connection);
			receiver = new Receiver(transmitter, connection);
			BindResponse bindResp = (BindResponse) send(bindReq, false);
			bound = ((bindResp != null) && (bindResp.getCommandStatus() == Data.ESME_ROK));
			if (!bound) {
				//receiver.stop();
				close();
			} else {
				receiver.start();
				if (bindReq.isTransmitter()) {
					if (bindReq.isReceiver()) {
						setState(STATE_TRANSCEIVER);
					} else {
						setState(STATE_TRANSMITTER);
					}
				} else {
					setState(STATE_RECEIVER);
				}
				setServerPDUEventListener(pduListener);
			}
			return bindResp;
		}
	}

	/**
	 * Sends an outbind PDU over the connection. For sending outbind
	 * PDU the session must not be bound to smsc (!), this is the 
	 * logic of outbind operation. Don't mess it up with receiving
	 * outbind PDUs from SMSC! Also this operation is only allowed when your
	 * session acts as SMSC (MC) session. ESMEs can't send outbinds.
	 * <p>
	 * See SMPP Protocol Specification 3.4, 4.1.7 OUTBIND Operation.
	 * 
	 * @param request The outbind PDU.
	 * 
	 * @exception IOException exception during communication
	 * @exception PDUException incorrect format of PDU
	 * @exception TimeoutException rest of data not received for too long time
	 * @exception ValueNotSetException optional param not set but requested
	 *            (this exception doesn't apply for outbind)
	 * @see Outbind
	 * @see OutbindReceiver
	 */
	final public void outbind(Outbind request)
		throws ValueNotSetException, TimeoutException, PDUException, IOException, WrongSessionStateException {
		checkState(request);
		send(request);
	}

	/**
	 * Stops <code>receiver</code> (if applicable), unbinds from SMSC
	 * by sending <code>Unbind</code> PDU, waits for <code>UnbindResp</code>
	 * and then closes the connection.<br>
	 * Note that the <code>unbind</code> method is <emp>always</emp> synchronous
	 * or asynchronous depending on the value of the <code>asynchronous</code>
	 * variable.
	 * <p>
	 * See "SMPP Protocol Specification 3.4, 4.2 UNBIND Operation."
	 *
	 * @return response PDU if successfully unbound, i.e. if still not bound
	 * after call to the method; null if unbind was not successful, i.e.
	 * no response has been received for unbind PDU
	 *
	 * @exception IOException exception during communication
	 * @exception PDUException incorrect format of PDU
	 * @exception TimeoutException rest of data not received for too long time
	 * @exception ValueNotSetException optional param not set but requested
	 *            (this exception doesn't apply for unbind)
	 * @see Unbind
	 * @see UnbindResp
	 */
	final public UnbindResp unbind()
		throws ValueNotSetException, TimeoutException, PDUException, IOException, WrongSessionStateException {
		UnbindResp unbindResp = null;
		if (bound) {
			Unbind unbindReq = new Unbind();
			checkState(unbindReq);
			ServerPDUEventListener origListener = null;
			if (asynchronous) {
				// we must assign the number now as we'll start waiting for
				// the response before we actually send the request
				unbindReq.assignSequenceNumber();
				origListener = getServerPDUEventListener();
				UnbindServerPDUEventListener unbindListener =
					new UnbindServerPDUEventListener(this, origListener, unbindReq);
				setServerPDUEventListener(unbindListener);
				synchronized (unbindListener) {
					send(unbindReq);
					try {
						unbindListener.wait(receiver.getReceiveTimeout());
						unbindResp = unbindListener.getUnbindResp();
					} catch (InterruptedException e) {
						// unbind reponse wasn't received in time
					}
				}
			} else {
				debug.write(DSESS, "going to unbound sync session");
				unbindResp = (UnbindResp) send(unbindReq);
			}
			bound = (unbindResp == null);
			if (!bound) {
				setState(STATE_OPENED);
				receiver.stop();
				receiver = null;
				transmitter = null;
				close();
			} else {
				// restore the listener - unbind unsuccessfull
				debug.write("Unbind unsuccessfull, restoring listener");
				setServerPDUEventListener(origListener);
			}
		}
		return unbindResp;
	}

	/**
	 * Sends GenericNack PDU provided as parameter.
	 * <p>
	 * See "SMPP Protocol Specification 3.4, 4.3 GENERIC_NACK Operation."
	 * 
	 * @param response The generic nack PDU.
	 * 
	 * @exception IOException exception during communication
	 * @exception TimeoutException rest of data not received for too long time
	 * @exception ValueNotSetException optional param not set but requested
	 *            (this exception doesn't apply for genericNack)
	 * @see GenericNack
	 */
	final public void genericNack(GenericNack response)
		throws ValueNotSetException, TimeoutException, IOException, WrongSessionStateException {
		checkState(response);
		try {
			respond(response);
		} catch (WrongSessionStateException e) {
			debug.write("strange, generic nack thrown " + e);
			debug.write("this shouldn't happend");
			event.write(e, "Unexpected exeption caught");
		}
	}

	/**
	 * Creates and sends <code>GenericNack</code> PDU with command status
	 * and sequence number provided as parameters.
	 * <p>
	 * See "SMPP Protocol Specification 3.4, 4.3 GENERIC_NACK Operation."
	 *
	 * @param commandStatus the code of error to be reported
	 * @param sequenceNumber the sequence number of the PDU the GenericNack
	 *                       is related to
	 * @exception IOException exception during communication
	 * @exception TimeoutException rest of data not received for too long time
	 * @exception ValueNotSetException optional param not set but requested
	 *            (this exception doesn't apply for genericNack)
	 * @see GenericNack
	 */
	final public void genericNack(int commandStatus, int sequenceNumber)
		throws ValueNotSetException, TimeoutException, IOException, WrongSessionStateException {
		GenericNack gnack = new GenericNack(commandStatus, sequenceNumber);
		checkState(gnack);
		genericNack(gnack);
	}

	/**
	 * Submits provided <code>SubmitSM</code> PDU to SMSC and returns response
	 * to the submission.
	 * <p>
	 * See "SMPP Protocol Specification 3.4, 4.4 SUBMIT_SM Operation."
	 *
	 * @param   request   the pdu to be submitted
	 * @return            correspondent response pdu
	 *
	 * @exception IOException exception during communication
	 * @exception PDUException incorrect format of PDU
	 * @exception TimeoutException rest of data not received for too long time
	 * @exception ValueNotSetException optional param not set but requested
	 * @see SubmitSM
	 * @see SubmitSMResp
	 */
	final public SubmitSMResp submit(SubmitSM request)
		throws ValueNotSetException, TimeoutException, PDUException, IOException, WrongSessionStateException {
		checkState(request);
		return (SubmitSMResp) send(request);
	}

	/**
	 * Submits provided <code>SubmitMultiSM</code> PDU to SMSC and returns
	 * response to the submission.
	 * <p>
	 * See "SMPP Protocol Specification 3.4, 4.5 SUBMIT_MULTI Operation."
	 *
	 * @param   request   the submit multi pdu to be submitted
	 * @return            correspondent response pdu
	 *
	 * @exception IOException exception during communication
	 * @exception PDUException incorrect format of PDU
	 * @exception TimeoutException rest of data not received for too long time
	 * @exception ValueNotSetException optional param not set but requested
	 * @see SubmitMultiSM
	 * @see SubmitMultiSMResp
	 */
	final public SubmitMultiSMResp submitMulti(SubmitMultiSM request)
		throws ValueNotSetException, TimeoutException, PDUException, IOException, WrongSessionStateException {
		checkState(request);
		return (SubmitMultiSMResp) send(request);
	}

	/**
	 * Submits provided <code>DeliverSM</code> PDU to SMSC and returns
	 * response to the submission.
	 * <p>
	 * See "SMPP Protocol Specification 3.4, 4.6 DELIVER_SM Operation."
	 *
	 * @param   request   the deliver pdu to be submitted
	 * @return            correspondent response pdu
	 *
	 * @exception IOException exception during communication
	 * @exception PDUException incorrect format of PDU
	 * @exception TimeoutException rest of data not received for too long time
	 * @exception ValueNotSetException optional param not set but requested
	 * @see DeliverSM
	 * @see DeliverSMResp
	 */
	final public DeliverSMResp deliver(DeliverSM request)
		throws ValueNotSetException, TimeoutException, PDUException, IOException, WrongSessionStateException {
		checkState(request);
		return (DeliverSMResp) send(request);
	}

	/**
	 * Submits provided <code>DataSM</code> PDU to SMSC and returns
	 * response to the submission.
	 * <p>
	 * See "SMPP Protocol Specification 3.4, 4.7 DATA_SM Operation."
	 *
	 * @param   request   the data pdu to be submitted
	 * @return            correspondent response pdu
	 *
	 * @exception IOException exception during communication
	 * @exception PDUException incorrect format of PDU
	 * @exception TimeoutException rest of data not received for too long time
	 * @exception ValueNotSetException optional param not set but requested
	 * @see DataSM
	 * @see DataSMResp
	 */
	final public DataSMResp data(DataSM request)
		throws ValueNotSetException, TimeoutException, PDUException, IOException, WrongSessionStateException {
		checkState(request);
		return (DataSMResp) send(request);
	}

	/**
	 * Queries status of previous submission by sending the
	 * <code>QuerySM</code> PDU to SMSC; returns the query response.
	 * <p>
	 * See "SMPP Protocol Specification 3.4, 4.8 QUERY_SM Operation."
	 *
	 * @param   request   the status query pdu to be sent
	 * @return            correspondent response pdu
	 *
	 * @exception IOException exception during communication
	 * @exception PDUException incorrect format of PDU
	 * @exception TimeoutException rest of data not received for too long time
	 * @exception ValueNotSetException optional param not set but requested
	 * @see QuerySM
	 * @see QuerySMResp
	 */
	final public QuerySMResp query(QuerySM request)
		throws ValueNotSetException, TimeoutException, PDUException, IOException, WrongSessionStateException {
		checkState(request);
		return (QuerySMResp) send(request);
	}

	/**
	 * Cancels previously submitted message by sending <code>CancelSM</code> PDU
	 * to SMSC; returns response to the cancel PDU.
	 * <p>
	 * See "SMPP Protocol Specification 3.4, 4.9 CANCEL_SM Operation."
	 *
	 * @param   request   the cancel pdu to be sent
	 * @return            correspondent response pdu
	 *
	 * @exception IOException exception during communication
	 * @exception PDUException incorrect format of PDU
	 * @exception TimeoutException rest of data not received for too long time
	 * @exception ValueNotSetException optional param not set but requested
	 * @see CancelSM
	 * @see CancelSMResp
	 */
	final public CancelSMResp cancel(CancelSM request)
		throws ValueNotSetException, TimeoutException, PDUException, IOException, WrongSessionStateException {
		checkState(request);
		return (CancelSMResp) send(request);
	}

	/**
	 * Replaces previously submitted message by sending <code>ReplaceSM</code>
	 * PDU to SMSC and returns response to the replace.
	 * <p>
	 * See "SMPP Protocol Specification 3.4, 4.10 REPLACE_SM Operation."
	 *
	 * @param   request   the replace pdu to be sent
	 * @return            correspondent response pdu
	 *
	 * @exception IOException exception during communication
	 * @exception PDUException incorrect format of PDU
	 * @exception TimeoutException rest of data not received for too long time
	 * @exception ValueNotSetException optional param not set but requested
	 * @see ReplaceSM
	 * @see ReplaceSMResp
	 */
	final public ReplaceSMResp replace(ReplaceSM request)
		throws ValueNotSetException, TimeoutException, PDUException, IOException, WrongSessionStateException {
		checkState(request);
		return (ReplaceSMResp) send(request);
	}

	/**
	 * Checks the status of connection between ESME and SMSC by sending
	 * <code>EnquireLink</code> PDU to SMSC; returns response 
	 * to the enquiry.
	 * <p>
	 * See "SMPP Protocol Specification 3.4, 4.11 ENQUIRE_LINK Operation."
	 *
	 * @param   request   the enquiry pdu to be submitted
	 * @return            correspondent response pdu
	 *
	 * @exception IOException exception during communication
	 * @exception PDUException incorrect format of PDU
	 * @exception TimeoutException rest of data not received for too long time
	 * @exception ValueNotSetException optional param not set but requested
	 * @see EnquireLink
	 * @see EnquireLinkResp
	 */
	final public EnquireLinkResp enquireLink(EnquireLink request)
		throws ValueNotSetException, TimeoutException, PDUException, IOException, WrongSessionStateException {
		checkState(request);
		return (EnquireLinkResp) send(request);
	}

	/**
	 * Simplified veriosn of <a href="#enquireLink(EnquireLink)">enquireLink</a>.
	 * As the <code>EnquireLink</code> PDU doesn't contain any parameters
	 * axcept of header, there is might be no need to provide the PDU as
	 * a parameter to the <code>enquireLink</code> method.<br>
	 * This method creates new <code>EnquireLink</code> object
	 * and sends it to SMSC; returns response to the enquiry.
	 * <p>
	 * See "SMPP Protocol Specification 3.4, 4.11 ENQUIRE_LINK Operation."
	 *
	 * @return            correspondent response pdu
	 *
	 * @exception IOException exception during communication
	 * @exception PDUException incorrect format of PDU
	 * @exception TimeoutException rest of data not received for too long time
	 * @exception ValueNotSetException optional param not set but requested
	 * @see EnquireLink
	 * @see EnquireLinkResp
	 */
	final public EnquireLinkResp enquireLink()
		throws ValueNotSetException, TimeoutException, PDUException, IOException, WrongSessionStateException {
		EnquireLink request = new EnquireLink();
		checkState(request);
		return enquireLink(request);
	}

	/**
	 * Submits provided <code>SubmitSM</code> PDU to SMSC and returns response to the submission.
	 * <p>
	 * See "SMPP Protocol Specification 3.4, 4.12 ALERT_NOTIFICATION Operation."
	 *
	 * @param   request   the pdu to be submitted
	 *
	 * @exception IOException exception during communication
	 * @exception PDUException incorrect format of PDU
	 * @exception TimeoutException rest of data not received for too long time
	 * @exception ValueNotSetException optional param not set but requested
	 * @see AlertNotification
	 */
	final public void alertNotification(AlertNotification request)
		throws ValueNotSetException, TimeoutException, PDUException, IOException, WrongSessionStateException {
		checkState(request);
		send(request);
	}

	/**
	 * Returns a PDU received from SMSC. This is blocking receive, caller
	 * will wait until a PDU will be received.<br>
	 * Note that this method can be called only when bound as receiver
	 * or transciever.
	 *
	 * @return received pdu
	 * 
	 * @exception IOException exception during communication
	 * @exception NotSynchronousException receive called in asynchronous mode
	 * @exception PDUException incorrect format of PDU
	 * @exception TimeoutException rest of data not received for too long time
	 * @exception UnknownCommandIdException PDU with unknown id was received
	 * @see Receiver
	 * @see ReceiverBase
	 */
	final public PDU receive()
		throws UnknownCommandIdException, TimeoutException, NotSynchronousException, PDUException, IOException {
		if (!asynchronous) {
			return receive(Data.RECEIVE_BLOCKING);
		} else {
			throw new NotSynchronousException(this);
		}
	}

	/**
	 * Returns a PDU received from SMSC. This receive will wait for
	 * maximum <code>timeout</code> time for a PDU; if there is
	 * no PDU received in the specified time, the function returns null.<br>
	 * Note that this method can be called only when bound as receiver
	 * or transciever.
	 *
	 * @return received pdu or null if none received
	 * 
	 * @exception IOException exception during communication
	 * @exception NotSynchronousException receive called in asynchronous mode
	 * @exception PDUException incorrect format of PDU
	 * @exception TimeoutException rest of data not received for too long time
	 * @exception UnknownCommandIdException PDU with unknown id was received
	 * @see Receiver
	 * @see ReceiverBase
	 */
	final public PDU receive(long timeout)
		throws UnknownCommandIdException, TimeoutException, NotSynchronousException, PDUException, IOException {
		PDU pdu = null;
		if (receiver.isReceiver()) {
			if (!asynchronous) {
				pdu = receiver.receive(timeout);
			} else {
				throw new NotSynchronousException(this);
			}
		} else {
			// throw?
		}
		return pdu;
	}

	/**
	 * Sends a response PDU. Use for sending responses for PDUs send
	 * from SMSC, e.g. DELIVERY_SM etc.
	 *
	 * @param response the response to be sent
	 * 
	 * @exception IOException exception during communication
	 * @exception ValueNotSetException optional param not set but requested
	 * @see Transmitter
	 */
	final public void respond(Response response) throws ValueNotSetException, IOException, WrongSessionStateException {
		checkState(response);
		debug.enter(DSESS, this, "respond(Response)");
		debug.write(DSESS, "Sending response " + response.debugString());
		try {
			transmitter.send(response);
		} catch (ValueNotSetException e) {
			event.write(e, "Sending a response.");
			debug.exit(DSESS, this);
			throw e;
		}
		debug.exit(DSESS, this);
	}

	/**
	 * Returns <code>Transmitter</code> object created for transmitting
	 * PDUs to SMSC.
	 *
	 * @return the <code>Transmitter</code> object
	 * @see Transmitter
	 */
	public Transmitter getTransmitter() {
		return transmitter;
	}

	/**
	 * Returns <code>Receiver</code> object created for receiving
	 * PDUs from SMSC.
	 *
	 * @return the <code>Receiver</code> object
	 * @see Receiver
	 */
	public Receiver getReceiver() {
		return receiver;
	}

	/**
	 * Returns <code>Connection</code> object provided for
	 * communication with SMSC.
	 *
	 * @return the <code>Connection</code> object
	 * @see Connection
	 */
	public Connection getConnection() {
		return connection;
	}

	/**
	 * The basic method for sending a <code>Request</code> PDU to SMSC
	 * and receiving correspondent <code>Response</code>, if applicable, and
	 * returns the received PDU.<br>
	 * In case there is a PDU received with unknown command id or with invalid
	 * length, <code>GenericNack</code> PDU is sent back to SMSC automatically
	 * to report the error and null is returned.
	 * If the timeout for receiving PDU set in the <code>Receiver</code>
	 * object expires, null is returned.
	 * <p>
	 * @param  request  the request PDU (not xxx_RESP) to be sent
	 * @return          the corresponding response or null in case of problems
	 *
	 * @exception IOException exception during communication
	 * @exception PDUException incorrect format of PDU
	 * @exception TimeoutException rest of data not received for too long time
	 * @exception ValueNotSetException optional param not set but requested
	 * @see Request
	 * @see Response
	 * @see GenericNack
	 * @see Transmitter
	 * @see Receiver
	 */
	final private Response send(Request request, boolean asynchronous)
		throws ValueNotSetException, TimeoutException, PDUException, IOException {
		debug.enter(DSESS, this, "send(Request)");
		Response response = null;
		debug.write(DSESS, "Sending request " + request.debugString());
		try {
			transmitter.send(request);
		} catch (ValueNotSetException e) {
			event.write(e, "Sending the request.");
			debug.exit(DSESS, this);
			throw e;
		}
		if ((!asynchronous) && (request.canResponse())) {
			PDU pdu = null;
			Response expResponse = null;
			expResponse = request.getResponse();
			try {
				debug.write(DSESS, "Going to receive response. Expecting " + expResponse.debugString());
				try {
					pdu = receiver.receive(expResponse);
				} catch (NotSynchronousException e) {
					debug.write("Unexpected NotSynchronousException caught, ignoring :-)");
				}
			} catch (UnknownCommandIdException e) {
				safeGenericNack(Data.ESME_RINVCMDID, e.getSequenceNumber());
			} catch (InvalidPDUException e) {
				if ((e.getException() instanceof NotEnoughDataInByteBufferException)
					|| (e.getException() instanceof TerminatingZeroNotFoundException)) {
					debug.write(DSESS, "wrong length " + e);
					debug.write(DSESS, " => sending gnack.");
					safeGenericNack(Data.ESME_RINVMSGLEN, e.getPDU().getSequenceNumber());
				} else {
					debug.write(DSESS, "InvalidPDUException - rethrowing " + e);
					debug.exit(DSESS, this);
					throw e;
				}
			} catch (TimeoutException e) {
				debug.write(DSESS, "TimeoutException - rethrowing " + e);
				debug.exit(DSESS, this);
				throw e;
			}
			if (pdu != null) {
				debug.write(DSESS, "Got response(?) pdu " + pdu.debugString());
				response = checkResponse(pdu, expResponse);
			} else {
				debug.write(DSESS, "No response received.");
			}
		}
		debug.exit(DSESS, this);
		return response;
	}

	/**
	 * Calls <code>send(Request,boolean)</code> with the current value of
	 * <code>asynchronous</code> flag.
	 * @see #send(Request,boolean)
	 */
	final private Response send(Request request)
		throws ValueNotSetException, TimeoutException, PDUException, IOException {
		return send(request, asynchronous);
	}

	/**
	 * Checks if the <code>pdu</code> received is
	 * matching the <code>expResponse</code> and returns corrected response.
	 * If the command id's don't match then if the <code>pdu</code>'s 
	 * command id is generic_nack, then the expected response is set
	 * the command id of generic_nack and command id, command status
	 * and sequence number of the received <code>pdu</code> and this
	 * way transformed response is returned: the class mathces, but
	 * the command id says, that the PDU is generic nack.<br>
	 * If the command id is not generic nack, then we received
	 * incorrect pdu as the sequence numbers match, so we send generic nack
	 * back to the smsc.
	 * if the command id's match, then the <code>pdu</code> is returned.
	 * @param pdu the received PDU which should be a response
	 * @param expResponse the response we expected from smsc
	 * @return either the received PDU or expected response transformed to
	 *         generic nack.
	 * @see #send(Request,boolean)
	 */
	private Response checkResponse(PDU pdu, Response expResponse)
		throws ValueNotSetException, TimeoutException, IOException {
		Response response = null;
		debug.write(DSESS, "checking response if it's what we expected.");
		if (pdu.getCommandId() != expResponse.getCommandId()) {
			debug.write(DSESS, "Got different response than expected " + expResponse.debugString());
			if (pdu.getCommandId() == Data.GENERIC_NACK) {
				// it's brutal, but it's necessary
				// we transform the response object to carry generic nack
				debug.write(DSESS, "Got generic nack. What could we do wrong?");
				expResponse.setCommandId(Data.GENERIC_NACK);
				expResponse.setCommandLength(pdu.getCommandLength());
				expResponse.setCommandStatus(pdu.getCommandStatus());
				expResponse.setSequenceNumber(pdu.getSequenceNumber());
				response = expResponse;
			} else {
				debug.write(DSESS, "invalid command id - sending gnack");
				safeGenericNack(Data.ESME_RINVCMDID, pdu.getSequenceNumber());
				response = null;
			}
		} else {
			// if the commandId is same as of the expected response's command id,
			// then the pdu must be Response as well
			response = (Response) pdu;
		}
		return response;
	}

	/**
	 * Sends a generic acknowledge with given comand status and sequence number
	 * and catches andy <code>SmppException</code>.
	 * @param commandStatus command status to report
	 * @param sequenceNumber to allow the other party to match the generic ack with
	 *                       their sent request
	 * @exception IOException if there is a comms error
	 */
	private void safeGenericNack(int commandStatus, int sequenceNumber) throws IOException {
		try {
			genericNack(commandStatus, sequenceNumber);
		} catch (SmppException e) {
			debug.write("Ignoring unexpected SmppException caught sending generic nack.");
			event.write(e, "Ignoring unexpected exception caught sending generic nack.");
		}
	}

	/** 
	 * Sets the state of the session.
	 * It's private as it's only for use by the methods of the session.
	 * @param state the new state of the session
	 * @see #checkState(int)
	 * @see #checkState(PDU)
	 * @see WrongSessionStateException
	 */
	private void setState(int state) {
		this.state = state;
	}

	/**
	 * Returns the current state of the session.
	 * It's public to allow the user to check the state of the session if necessary.
	 * @return the current state of the session
	 * @see #checkState(int)
	 * @see #checkState(PDU)
	 * @see WrongSessionStateException
	 */
	public int getState() {
		return state;
	}

	/**
	 * Enables checking if the session allows certain operation in the current state.
	 */
	public void enableStateChecking() {
		this.stateChecking = true;
	}

	/**
	 * Disables checking if the session allows certain operation in the current state.
	 */
	public void disableStateChecking() {
		this.stateChecking = false;
	}

	/**
	 * Checks if the session is in the state which is required by the parameter.
	 * If not, then exception <code>WrongSessionStateException</code> is thrown.
	 * Note that the states are bit values in integer, so there can be a "set"
	 * of states required, the session must be in one of the states required.
	 * The checking can be turned off, see
	 * <a href="#disableStateChecking()">disableStateChecking</a>.
	 * @param requestedState the state(s) in which the session is expected to be; if it
	 *                       is not, then exception is thrown
	 * @throws WrongSessionStateException if the session is not in the state
	 *         required
	 * @see WrongSessionStateException
	 */
	public void checkState(int requestedState) throws WrongSessionStateException {
		if (stateChecking) {
			debug.write(
				DSESS,
				"checking state current=0x"
					+ Integer.toHexString(state)
					+ " requested esme=0x"
					+ Integer.toHexString(requestedState));
			if ((state & requestedState) == 0) {
				throw new WrongSessionStateException(type, requestedState, state);
			}
		}
	}

	/**
	 * Checks if the session's state allows sending the PDU provided.
	 * If not, then exception <code>WrongSessionStateException</code> is thrown.
	 * For each state there is only a subset of PDUs which can be sent over the
	 * session. For example, if the session is bound as a receiver, it cannot
	 * submit messages, but it can send enquire link, generic nack etc.
	 * This method checks the PDU type (command id) aganst matrix of allowed states.
	 * The checking can be turned off, see
	 * <a href="#disableStateChecking()">disableStateChecking</a>.
	 * @param pdu the pdu which has to be checked if it's allowed in the current
	 *            state
	 * @throws WrongSessionStateException if the session is not in the state
	 *         required
	 * @see #checkState(int)
	 * @see WrongSessionStateException
	 */
	public void checkState(PDU pdu) throws WrongSessionStateException {
		if (stateChecking) {
			Hashtable<Integer, Integer> pduMatrix = getStateMatrix(type);
			Integer commandIdInteger = new Integer(pdu.getCommandId());
			Integer requestedStateInteger = pduMatrix == null ? null : pduMatrix.get(commandIdInteger);
			if (requestedStateInteger != null) {
				checkState(requestedStateInteger.intValue());
			} else {
				if (disallowUnknownPDU) {
					throw new WrongSessionStateException();
				}
			}
		}
	}

	/**
	 * Checks if the session is in the state which is required by the parameter.
	 * Note that this method doesn't throw an exception rather it returns false
	 * if the session is not in one of the provided states.
	 * @param requestedState the state(s) which have to be checked
	 * @return if the session is in on of the provided states
	 * @see #checkState(int)
	 */
	public boolean isStateAllowed(int requestedState) {
		boolean stateAllowed = true;
		try {
			checkState(requestedState);
		} catch (WrongSessionStateException e) {
			stateAllowed = false;
		}
		return stateAllowed;
	}

	/**
	 * Checks if the pdu provided is allowed in the current session state.
	 * Note that this method doesn't throw an exception rather it returns false
	 * if the pdu is not alloewd in the current session state.
	 * @param pdu the pdu which has to be checked if it's allowed in the current
	 *            state
	 * @return if the pdu is allowed to be sent in the current session state
	 * @see #checkState(PDU)
	 */
	public boolean isPDUAllowed(PDU pdu) {
		boolean pduAllowed = true;
		try {
			checkState(pdu);
		} catch (WrongSessionStateException e) {
			pduAllowed = false;
		}
		return pduAllowed;
	}

	/**
	 * Initialises state matrices for checking if PDU is allowed in a certain session
	 * state.
	 */
	static {
		initialiseStateMatrix();
	}

	/**
	 * Initialise list containing which operations (PDUs) valid in which state.
	 * @see #checkState(PDU)
	 * @see #type
	 */
	private static void initialiseStateMatrix() {
		esmeStateMatrix = new Hashtable<Integer, Integer>();
		addValidState(esmeStateMatrix, Data.BIND_TRANSMITTER, STATE_CLOSED);
		addValidState(esmeStateMatrix, Data.BIND_TRANSMITTER_RESP, STATE_NOT_ALLOWED);
		addValidState(esmeStateMatrix, Data.BIND_RECEIVER, STATE_CLOSED);
		addValidState(esmeStateMatrix, Data.BIND_RECEIVER_RESP, STATE_NOT_ALLOWED);
		addValidState(esmeStateMatrix, Data.BIND_TRANSCEIVER, STATE_CLOSED);
		addValidState(esmeStateMatrix, Data.BIND_TRANSCEIVER_RESP, STATE_NOT_ALLOWED);
		addValidState(esmeStateMatrix, Data.OUTBIND, STATE_NOT_ALLOWED);
		addValidState(esmeStateMatrix, Data.UNBIND, STATE_TRANSMITTER | STATE_RECEIVER | STATE_TRANSCEIVER);
		addValidState(esmeStateMatrix, Data.UNBIND_RESP, STATE_TRANSMITTER | STATE_RECEIVER | STATE_TRANSCEIVER);
		addValidState(esmeStateMatrix, Data.SUBMIT_SM, STATE_TRANSMITTER | STATE_TRANSCEIVER);
		addValidState(esmeStateMatrix, Data.SUBMIT_SM_RESP, STATE_NOT_ALLOWED);
		addValidState(esmeStateMatrix, Data.SUBMIT_MULTI, STATE_TRANSMITTER | STATE_TRANSCEIVER);
		addValidState(esmeStateMatrix, Data.SUBMIT_MULTI_RESP, STATE_NOT_ALLOWED);
		addValidState(esmeStateMatrix, Data.DATA_SM, STATE_TRANSMITTER | STATE_TRANSCEIVER);
		addValidState(esmeStateMatrix, Data.DATA_SM_RESP, STATE_RECEIVER | STATE_TRANSCEIVER);
		addValidState(esmeStateMatrix, Data.DELIVER_SM, STATE_NOT_ALLOWED);
		addValidState(esmeStateMatrix, Data.DELIVER_SM_RESP, STATE_RECEIVER | STATE_TRANSCEIVER);
		addValidState(esmeStateMatrix, Data.QUERY_SM, STATE_TRANSMITTER | STATE_TRANSCEIVER);
		addValidState(esmeStateMatrix, Data.QUERY_SM_RESP, STATE_NOT_ALLOWED);
		addValidState(esmeStateMatrix, Data.CANCEL_SM, STATE_TRANSMITTER | STATE_TRANSCEIVER);
		addValidState(esmeStateMatrix, Data.CANCEL_SM_RESP, STATE_NOT_ALLOWED);
		addValidState(esmeStateMatrix, Data.REPLACE_SM, STATE_TRANSMITTER | STATE_TRANSCEIVER);
		addValidState(esmeStateMatrix, Data.REPLACE_SM_RESP, STATE_NOT_ALLOWED);
		addValidState(esmeStateMatrix, Data.ENQUIRE_LINK, STATE_TRANSMITTER | STATE_RECEIVER | STATE_TRANSCEIVER);
		// STATE_ALWAYS);
		addValidState(esmeStateMatrix, Data.ENQUIRE_LINK_RESP, STATE_TRANSMITTER | STATE_RECEIVER | STATE_TRANSCEIVER);
		// STATE_ALWAYS);
		addValidState(esmeStateMatrix, Data.ALERT_NOTIFICATION, STATE_NOT_ALLOWED);
		addValidState(esmeStateMatrix, Data.GENERIC_NACK, STATE_TRANSMITTER | STATE_RECEIVER | STATE_TRANSCEIVER);
		// STATE_ALWAYS);

		mcStateMatrix = new Hashtable<Integer, Integer>();
		addValidState(mcStateMatrix, Data.BIND_TRANSMITTER, STATE_NOT_ALLOWED);
		addValidState(
			mcStateMatrix,
			Data.BIND_TRANSMITTER_RESP,
			STATE_TRANSMITTER | STATE_RECEIVER | STATE_TRANSCEIVER);
		// STATE_OPENED);
		addValidState(mcStateMatrix, Data.BIND_RECEIVER, STATE_NOT_ALLOWED);
		addValidState(mcStateMatrix, Data.BIND_RECEIVER_RESP, STATE_TRANSMITTER | STATE_RECEIVER | STATE_TRANSCEIVER);
		// STATE_OPENED);
		addValidState(mcStateMatrix, Data.BIND_TRANSCEIVER, STATE_NOT_ALLOWED);
		addValidState(
			mcStateMatrix,
			Data.BIND_TRANSCEIVER_RESP,
			STATE_TRANSMITTER | STATE_RECEIVER | STATE_TRANSCEIVER);
		// STATE_OPENED);
		addValidState(mcStateMatrix, Data.OUTBIND, STATE_TRANSMITTER | STATE_RECEIVER | STATE_TRANSCEIVER);
		// STATE_OPENED);
		addValidState(mcStateMatrix, Data.UNBIND, STATE_TRANSMITTER | STATE_RECEIVER | STATE_TRANSCEIVER);
		addValidState(mcStateMatrix, Data.UNBIND_RESP, STATE_TRANSMITTER | STATE_RECEIVER | STATE_TRANSCEIVER);
		addValidState(mcStateMatrix, Data.SUBMIT_SM, STATE_NOT_ALLOWED);
		addValidState(mcStateMatrix, Data.SUBMIT_SM_RESP, STATE_TRANSMITTER | STATE_TRANSCEIVER);
		addValidState(mcStateMatrix, Data.SUBMIT_MULTI, STATE_NOT_ALLOWED);
		addValidState(mcStateMatrix, Data.SUBMIT_MULTI_RESP, STATE_TRANSMITTER | STATE_TRANSCEIVER);
		addValidState(mcStateMatrix, Data.DATA_SM, STATE_RECEIVER | STATE_TRANSCEIVER);
		addValidState(mcStateMatrix, Data.DATA_SM_RESP, STATE_TRANSMITTER | STATE_TRANSCEIVER);
		addValidState(mcStateMatrix, Data.DELIVER_SM, STATE_RECEIVER | STATE_TRANSCEIVER);
		addValidState(mcStateMatrix, Data.DELIVER_SM_RESP, STATE_NOT_ALLOWED);
		addValidState(mcStateMatrix, Data.QUERY_SM, STATE_NOT_ALLOWED);
		addValidState(mcStateMatrix, Data.QUERY_SM_RESP, STATE_TRANSMITTER | STATE_TRANSCEIVER);
		addValidState(mcStateMatrix, Data.CANCEL_SM, STATE_NOT_ALLOWED);
		addValidState(mcStateMatrix, Data.CANCEL_SM_RESP, STATE_TRANSMITTER | STATE_TRANSCEIVER);
		addValidState(mcStateMatrix, Data.REPLACE_SM, STATE_NOT_ALLOWED);
		addValidState(mcStateMatrix, Data.REPLACE_SM_RESP, STATE_TRANSMITTER | STATE_TRANSCEIVER);
		addValidState(mcStateMatrix, Data.ENQUIRE_LINK, STATE_TRANSMITTER | STATE_RECEIVER | STATE_TRANSCEIVER);
		// STATE_ALWAYS);
		addValidState(mcStateMatrix, Data.ENQUIRE_LINK_RESP, STATE_TRANSMITTER | STATE_RECEIVER | STATE_TRANSCEIVER);
		// STATE_ALWAYS);
		addValidState(mcStateMatrix, Data.ALERT_NOTIFICATION, STATE_RECEIVER | STATE_TRANSCEIVER);
		addValidState(mcStateMatrix, Data.GENERIC_NACK, STATE_TRANSMITTER | STATE_RECEIVER | STATE_TRANSCEIVER);
		// STATE_ALWAYS);
	}

	/**
	 * Adds to the matrix a set of states in which can be sent a PDU with the 
	 * provided command id.
	 * @param matrix the matrix to add the mapping to
	 * @param commandId the commandId of the PDU the mapping is created for
	 * @param state the state(s) in which is the PDU valid
	 * @see #checkState(PDU)
	 * @see #isStateAllowed(int)
	 */
	private static void addValidState(Hashtable<Integer, Integer> matrix, int commandId, int state) {
		matrix.put(new Integer(commandId), new Integer(state));
	}

	/**
	 * Returns the state matrix for the requested session type.
	 * @see #type
	 * @see #TYPE_ESME
	 * @see #TYPE_MC
	 * @see #initialiseStateMatrix()
	 */
	private static Hashtable<Integer, Integer> getStateMatrix(int type) {
		switch (type) {
			case TYPE_ESME :
				return esmeStateMatrix;
			case TYPE_MC :
				return mcStateMatrix;
			default :
				return null;
		}
	}

	/**
	 * God, I would never think that to keep unbind synchronous in
	 * an asynchronous enviroment would be so funny. Here is the replacement
	 * listener which encapsulates the original listener and hunts for the 
	 * unbind pdu. Good luck, you source code reader!<br>
	 * The problem is that we want to return a response from session's unbind()
	 * even if the session is asynchronous, i.e. all pdus from the smsc
	 * are passed to an implementation of <code>ServerPDUEventListener</code>
	 * event responses. We can't simply stop the asynchronicity
	 * as there can still be some responses expected, so we need a bridge
	 * which allows us to wait for the unbind response and still serve
	 * the other pdus from the smsc in asynchronous manner. Thus this
	 * encapsulating listener, which exactly does the thing.
	 */
	private class UnbindServerPDUEventListener extends SmppObject implements ServerPDUEventListener {
		Session session;
		ServerPDUEventListener origListener;
		Unbind unbindReq;
		UnbindResp expectedResp;
		UnbindResp unbindResp = null;

		public UnbindServerPDUEventListener(Session session, ServerPDUEventListener origListener, Unbind unbindReq) {
			this.session = session;
			this.origListener = origListener;
			this.unbindReq = unbindReq;
			expectedResp = (UnbindResp) unbindReq.getResponse();
		}

		public void handleEvent(ServerPDUEvent event) {
			PDU pdu = event.getPDU();
			if (pdu.getSequenceNumber() == unbindReq.getSequenceNumber()) {
				synchronized (this) {
					try {
						unbindResp = (UnbindResp) (session.checkResponse(pdu, expectedResp));
					} catch (Exception e) {
						debug.write(DSESS, "exception handling unbind " + e);
						SmppObject.event.write(e, "exception handling unbind");
					}
					// notify as session waits for the notification
					this.notify();
				}
			} else {
				// all other pdus are processed by the original handler,
				// if any
				if (origListener != null) {
					origListener.handleEvent(event);
				}
			}
		}

		public UnbindResp getUnbindResp() {
			return unbindResp;
		}
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.3  2006/02/22 16:45:01  paoloc
 * UnbindServerPDUEventListener: if (pdu.equals(unbindReq)): as reported by users, this is incompatible with the new improved PDU.equals(); fixed.
 *
 * Revision 1.2  2004/09/10 23:03:44  sverkera
 * Added isOpened method
 *
 * Revision 1.1  2003/07/23 00:28:39  sverkera
 * Imported
 *
 * 
 * Old changelog:
 * 08-08-01 ticp@logica.com added asynchronous processing capability
 * 02-10-01 ticp@logica.com tracing now belongs to DSESS group
 * 20-11-01 ticp@logica.com the receiver thread is now started even for
 *                          the transmitter session - before transmitter
 *                          sessions couldn't receive anything
 * 20-11-01 ticp@logica.com receiver thread is started after the bind request
 *                          is sent to the MC, before it was started before the bind
 *                          req was sent to MC, therefore the response could be processed
 *                          by the listener
 * 22-11-01 ticp@logica.com implemented session states with checking if certain
 *                          operation is allowed in the current session state
 */
