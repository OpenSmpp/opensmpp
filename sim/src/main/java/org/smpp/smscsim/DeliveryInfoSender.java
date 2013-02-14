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

import java.util.Date;
import java.text.SimpleDateFormat;

import org.smpp.Data;
import org.smpp.SmppObject;
import org.smpp.debug.Debug;
import org.smpp.pdu.DeliverSM;
import org.smpp.pdu.SubmitSM;
import org.smpp.pdu.WrongLengthOfStringException;
import org.smpp.util.ProcessingThread;
import org.smpp.util.Queue;

/**
 * Class <code>DeliveryInfoSender</code> ...
 *
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision$
 * @see SimulatorPDUProcessor
 */
public class DeliveryInfoSender extends ProcessingThread {

	public static final int DELIVERED = 0;
	public static final int EXPIRED = 1;
	public static final int DELETED = 2;
	public static final int UNDELIVERABLE = 3;
	public static final int ACCEPTED = 4;
	public static final int UNKNOWN = 5;
	public static final int REJECTED = 6;

	private static final String DLVR_INFO_SENDER_NAME = "DlvrInfoSender";
	private static int dlvrInfoSenderIndex = 0;

	private static final String DELIVERY_RCPT_DATE_FORMAT = "yyMMddHHmm";

	private SimpleDateFormat dateFormatter = new SimpleDateFormat(DELIVERY_RCPT_DATE_FORMAT);

	private long waitForQueueInterval = 5000; // in ms

	private Debug debug = SmppObject.getDebug();

	private static String[] states;

	static {
		states = new String[7];
		states[DELIVERED] = "DELIVRD";
		states[EXPIRED] = "EXPIRED";
		states[DELETED] = "DELETED";
		states[UNDELIVERABLE] = "UNDELIV";
		states[ACCEPTED] = "ACCEPTD";
		states[UNKNOWN] = "UNKNOWN";
		states[REJECTED] = "REJECTD";
	}

	private Queue submitRequests = new Queue();

	public void submit(PDUProcessor processor, SubmitSM submitRequest, String messageId, int stat, int err) {
		DeliveryInfoEntry entry = new DeliveryInfoEntry(processor, submitRequest, stat, err, messageId);
		submitRequests.enqueue(entry);
	}

	public void submit(PDUProcessor processor, SubmitSM submitRequest, String messageId) {
		submit(processor, submitRequest, messageId, DELIVERED, 0);
	}

	private void deliver(DeliveryInfoEntry entry) {
		debug.enter(this, "deliver");
		SubmitSM submit = entry.submit;
		DeliverSM deliver = new DeliverSM();
                deliver.setEsmClass((byte)Data.SM_SMSC_DLV_RCPT_TYPE);
                
		deliver.setSourceAddr(submit.getDestAddr());
		deliver.setDestAddr(submit.getDestAddr());
                
                deliver.setDataCoding((byte) 0x03); // ISO-Latin-1
		String msg = "";
		msg += "id:" + entry.messageId + " ";
		msg += "sub:" + entry.sub + " ";
		msg += "dlvrd:" + entry.dlvrd + " ";
		msg += "submit date:" + formatDate(entry.submitted) + " ";
		msg += "done date:" + formatDate(System.currentTimeMillis()) + " ";
		msg += "stat:" + states[entry.stat] + " ";
		msg += "err:" + entry.err + " ";
		String shortMessage = submit.getShortMessage();
		int msgLen = shortMessage.length();
		msg += "text:" + shortMessage.substring(0, (msgLen > 20 ? 20 : msgLen));
		try {
			deliver.setShortMessage(msg);
			deliver.setServiceType(submit.getServiceType());
		} catch (WrongLengthOfStringException e) {
		}
		try {
			entry.processor.serverRequest(deliver);
		} catch (Exception e) {
		}
		debug.exit(this);
	}

	public void process() {
		if (submitRequests.isEmpty()) {
			try {
				synchronized (submitRequests) {
					submitRequests.wait(waitForQueueInterval);
				}
			} catch (InterruptedException e) {
				// it's ok to be interrupted when waiting
			}
		} else {
			while (!submitRequests.isEmpty()) {
				deliver((DeliveryInfoEntry) submitRequests.dequeue());
			}
		}
	}

	public String getThreadName() {
		return DLVR_INFO_SENDER_NAME;
	}

	public int getThreadIndex() {
		return ++dlvrInfoSenderIndex;
	}

	private String formatDate(long ms) {
		synchronized (dateFormatter) {
			return dateFormatter.format(new Date(ms));
		}
	}

	protected class DeliveryInfoEntry {
		public PDUProcessor processor;
		public SubmitSM submit;
		public int sub = 1;
		public int dlvrd = 1;
		public int stat;
		public int err;
		public String messageId;
		public long submitted = System.currentTimeMillis();

		public DeliveryInfoEntry(PDUProcessor processor, SubmitSM submit, int stat, int err, String messageId) {
			this.processor = processor;
			this.submit = submit;
			this.stat = stat;
			this.err = err;
			this.messageId = messageId;
		}
	}
}
/*
 * $Log$
 */
