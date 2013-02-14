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
package org.smpp.test;

import java.io.*;
import java.util.Properties;

import org.smpp.*;
import org.smpp.debug.Debug;
import org.smpp.debug.Event;
import org.smpp.debug.FileDebug;
import org.smpp.debug.FileEvent;
import org.smpp.pdu.*;
import org.smpp.util.Queue;

/**
 * Class <code>SMPPTest</code> shows how to use the SMPP toolkit.
 * You can bound and unbind from the SMSC, you can send every possible 
 * pdu and wait for a pdu sent from the SMSC.
 *
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.2 $
 */
public class SMPPTest {
	static final String copyright =
		"Copyright (c) 1996-2001 Logica Mobile Networks Limited\n"
			+ "This product includes software developed by Logica by whom copyright\n"
			+ "and know-how are retained, all rights reserved.\n";
	static final String version = "SMPP Open Source test & demonstration application, version 1.1\n";

	static {
		System.out.println(copyright);
		System.out.println(version);
	}

	/**
	 * Directory for creating of debug and event files.
	 */
	static final String dbgDir = "./";

	/**
	 * The debug object.
	 * @see FileDebug
	 */
	static Debug debug = new FileDebug(dbgDir, "test.dbg");

	/**
	 * The event object.
	 * @see FileEvent
	 */
	static Event event = new FileEvent(dbgDir, "test.evt");

	/**
	 * File with default settings for the application.
	 */
	static String propsFilePath = "./smpptest.cfg";

