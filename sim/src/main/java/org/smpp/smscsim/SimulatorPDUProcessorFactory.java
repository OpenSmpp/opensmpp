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

import org.smpp.debug.FileLog;
import org.smpp.smscsim.util.Table;
/**
 * Class <code>SimulatorPDUProcessorFactory</code> creates new instances of
 * a <code>SimulatorPDUProcessor</code>. It's passed to <code>SMSCListener</code>
 * which uses it to create new PDU processors whenewer new connection
 * from client is requested. The PDU processor is passed to
 * instance of <code>SMSCSession</code> which uses the processor to handle
 * client requests and responses.
 * 
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.1 $
 * @see PDUProcessorFactory
 * @see PDUProcessorGroup
 * @see SimulatorPDUProcessor
 */
public class SimulatorPDUProcessorFactory implements PDUProcessorFactory {
	private PDUProcessorGroup procGroup;
	private ShortMessageStore messageStore;
	private DeliveryInfoSender deliveryInfoSender;
	private Table users;

	/**
	 * If the information about processing has to be printed
	 * to the standard output.
	 */
	private boolean displayInfo = false;

	/**
	 * Constructs processor factory with given processor group, 
	 * message store for storing of the messages and a table of
	 * users for authentication. The message store and users parameters are
	 * passed to generated instancies of <code>SimulatorPDUProcessor</code>.
	 * @param procGroup the group the newly generated PDU processors will belong to
	 * @param messageStore the store for messages received from the client
	 * @param users the list of users used for authenticating of the client
	 */
	public SimulatorPDUProcessorFactory(
		PDUProcessorGroup procGroup,
		ShortMessageStore messageStore,
		DeliveryInfoSender deliveryInfoSender,
		Table users) {
		this.procGroup = procGroup;
		this.messageStore = messageStore;
		this.deliveryInfoSender = deliveryInfoSender;
		this.users = users;
	}

	/**
	 * Creates a new instance of <code>SimulatorPDUProcessor</code> with
	 * parameters provided in construction of th factory.
	 *
	 * @param session the sessin the PDU processor will work for
	 * @return newly created <code>SimulatorPDUProcessor</code>
	 */
	public PDUProcessor createPDUProcessor(SMSCSession session) {
		SimulatorPDUProcessor simPDUProcessor = new SimulatorPDUProcessor(session, messageStore, users);
		simPDUProcessor.setDisplayInfo(getDisplayInfo());
		simPDUProcessor.setGroup(procGroup);
		simPDUProcessor.setDeliveryInfoSender(deliveryInfoSender);
		display("new connection accepted");
		return simPDUProcessor;
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

	private void display(String info) {
		if (getDisplayInfo()) {
			System.out.println(FileLog.getLineTimeStamp() + " [sys] " + info);
		}
	}
}
/*
 * $Log: not supported by cvs2svn $
 * 
 * Old changelog:
 * 20-09-01 ticp@logica.com added reference to the DeliveryInfoSender to support
 *						    automatic sending of delivery info PDUs
 */
