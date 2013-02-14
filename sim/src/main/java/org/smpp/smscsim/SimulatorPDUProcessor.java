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
package org.smpp.smscsim;

import java.io.IOException;

import org.smpp.*;
import org.smpp.debug.Debug;
import org.smpp.debug.Event;
import org.smpp.debug.FileLog;
import org.smpp.pdu.*;
import org.smpp.smscsim.util.Record;
import org.smpp.smscsim.util.Table;

/**
 * Class <code>SimulatorPDUProcessor</code> gets the <code>Request</code>
 * from the client and creates the proper
 * <code>Response</code> and sends it. At the beginning it authenticates
 * the client using information in the bind request and list of users provided
 * during construction of the processor. It also stores messages
 * sent from client and allows cancellation and replacement of the messages.
 *
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.2 $
 * @see PDUProcessor
 * @see SimulatorPDUProcessorFactory
 * @see SMSCSession
 * @see ShortMessageStore
 * @see Table
 */
public class SimulatorPDUProcessor extends PDUProcessor {
	/**
	 * The session this processor uses for sending of PDUs.
	 */
	private SMSCSession session = null;

	/**
	 * The container for received messages.
	 */
	private ShortMessageStore messageStore = null;

	/**
	 * The thread which sends delivery information for messages
	 * which require delivery information.
	 */
	private DeliveryInfoSender deliveryInfoSender = null;

	/**
	 * The table with system id's and passwords for authenticating
	 * of the bounding ESMEs.
	 */
	private Table users = null;

	/**
	 * Indicates if the bound has passed.
	 */
	private boolean bound = false;

	/**
	 * The system id of the bounded ESME.
	 */
	private String systemId = null;

	/**
	 * If the information about processing has to be printed
	 * to the standard output.
	 */
	private boolean displayInfo = false;

	/**
	 * The message id assigned by simulator to submitted messages.
	 */
	private static int intMessageId = 2000;

	/**
	 * System id of this simulator sent to the ESME in bind response.
	 */
	private static final String SYSTEM_ID = "Smsc Simulator";

	/**
	 * The name of attribute which contains the system id of ESME.
	 */
	private static final String SYSTEM_ID_ATTR = "name";

	/**
	 * The name of attribute which conatins password of ESME.
	 */
	private static final String PASSWORD_ATTR = "password";

	private Debug debug = SmppObject.getDebug();
	private Event event = SmppObject.getEvent();

	/**
	 * Constructs the PDU processor with given session,
	 * message store for storing of the messages and a table of
	 * users for authentication.
	 * @param session the sessin this PDU processor works for
	 * @param messageStore the store for messages received from the client
	 * @param users the list of users used for authenticating of the client
	 */
	public SimulatorPDUProcessor(SMSCSession session, ShortMessageStore messageStore, Table users) {
		this.session = session;
		this.messageStore = messageStore;
		this.users = users;
	}

	public void stop() {
	}