	static BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));

	/**
	 * This is the SMPP session used for communication with SMSC.
	 */
	static Session session = null;

	/**
	 * Contains the parameters and default values for this test
	 * application such as system id, password, default npi and ton
	 * of sender etc.
	 */
	Properties properties = new Properties();

	/**
	 * If the application is bound to the SMSC.
	 */
	boolean bound = false;

	/**
	 * If the application has to keep reading commands
	 * from the keyboard and to do what's requested.
	 */
	private boolean keepRunning = true;

	/**
	 * Address of the SMSC.
	 */
	String ipAddress = null;

	/**
	 * The port number to bind to on the SMSC server.
	 */
	int port = 0;

	/**
	 * The name which identifies you to SMSC.
	 */
	String systemId = null;

	/**
	 * The password for authentication to SMSC.
	 */
	String password = null;

	/**
	 * How you want to bind to the SMSC: transmitter (t), receiver (r) or
	 * transciever (tr). Transciever can both send messages and receive
	 * messages. Note, that if you bind as receiver you can still receive
	 * responses to you requests (submissions).
	 */
	String bindOption = "t";

	/**
	 * Indicates that the Session has to be asynchronous.
	 * Asynchronous Session means that when submitting a Request to the SMSC
	 * the Session does not wait for a response. Instead the Session is provided
	 * with an instance of implementation of ServerPDUListener from the smpp
	 * library which receives all PDUs received from the SMSC. It's
	 * application responsibility to match the received Response with sended Requests.
	 */
	boolean asynchronous = false;

	/**
	 * This is an instance of listener which obtains all PDUs received from the SMSC.
	 * Application doesn't have explicitly call Session's receive() function,
	 * all PDUs are passed to this application callback object.
	 * See documentation in Session, Receiver and ServerPDUEventListener classes
	 * form the SMPP library.
	 */
	SMPPTestPDUEventListener pduListener = null;

	/**
	 * The range of addresses the smpp session will serve.
	 */
	AddressRange addressRange = new AddressRange();

	/*
	 * for information about these variables have a look in SMPP 3.4
	 * specification
	 */
	String systemType = "";
	String serviceType = "";
	Address sourceAddress = new Address();
	Address destAddress = new Address();
	String scheduleDeliveryTime = "";
	String validityPeriod = "";
	String shortMessage = "";
	int numberOfDestination = 1;
	String messageId = "";
	byte esmClass = 0;
	byte protocolId = 0;
	byte priorityFlag = 0;
	byte registeredDelivery = 0;
	byte replaceIfPresentFlag = 0;
	byte dataCoding = 0;
	byte smDefaultMsgId = 0;

	/**
	 * If you attemt to receive message, how long will the application
	 * wait for data.
	 */
	long receiveTimeout = Data.RECEIVE_BLOCKING;

	/**
	 * Initialises the application, lods default values for
	 * connection to SMSC and for various PDU fields.
	 */
	public SMPPTest() throws IOException {
		loadProperties(propsFilePath);
	}

	/**
	 * Sets global SMPP library debug and event objects.
	 * Runs the application.
	 * @see SmppObject#setDebug(Debug)
	 * @see SmppObject#setEvent(Event)
	 */
	public static void main(String args[]) {
		System.out.println("Initialising...");
		System.out.println("Charset used: " + new InputStreamReader(System.in).getEncoding());
		debug.activate();
		event.activate();
		SmppObject.setDebug(debug);
		SmppObject.setEvent(event);
		SMPPTest test = null;
		try {
			test = new SMPPTest();
		} catch (IOException e) {
			event.write(e, "");
			debug.write("exception initialising SMPPTest " + e);
			System.out.println("Exception initialising SMPPTest " + e);
		}
		if (test != null) {
			test.menu();
		}
	}

	/**
	 * Displays the menu and lets you choose from available options.
	 */
	public void menu() {
		keepRunning = true;
		String option = "1";
		int optionInt;

		while (keepRunning) {
			System.out.println();
			System.out.println("-  1 bind");
			System.out.println("-  2 submit (t/tr)");
			System.out.println("-  3 submit multi (t/tr)");
			System.out.println("-  4 data (t/tr)");
			System.out.println("-  5 query (t/tr)");
			System.out.println("-  6 replace (t/tr)");
			System.out.println("-  7 cancel (t/tr)");
			System.out.println("-  8 enquire link (t/tr)");
			System.out.println("-  9 unbind");
			System.out.println("- 10 receive message (tr/r)");
			System.out.println("-  0 exit");
			System.out.print("> ");
			optionInt = -1;
			try {
				option = keyboard.readLine();
				optionInt = Integer.parseInt(option);
			} catch (Exception e) {
				debug.write("exception reading keyboard " + e);
				optionInt = -1;
			}
			switch (optionInt) {
				case 1 :
					bind();
					break;
				case 2 :
					submit();
					break;
				case 3 :
					submitMulti();
					break;
				case 4 :
					data();
					break;
				case 5 :
					query();
					break;
				case 6 :
					replace();
					break;
				case 7 :
					cancel();
					break;
				case 8 :
					enquireLink();
					break;
				case 9 :
					unbind();
					break;
				case 10 :
					receive();
					break;
				case 0 :
					exit();
					break;
				case -1 :
					// default option if entering an option went wrong
					break;
				default :
					System.out.println("Invalid option. Choose between 0 and 10.");
					break;
			}
		}
	}

	/**
	 * The first method called to start communication
	 * betwen an ESME and a SMSC. A new instance of <code>TCPIPConnection</code>
	 * is created and the IP address and port obtained from user are passed
	 * to this instance. New <code>Session</code> is created which uses the created
	 * <code>TCPIPConnection</code>.
	 * All the parameters required for a bind are set to the <code>BindRequest</code>
	 * and this request is passed to the <code>Session</code>'s <code>bind</code>
	 * method. If the call is successful, the application should be bound to the SMSC.
	 *
	 * See "SMPP Protocol Specification 3.4, 4.1 BIND Operation."
	 * @see BindRequest
	 * @see BindResponse
	 * @see TCPIPConnection
	 * @see Session#bind(BindRequest)
	 * @see Session#bind(BindRequest,ServerPDUEventListener)
	 */
	private void bind() {
		debug.enter(this, "SMPPTest.bind()");
		try {

			if (bound) {
				System.out.println("Already bound, unbind first.");
				return;
			}

			BindRequest request = null;
			BindResponse response = null;
			String syncMode = (asynchronous ? "a" : "s");

			// type of the session
			syncMode = getParam("Asynchronous/Synchronnous Session? (a/s)", syncMode);
			if (syncMode.compareToIgnoreCase("a") == 0) {
				asynchronous = true;
			} else if (syncMode.compareToIgnoreCase("s") == 0) {
				asynchronous = false;
			} else {
				System.out.println(
					"Invalid mode async/sync, expected a or s, got " + syncMode + ". Operation canceled.");
				return;
			}

			// input values
			bindOption = getParam("Transmitter/Receiver/Transciever (t/r/tr)", bindOption);

			if (bindOption.compareToIgnoreCase("t") == 0) {
				request = new BindTransmitter();
			} else if (bindOption.compareToIgnoreCase("r") == 0) {
				request = new BindReceiver();
			} else if (bindOption.compareToIgnoreCase("tr") == 0) {
				request = new BindTransciever();
			} else {
				System.out.println(
					"Invalid bind mode, expected t, r or tr, got " + bindOption + ". Operation canceled.");
				return;
			}

			ipAddress = getParam("IP address of SMSC", ipAddress);
			port = getParam("Port number", port);

			TCPIPConnection connection = new TCPIPConnection(ipAddress, port);
			connection.setReceiveTimeout(20 * 1000);
			session = new Session(connection);

			systemId = getParam("Your system ID", systemId);
			password = getParam("Your password", password);

			// set values
			request.setSystemId(systemId);
			request.setPassword(password);
			request.setSystemType(systemType);
			request.setInterfaceVersion((byte) 0x34);
			request.setAddressRange(addressRange);

			// send the request
			System.out.println("Bind request " + request.debugString());
			if (asynchronous) {
				pduListener = new SMPPTestPDUEventListener(session);
				response = session.bind(request, pduListener);
			} else {
				response = session.bind(request);
			}
			System.out.println("Bind response " + response.debugString());
			if (response.getCommandStatus() == Data.ESME_ROK) {
				bound = true;
			}

		} catch (Exception e) {
			event.write(e, "");
			debug.write("Bind operation failed. " + e);
			System.out.println("Bind operation failed. " + e);
		} finally {
			debug.exit(this);
		}
	}

	/**
	 * Ubinds (logs out) from the SMSC and closes the connection.
	 *
	 * See "SMPP Protocol Specification 3.4, 4.2 UNBIND Operation."
	 * @see Session#unbind()
	 * @see Unbind
	 * @see UnbindResp
	 */
	private void unbind() {
		debug.enter(this, "SMPPTest.unbind()");
		try {

			if (!bound) {
				System.out.println("Not bound, cannot unbind.");
				return;
			}

			// send the request
			System.out.println("Going to unbind.");
			if (session.getReceiver().isReceiver()) {
				System.out.println("It can take a while to stop the receiver.");
			}
			UnbindResp response = session.unbind();
			System.out.println("Unbind response " + response.debugString());
			bound = false;

		} catch (Exception e) {
			event.write(e, "");
			debug.write("Unbind operation failed. " + e);
			System.out.println("Unbind operation failed. " + e);
		} finally {
			debug.exit(this);
		}
	}

	/**
	 * Creates a new instance of <code>SubmitSM</code> class, lets you set
	 * subset of fields of it. This PDU is used to send SMS message
	 * to a device.
	 *
	 * See "SMPP Protocol Specification 3.4, 4.4 SUBMIT_SM Operation."
	 * @see Session#submit(SubmitSM)
	 * @see SubmitSM
	 * @see SubmitSMResp
	 */
	private void submit() {
		debug.enter(this, "SMPPTest.submit()");

		try {
			SubmitSM request = new SubmitSM();
			SubmitSMResp response;

			// input values
			serviceType = getParam("Service type", serviceType);
			sourceAddress = getAddress("Source", sourceAddress);
			destAddress = getAddress("Destination", destAddress);
			replaceIfPresentFlag = getParam("Replace if present flag", replaceIfPresentFlag);
			shortMessage = getParam("The short message", shortMessage);
			scheduleDeliveryTime = getParam("Schedule delivery time", scheduleDeliveryTime);
			validityPeriod = getParam("Validity period", validityPeriod);
			esmClass = getParam("Esm class", esmClass);
			protocolId = getParam("Protocol id", protocolId);
			priorityFlag = getParam("Priority flag", priorityFlag);
			registeredDelivery = getParam("Registered delivery", registeredDelivery);
			dataCoding = getParam("Data encoding", dataCoding);
			smDefaultMsgId = getParam("Sm default msg id", smDefaultMsgId);

			// set values
			request.setServiceType(serviceType);
			request.setSourceAddr(sourceAddress);
			request.setDestAddr(destAddress);
			request.setReplaceIfPresentFlag(replaceIfPresentFlag);
			request.setShortMessage(shortMessage,Data.ENC_GSM7BIT);
			request.setScheduleDeliveryTime(scheduleDeliveryTime);
			request.setValidityPeriod(validityPeriod);
			request.setEsmClass(esmClass);
			request.setProtocolId(protocolId);
			request.setPriorityFlag(priorityFlag);
			request.setRegisteredDelivery(registeredDelivery);
			request.setDataCoding(dataCoding);
			request.setSmDefaultMsgId(smDefaultMsgId);

			// send the request

			int count = 1;
			System.out.println();
			count = getParam("How many times to submit this message (load test)", count);
			for (int i = 0; i < count; i++) {
				request.assignSequenceNumber(true);
				System.out.print("#" + i + "  ");
				System.out.println("Submit request " + request.debugString());
				if (asynchronous) {
					session.submit(request);
					System.out.println();
				} else {
					response = session.submit(request);
					System.out.println("Submit response " + response.debugString());
					messageId = response.getMessageId();
				}
			}

		} catch (Exception e) {
			event.write(e, "");
			debug.write("Submit operation failed. " + e);
			System.out.println("Submit operation failed. " + e);
		} finally {
			debug.exit(this);
		}
	}

	/**
	 * Creates a new instance of <code>SubmitMultiSM</code> class, lets you set
	 * subset of fields of it. This PDU is used to send SMS message
	 * to multiple devices.
	 *
	 * See "SMPP Protocol Specification 3.4, 4.5 SUBMIT_MULTI Operation."
	 * @see Session#submitMulti(SubmitMultiSM)
	 * @see SubmitMultiSM
	 * @see SubmitMultiSMResp
	 */
	private void submitMulti() {
		debug.enter(this, "SMPPTest.submitMulti()");

		try {
			SubmitMultiSM request = new SubmitMultiSM();
			SubmitMultiSMResp response;

			// input values and set some :-)
			serviceType = getParam("Service type", serviceType);
			sourceAddress = getAddress("Source", sourceAddress);
			numberOfDestination = getParam("Number of destinations", numberOfDestination);
			for (int i = 0; i < numberOfDestination; i++) {
				request.addDestAddress(new DestinationAddress(getAddress("Destination", destAddress)));
			}
			replaceIfPresentFlag = getParam("Replace if present flag", replaceIfPresentFlag);
			shortMessage = getParam("The short message", shortMessage);
			scheduleDeliveryTime = getParam("Schdule delivery time", scheduleDeliveryTime);
			validityPeriod = getParam("Validity period", validityPeriod);
			esmClass = getParam("Esm class", esmClass);
			protocolId = getParam("Protocol id", protocolId);
			priorityFlag = getParam("Priority flag", priorityFlag);
			registeredDelivery = getParam("Registered delivery", registeredDelivery);
			dataCoding = getParam("Data encoding", dataCoding);
			smDefaultMsgId = getParam("Sm default msg id", smDefaultMsgId);

			// set other values
			request.setServiceType(serviceType);
			request.setSourceAddr(sourceAddress);
			request.setReplaceIfPresentFlag(replaceIfPresentFlag);
			request.setShortMessage(shortMessage);
			request.setScheduleDeliveryTime(scheduleDeliveryTime);
			request.setValidityPeriod(validityPeriod);
			request.setEsmClass(esmClass);
			request.setProtocolId(protocolId);
			request.setPriorityFlag(priorityFlag);
			request.setRegisteredDelivery(registeredDelivery);
			request.setDataCoding(dataCoding);
			request.setSmDefaultMsgId(smDefaultMsgId);

			// send the request
			System.out.println("Submit Multi request " + request.debugString());
			if (asynchronous) {
				session.submitMulti(request);
			} else {
				response = session.submitMulti(request);
				System.out.println("Submit Multi response " + response.debugString());
				messageId = response.getMessageId();
			}

		} catch (Exception e) {
			event.write(e, "");
			debug.write("Submit Multi operation failed. " + e);
			System.out.println("Submit Multi operation failed. " + e);
		} finally {
			debug.exit(this);
		}
	}

	/**
	 * Creates a new instance of <code>ReplaceSM</code> class, lets you set
	 * subset of fields of it. This PDU is used to replace certain
	 * attributes of already submitted message providing that you 'remember'
	 * message id of the submitted message. The message id is assigned
	 * by SMSC and is returned to you with the response to the submision
	 * PDU (SubmitSM, DataSM etc.).
	 *
	 * See "SMPP Protocol Specification 3.4, 4.10 REPLACE_SM Operation."
	 * @see Session#replace(ReplaceSM)
	 * @see ReplaceSM
	 * @see ReplaceSMResp
	 */
	private void replace() {
		debug.enter(this, "SMPPTest.replace()");
		try {
			ReplaceSM request = new ReplaceSM();
			ReplaceSMResp response;

			// input values
			messageId = getParam("Message id", messageId);
			sourceAddress = getAddress("Source", sourceAddress);
			shortMessage = getParam("The short message", shortMessage);
			scheduleDeliveryTime = getParam("Schedule delivery time", scheduleDeliveryTime);
			validityPeriod = getParam("Validity period", validityPeriod);
			registeredDelivery = getParam("Registered delivery", registeredDelivery);
			smDefaultMsgId = getParam("Sm default msg id", smDefaultMsgId);

			// set values
			request.setMessageId(messageId);
			request.setSourceAddr(sourceAddress);
			request.setShortMessage(shortMessage);
			request.setScheduleDeliveryTime(scheduleDeliveryTime);
			request.setValidityPeriod(validityPeriod);
			request.setRegisteredDelivery(registeredDelivery);
			request.setSmDefaultMsgId(smDefaultMsgId);

			// send the request
			System.out.println("Replace request " + request.debugString());
			if (asynchronous) {
				session.replace(request);
			} else {
				response = session.replace(request);
				System.out.println("Replace response " + response.debugString());
			}

		} catch (Exception e) {
			event.write(e, "");
			debug.write("Replace operation failed. " + e);
			System.out.println("Replace operation failed. " + e);
		} finally {
			debug.exit(this);
		}
	}

	/**
	 * Creates a new instance of <code>CancelSM</code> class, lets you set
	 * subset of fields of it. This PDU is used to cancel an already
	 * submitted message. You can only cancel a message which haven't been
	 * delivered to the device yet.
	 *
	 * See "SMPP Protocol Specification 3.4, 4.9 CANCEL_SM Operation."
	 * @see Session#cancel(CancelSM)
	 * @see CancelSM
	 * @see CancelSMResp
	 */
	private void cancel() {
		debug.enter(this, "SMPPTest.cancel()");
		try {
			CancelSM request = new CancelSM();
			CancelSMResp response;

			// input values
			serviceType = getParam("Service type", serviceType);
			messageId = getParam("Message id", messageId);
			sourceAddress = getAddress("Source", sourceAddress);
			destAddress = getAddress("Destination", destAddress);

			// set values
			request.setServiceType(serviceType);
			request.setMessageId(messageId);
			request.setSourceAddr(sourceAddress);
			request.setDestAddr(destAddress);

			// send the request
			System.out.println("Cancel request " + request.debugString());
			if (asynchronous) {
				session.cancel(request);
			} else {
				response = session.cancel(request);
				System.out.println("Cancel response " + response.debugString());
			}

		} catch (Exception e) {
			event.write(e, "");
			debug.write("Cancel operation failed. " + e);
			System.out.println("Cancel operation failed. " + e);
		} finally {
			debug.exit(this);
		}
	}

	/**
	 * Creates a new instance of <code>DataSM</code> class, lets you set
	 * subset of fields of it. This PDU is an alternative to the <code>SubmitSM</code>
	 * and </code>DeliverSM</code>. It delivers the data to the specified device.
	 *
	 * See "SMPP Protocol Specification 3.4, 4.7 DATA_SM Operation."
	 * @see Session#data(DataSM)
	 * @see DataSM
	 * @see DataSMResp
	 */
	private void data() {
		debug.enter(this, "SMPPTest.data()");

		try {
			DataSM request = new DataSM();
			DataSMResp response;

			// input values
			serviceType = getParam("Service type", serviceType);
			sourceAddress = getAddress("Source", sourceAddress, Data.SM_DATA_ADDR_LEN);
			destAddress = getAddress("Destination", destAddress, Data.SM_DATA_ADDR_LEN);
			esmClass = getParam("Esm class", esmClass);
			registeredDelivery = getParam("Registered delivery", registeredDelivery);
			dataCoding = getParam("Data encoding", dataCoding);

			// set values
			request.setServiceType(serviceType);
			request.setSourceAddr(sourceAddress);
			request.setDestAddr(destAddress);
			request.setEsmClass(esmClass);
			request.setRegisteredDelivery(registeredDelivery);
			request.setDataCoding(dataCoding);

			// send the request
			System.out.println("Data request " + request.debugString());
			if (asynchronous) {
				session.data(request);
			} else {
				response = session.data(request);
				System.out.println("Data response " + response.debugString());
				messageId = response.getMessageId();
			}

		} catch (Exception e) {
			event.write(e, "");
			debug.write("Data operation failed. " + e);
			System.out.println("Data operation failed. " + e);
		} finally {
			debug.exit(this);
		}
	}

	/**
	 * Creates a new instance of <code>QuerySM</code> class, lets you set
	 * subset of fields of it. This PDU is used to fetch information
	 * about status of already submitted message providing that you 'remember'
	 * message id of the submitted message. The message id is assigned
	 * by SMSC and is returned to you with the response to the submision
	 * PDU (SubmitSM, DataSM etc.).
	 *
	 * See "SMPP Protocol Specification 3.4, 4.8 QUERY_SM Operation."
	 * @see Session#query(QuerySM)
	 * @see QuerySM
	 * @see QuerySMResp
	 */
	private void query() {
		debug.enter(this, "SMPPTest.query()");
		try {
			QuerySM request = new QuerySM();
			QuerySMResp response;

			// input values
			messageId = getParam("Message id", messageId);
			sourceAddress = getAddress("Source", sourceAddress);

			// set values
			request.setMessageId(messageId);
			request.setSourceAddr(sourceAddress);

			// send the request
			System.out.println("Query request " + request.debugString());
			if (asynchronous) {
				session.query(request);
			} else {
				response = session.query(request);
				System.out.println("Query response " + response.debugString());
				messageId = response.getMessageId();
			}

		} catch (Exception e) {
			event.write(e, "");
			debug.write("Query operation failed. " + e);
			System.out.println("Query operation failed. " + e);
		} finally {
			debug.exit(this);
		}
	}

	/**
	 * Creates a new instance of <code>EnquireSM</code> class.
	 * This PDU is used to check that application level of the other party
	 * is alive. It can be sent both by SMSC and ESME.
	 *
	 * See "SMPP Protocol Specification 3.4, 4.11 ENQUIRE_LINK Operation."
	 * @see Session#enquireLink(EnquireLink)
	 * @see EnquireLink
	 * @see EnquireLinkResp
	 */
	private void enquireLink() {
		debug.enter(this, "SMPPTest.enquireLink()");
		try {

			EnquireLink request = new EnquireLink();
			EnquireLinkResp response;
			System.out.println("Enquire Link request " + request.debugString());
			if (asynchronous) {
				session.enquireLink(request);
			} else {
				response = session.enquireLink(request);
				System.out.println("Enquire Link response " + response.debugString());
			}

		} catch (Exception e) {
			event.write(e, "");
			debug.write("Enquire Link operation failed. " + e);
			System.out.println("Enquire Link operation failed. " + e);
		} finally {
			debug.exit(this);
		}
	}

	/**
	 * Receives one PDU of any type from SMSC and prints it on the screen.
	 * @see Session#receive()
	 * @see Response
	 * @see ServerPDUEvent
	 */
	private void receive() {
		debug.enter(this, "SMPPTest.receive()");
		try {

			PDU pdu = null;
			System.out.print("Going to receive a PDU. ");
			if (receiveTimeout == Data.RECEIVE_BLOCKING) {
				System.out.print(
					"The receive is blocking, i.e. the application " + "will stop until a PDU will be received.");
			} else {
				System.out.print("The receive timeout is " + receiveTimeout / 1000 + " sec.");
			}
			System.out.println();
			if (asynchronous) {
				ServerPDUEvent pduEvent = pduListener.getRequestEvent(receiveTimeout);
				if (pduEvent != null) {
					pdu = pduEvent.getPDU();
				}
			} else {
				pdu = session.receive(receiveTimeout);
			}
			if (pdu != null) {
				System.out.println("Received PDU " + pdu.debugString());
				if (pdu.isRequest()) {
					Response response = ((Request) pdu).getResponse();
					// respond with default response
					System.out.println("Going to send default response to request " + response.debugString());
					session.respond(response);
				}
			} else {
				System.out.println("No PDU received this time.");
			}

		} catch (Exception e) {
			event.write(e, "");
			debug.write("Receiving failed. " + e);
			System.out.println("Receiving failed. " + e);
		} finally {
			debug.exit(this);
		}
	}

	/**
	 * If bound, unbinds and then exits this application.
	 */
	private void exit() {
		debug.enter(this, "SMPPTest.exit()");
		if (bound) {
			unbind();
		}
		keepRunning = false;
		debug.exit(this);
	}

	/**
	 * Implements simple PDU listener which handles PDUs received from SMSC.
	 * It puts the received requests into a queue and discards all received
	 * responses. Requests then can be fetched (should be) from the queue by
	 * calling to the method <code>getRequestEvent</code>.
	 * @see Queue
	 * @see ServerPDUEvent
	 * @see ServerPDUEventListener
	 * @see SmppObject
	 */
	private class SMPPTestPDUEventListener extends SmppObject implements ServerPDUEventListener {
		@SuppressWarnings("unused")
		Session session;
		Queue requestEvents = new Queue();

		public SMPPTestPDUEventListener(Session session) {
			this.session = session;
		}

		public void handleEvent(ServerPDUEvent event) {
			PDU pdu = event.getPDU();
			if (pdu.isRequest()) {
				System.out.println("async request received, enqueuing " + pdu.debugString());
				synchronized (requestEvents) {
					requestEvents.enqueue(event);
					requestEvents.notify();
				}
			} else if (pdu.isResponse()) {
				System.out.println("async response received " + pdu.debugString());
			} else {
				System.out.println(
					"pdu of unknown class (not request nor " + "response) received, discarding " + pdu.debugString());
			}
		}

		/**
		 * Returns received pdu from the queue. If the queue is empty,
		 * the method blocks for the specified timeout.
		 */
		public ServerPDUEvent getRequestEvent(long timeout) {
			ServerPDUEvent pduEvent = null;
			synchronized (requestEvents) {
				if (requestEvents.isEmpty()) {
					try {
						requestEvents.wait(timeout);
					} catch (InterruptedException e) {
						// ignoring, actually this is what we're waiting for
					}
				}
				if (!requestEvents.isEmpty()) {
					pduEvent = (ServerPDUEvent) requestEvents.dequeue();
				}
			}
			return pduEvent;
		}
	}

	/**
	 * Prompts the user to enter a string value for a parameter.
	 */
	private String getParam(String prompt, String defaultValue) {
		String value = "";
		String promptFull = prompt;
		promptFull += defaultValue == null ? "" : " [" + defaultValue + "] ";
		System.out.print(promptFull);
		try {
			value = keyboard.readLine();
		} catch (IOException e) {
			event.write(e, "");
			debug.write("Got exception getting a param. " + e);
		}
		if (value.compareTo("") == 0) {
			return defaultValue;
		} else {
			return value;
		}
	}

	/**
	 * Prompts the user to enter a byte value for a parameter.
	 */
	private byte getParam(String prompt, byte defaultValue) {
		return Byte.parseByte(getParam(prompt, Byte.toString(defaultValue)));
	}

	/**
	 * Prompts the user to enter an integer value for a parameter.
	 */
	private int getParam(String prompt, int defaultValue) {
		return Integer.parseInt(getParam(prompt, Integer.toString(defaultValue)));
	}

	/**
	 * Prompts the user to enter an address value with specified max length.
	 */
	private Address getAddress(String type, Address address, int maxAddressLength)
		throws WrongLengthOfStringException {
		byte ton = getParam(type + " address TON", address.getTon());
		byte npi = getParam(type + " address NPI", address.getNpi());
		String addr = getParam(type + " address", address.getAddress());
		address.setTon(ton);
		address.setNpi(npi);
		address.setAddress(addr, maxAddressLength);
		return address;
	}

	/**
	 * Prompts the user to enter an address value with max length set to the
	 * default length Data.SM_ADDR_LEN.
	 */
	private Address getAddress(String type, Address address) throws WrongLengthOfStringException {
		return getAddress(type, address, Data.SM_ADDR_LEN);
	}

	/**
	 * Loads configuration parameters from the file with the given name.
	 * Sets private variable to the loaded values.
	 */
	private void loadProperties(String fileName) throws IOException {
		System.out.println("Reading configuration file " + fileName + "...");
		FileInputStream propsFile = new FileInputStream(fileName);
		properties.load(propsFile);
		propsFile.close();
		System.out.println("Setting default parameters...");
		byte ton;
		byte npi;
		String addr;
		String bindMode;
		int rcvTimeout;
		String syncMode;

		ipAddress = properties.getProperty("ip-address");
		port = getIntProperty("port", port);
		systemId = properties.getProperty("system-id");
		password = properties.getProperty("password");

		ton = getByteProperty("addr-ton", addressRange.getTon());
		npi = getByteProperty("addr-npi", addressRange.getNpi());
		addr = properties.getProperty("address-range", addressRange.getAddressRange());
		addressRange.setTon(ton);
		addressRange.setNpi(npi);
		try {
			addressRange.setAddressRange(addr);
		} catch (WrongLengthOfStringException e) {
			System.out.println("The length of address-range parameter is wrong.");
		}

		ton = getByteProperty("source-ton", sourceAddress.getTon());
		npi = getByteProperty("source-npi", sourceAddress.getNpi());
		addr = properties.getProperty("source-address", sourceAddress.getAddress());
		setAddressParameter("source-address", sourceAddress, ton, npi, addr);

		ton = getByteProperty("destination-ton", destAddress.getTon());
		npi = getByteProperty("destination-npi", destAddress.getNpi());
		addr = properties.getProperty("destination-address", destAddress.getAddress());
		setAddressParameter("destination-address", destAddress, ton, npi, addr);

		serviceType = properties.getProperty("service-type", serviceType);
		systemType = properties.getProperty("system-type", systemType);
		bindMode = properties.getProperty("bind-mode", bindOption);
		if (bindMode.equalsIgnoreCase("transmitter")) {
			bindMode = "t";
		} else if (bindMode.equalsIgnoreCase("receiver")) {
			bindMode = "r";
		} else if (bindMode.equalsIgnoreCase("transciever")) {
			bindMode = "tr";
		} else if (
			!bindMode.equalsIgnoreCase("t") && !bindMode.equalsIgnoreCase("r") && !bindMode.equalsIgnoreCase("tr")) {
			System.out.println(
				"The value of bind-mode parameter in "
					+ "the configuration file "
					+ fileName
					+ " is wrong. "
					+ "Setting the default");
			bindMode = "t";
		}
		bindOption = bindMode;

		// receive timeout in the cfg file is in seconds, we need milliseconds
		// also conversion from -1 which indicates infinite blocking
		// in the cfg file to Data.RECEIVE_BLOCKING which indicates infinite
		// blocking in the library is needed.
		if (receiveTimeout == Data.RECEIVE_BLOCKING) {
			rcvTimeout = -1;
		} else {
			rcvTimeout = ((int) receiveTimeout) / 1000;
		}
		rcvTimeout = getIntProperty("receive-timeout", rcvTimeout);
		if (rcvTimeout == -1) {
			receiveTimeout = Data.RECEIVE_BLOCKING;
		} else {
			receiveTimeout = rcvTimeout * 1000;
		}

		syncMode = properties.getProperty("sync-mode", (asynchronous ? "async" : "sync"));
		if (syncMode.equalsIgnoreCase("sync")) {
			asynchronous = false;
		} else if (syncMode.equalsIgnoreCase("async")) {
			asynchronous = true;
		} else {
			asynchronous = false;
		}

		/*
		scheduleDeliveryTime
		validityPeriod
		shortMessage
		numberOfDestination
		messageId
		esmClass
		protocolId
		priorityFlag
		registeredDelivery
		replaceIfPresentFlag
		dataCoding
		smDefaultMsgId
		*/
	}

	/**
	 * Gets a property and converts it into byte.
	 */
	private byte getByteProperty(String propName, byte defaultValue) {
		return Byte.parseByte(properties.getProperty(propName, Byte.toString(defaultValue)));
	}

	/**
	 * Gets a property and converts it into integer.
	 */
	private int getIntProperty(String propName, int defaultValue) {
		return Integer.parseInt(properties.getProperty(propName, Integer.toString(defaultValue)));
	}

	/**
	 * Sets attributes of <code>Address</code> to the provided values.
	 */
	private void setAddressParameter(String descr, Address address, byte ton, byte npi, String addr) {
		address.setTon(ton);
		address.setNpi(npi);
		try {
			address.setAddress(addr);
		} catch (WrongLengthOfStringException e) {
			System.out.println("The length of " + descr + " parameter is wrong.");
		}
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.1  2003/07/23 00:28:39  sverkera
 * Imported
 *
 * 
 * Old changelog:
 * 15-09-01 ticp@logica.com added asynchronous processing capability
 *                          as the library has this feature now
 * 10-10-01 ticp@logica.com file moved to package com.logica.smpp.test,
 *                          old package com.logica.smpptest not used anymore
 * 11-10-01 ticp@logica.com the max address length checking reflected
 * 11-10-01 ticp@logica.com added commants
 */
