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

import java.util.Enumeration;
import java.util.Hashtable;
import org.smpp.pdu.SubmitSM;
import org.smpp.smscsim.ShortMessageValue;

/**
 * Class <code>ShortMessageStore</code> is used to store the sms's sent by
 * a client to the smsc. Provides method to store the message, cancel
 * the message and replace it as well as methods for printing of all
 * the messages to standard output.
 *
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.2 $
 * @see ShortMessageValue
 */
public class ShortMessageStore {
	private Hashtable<String, ShortMessageValue> messages = new Hashtable<String, ShortMessageValue>();

	/**
	 * Construct the message store.
	 */
	public ShortMessageStore() {
	}

	/**
	 * Stores the message in a hashtable 
	 * where the key is the <code>messageId</code> and the value is
	 * a new instance of the class <code>ShortMessageValue</code>
	 * which contains subset of fields from <code>SubmitSM</code>.
	 *
	 * @param message the message received from the client
	 * @param messageId the message id assigned by smsc
	 * @param systemId the system id of the client to distinguish different
	 *                 clients
	 * @see ShortMessageValue
	 * @see org.smpp.pdu.SubmitSM
	 */
	public synchronized void submit(SubmitSM message, String messageId, String systemId) {
		messages.put(messageId, new ShortMessageValue(systemId, message));
	}

	/**
	 * Removes message with given <code>messageId</code> from the message store.
	 *
	 * @param messageId id of the message to remove
	 */
	public synchronized void cancel(String messageId) {
		messages.remove(messageId);
	}

	/**
	 * Finds message with the <code>messageId</code> and replaces it by
	 * the new message.
	 *
	 * @param messageId id of message to replace
	 * @param newMessage the text of the new message
	 */
	public synchronized void replace(String messageId, String newMessage) {
		ShortMessageValue sMV = (ShortMessageValue) messages.get(messageId);
		if (sMV != null) {
			sMV.shortMessage = newMessage;
		}
	}

	/**
	 * Returns a message with given messageId in internal format,
	 * i.e. ShortMessageValue.
	 *
	 * @param messageId the messag id of the message to return
	 * @see ShortMessageValue
	 */
	public synchronized ShortMessageValue getMessage(String messageId) {
		return (ShortMessageValue) messages.get(messageId);
	}

	/**
	 * Prints all messages currently stored in the message store.
	 */
	public synchronized void print() {
		if (messages.size() != 0) {
			ShortMessageValue sMV;
			Enumeration<String> keys = messages.keys();
			Object key;
			System.out.println("------------------------------------------------------------------------");
			System.out.println("| Msg Id   |Sender     |ServT|Source address |Dest address   |Message   ");
			System.out.println("------------------------------------------------------------------------");
			while (keys.hasMoreElements()) {
				key = keys.nextElement();
				sMV = (ShortMessageValue) messages.get(key);
				printMessage(key, sMV);
			}
		} else {
			System.out.println("There is no message in the message store.");
		}
	}

	/**
	 * Prints one message.
	 *
	 * @param key the key (message id) of the message
	 * @param sMV the message to print
	 */
	private void printMessage(Object key, ShortMessageValue sMV) {
		String messageId, systemId, serviceType, sourceAddr, destAddr, shortMessage;

		messageId = key.toString();
		systemId = pad(sMV.systemId, 11);
		if (sMV.serviceType.equals("")) {
			serviceType = "null";
		} else {
			serviceType = sMV.serviceType;
		}
		serviceType = pad(serviceType, 5);
		sourceAddr = pad(sMV.sourceAddr, 15);
		destAddr = pad(sMV.destinationAddr, 15);
		shortMessage = sMV.shortMessage;
		System.out.println(
			"- "
				+ messageId
				+ " |"
				+ systemId
				+ "|"
				+ serviceType
				+ "|"
				+ sourceAddr
				+ "|"
				+ destAddr
				+ "|"
				+ shortMessage);
	}

	private String pad(String data, int length) {
		String result;
		if (data == null) {
			data = "";
		}
		if (data.length() > length) {
			result = data.substring(1, length + 1);
		} else {
			int padCount = length - data.length();
			result = data;
			for (int i = 1; i <= padCount; i++) {
				result += " ";
			}
		}
		return result;
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.1  2003/07/23 00:28:39  sverkera
 * Imported
 *
 * 
 * Old changelog:
 * 20-09-01 ticp@logica.com added getMessage() returning a message data
 *                          for message with given messageId
 */