	/**
	 * Depending on the <code>commandId</code>
	 * of the <code>request</code> creates the proper response.
	 * The first request must be a <code>BindRequest</code> with the correct
	 * parameters.
	 * @param request the request from client
	 */
	public void clientRequest(Request request) {
		debug.write("SimulatorPDUProcessor.clientRequest() " + request.debugString());
		Response response;
		int commandStatus;
		int commandId = request.getCommandId();
		try {
			display("client request: " + request.debugString());
			if (!bound) { // the first PDU must be bound request
				if (commandId == Data.BIND_TRANSMITTER
					|| commandId == Data.BIND_RECEIVER
					|| commandId == Data.BIND_TRANSCEIVER) {
					commandStatus = checkIdentity((BindRequest) request);
					if (commandStatus == 0) { // authenticated
						// firstly generate proper bind response
						BindResponse bindResponse = (BindResponse) request.getResponse();
						bindResponse.setSystemId(SYSTEM_ID);
						// and send it to the client via serverResponse
						serverResponse(bindResponse);
						// success => bound
						bound = true;
					} else { // system id not authenticated
						// get the response
						response = request.getResponse();
						// set it the error command status
						response.setCommandStatus(commandStatus);
						// and send it to the client via serverResponse
						serverResponse(response);
						// bind failed, stopping the session
						session.stop();
					}
				} else {
					// the request isn't a bound req and this is wrong: if not
					// bound, then the server expects bound PDU
					if (request.canResponse()) {
						// get the response
						response = request.getResponse();
						response.setCommandStatus(Data.ESME_RINVBNDSTS);
						// and send it to the client via serverResponse
						serverResponse(response);
					} else {
						// cannot respond to a request which doesn't have
						// a response :-(
					}
					// bind failed, stopping the session
					session.stop();
				}
			} else { // already bound, can receive other PDUs
				if (request.canResponse()) {
					response = request.getResponse();
					switch (commandId) { // for selected PDUs do extra steps
						case Data.SUBMIT_SM :
							SubmitSMResp submitResponse = (SubmitSMResp) response;
							submitResponse.setMessageId(assignMessageId());
							display("putting message into message store");
							messageStore.submit((SubmitSM) request, submitResponse.getMessageId(), systemId);
							byte registeredDelivery =
								(byte) (((SubmitSM) request).getRegisteredDelivery() & Data.SM_SMSC_RECEIPT_MASK);
							if (registeredDelivery == Data.SM_SMSC_RECEIPT_REQUESTED) {
								deliveryInfoSender.submit(this, (SubmitSM) request, submitResponse.getMessageId());
							}
							break;

						case Data.SUBMIT_MULTI :
							SubmitMultiSMResp submitMultiResponse = (SubmitMultiSMResp) response;
							submitMultiResponse.setMessageId(assignMessageId());
							break;

						case Data.DELIVER_SM :
							DeliverSMResp deliverResponse = (DeliverSMResp) response;
							deliverResponse.setMessageId(assignMessageId());
							break;

						case Data.DATA_SM :
							DataSMResp dataResponse = (DataSMResp) response;
							dataResponse.setMessageId(assignMessageId());
							break;

						case Data.QUERY_SM :
							QuerySM queryRequest = (QuerySM) request;
							QuerySMResp queryResponse = (QuerySMResp) response;
							display("querying message in message store");
							queryResponse.setMessageId(queryRequest.getMessageId());
							break;

						case Data.CANCEL_SM :
							CancelSM cancelRequest = (CancelSM) request;
							display("cancelling message in message store");
							messageStore.cancel(cancelRequest.getMessageId());
							break;

						case Data.REPLACE_SM :
							ReplaceSM replaceRequest = (ReplaceSM) request;
							display("replacing message in message store");
							messageStore.replace(replaceRequest.getMessageId(), replaceRequest.getShortMessage());
							break;

						case Data.UNBIND :
							// do nothing, just respond and after sending
							// the response stop the session
							break;
					}
					// send the prepared response
					serverResponse(response);
					if (commandId == Data.UNBIND) {
						// unbind causes stopping of the session
						session.stop();
					}
				} else {
					// can't respond => nothing to do :-)
				}
			}
		} catch (WrongLengthOfStringException e) {
			event.write(e, "");
		} catch (Exception e) {
			event.write(e, "");
		}
	}

	/**
	 * Processes the response received from the client.
	 * @param response the response from client
	 */
	public void clientResponse(Response response) {
		debug.write("SimulatorPDUProcessor.clientResponse() " + response.debugString());
		display("client response: " + response.debugString());
	}

	/**
	 * Sends a request to a client. For example, it can be used to send
	 * delivery info to the client.
	 * @param request the request to be sent to the client
	 */
	public void serverRequest(Request request) throws IOException, PDUException {
		debug.write("SimulatorPDUProcessor.serverRequest() " + request.debugString());
		display("server request: " + request.debugString());
		session.send(request);
	}

	/**
	 * Send the response created by <code>clientRequest</code> to the client.
	 * @param response the response to send to client
	 */
	public void serverResponse(Response response) throws IOException, PDUException {
		synchronized(this) {
			try {
				// Sleep for 300 ms sending reply to more accuratly simulate smsc
				this.wait(300);
			} catch( Exception e ) { e.printStackTrace(); }
		}

		debug.write("SimulatorPDUProcessor.serverResponse() " + response.debugString());
		display("server response: " + response.debugString());
		session.send(response);
	}

	/**
	 * Checks if the bind request contains valid system id and password.
	 * For this uses the table of users provided in the constructor of the
	 * <code>SimulatorPDUProcessor</code>. If the authentication fails,
	 * i.e. if either the user isn't found or the password is incorrect,
	 * the function returns proper status code.
	 * @param request the bind request as received from the client
	 * @return status code of the authentication; ESME_ROK if authentication
	 *         passed
	 */
	private int checkIdentity(BindRequest request) {
		int commandStatus = Data.ESME_ROK;
		Record user = users.find(SYSTEM_ID_ATTR, request.getSystemId());
		if (user != null) {
			String password = user.getValue(PASSWORD_ATTR);
			if (password != null) {
				if (!request.getPassword().equals(password)) {
					commandStatus = Data.ESME_RINVPASWD;
					debug.write("system id " + request.getSystemId() + " not authenticated. Invalid password.");
					display("not authenticated " + request.getSystemId() + " -- invalid password");
				} else {
					systemId = request.getSystemId();
					debug.write("system id " + systemId + " authenticated");
					display("authenticated " + systemId);
				}
			} else {
				commandStatus = Data.ESME_RINVPASWD;
				debug.write(
					"system id " + systemId + " not authenticated. " + "Password attribute not found in users file");
				display("not authenticated " + systemId + " -- no password for user.");
			}
		} else {
			commandStatus = Data.ESME_RINVSYSID;
			debug.write("system id " + request.getSystemId() + " not authenticated -- not found");
			display("not authenticated " + request.getSystemId() + " -- user not found");
		}
		return commandStatus;
	}

	/**
	 * Creates a unique message_id for each sms sent by a client to the smsc.
	 * @return unique message id
	 */
	private String assignMessageId() {
		String messageId = "Smsc";
		intMessageId++;
		messageId += intMessageId;
		return messageId;
	}

	/**
	 * Returns the session this PDU processor works for.
	 * @return the session of this PDU processor
	 */
	public SMSCSession getSession() {
		return session;
	}

	/**
	 * Returns the system id of the client for whose is this PDU processor
	 * processing PDUs.
	 * @return system id of client
	 */
	public String getSystemId() {
		return systemId;
	}

	/**
	 * Sets if the info about processing has to be printed on
	 * the standard output.
	 */
	public void setDisplayInfo(boolean on) {
		displayInfo = on;
	}

	/**
	 * Returns status of printing of processing info on the standard output.
	 */
	public boolean getDisplayInfo() {
		return displayInfo;
	}

	/**
	 * Sets the delivery info sender object which is used to generate and send
	 * delivery pdus for messages which require the delivery info as the outcome
	 * of their sending.
	 */
	public void setDeliveryInfoSender(DeliveryInfoSender deliveryInfoSender) {
		this.deliveryInfoSender = deliveryInfoSender;
	}

	private void display(String info) {
		if (getDisplayInfo()) {
			String sysId = getSystemId();
			if (sysId == null) {
				sysId = "";
			}
			System.out.println(FileLog.getLineTimeStamp() + " [" + sysId + "] " + info);
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
 * 20-09-01 ticp@logica.com added reference to the DeliveryInfoSender to support
 *                          automatic sending of delivery info PDUs
 */
