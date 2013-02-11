package org.smpp.pdu;

import java.util.Vector;

/**
 * This code was stripped almost verbatim out of PDU to solve bug #1029141 (@see
 * PDUInitDeadlockTest). It avoids the problem of deadlocks during construction
 * of PDU subclasses, by moving the factory code to a separate class (thus
 * avoiding PDU descendants' constructors being called during class
 * initialisation). See JLS section 12.4.2 for class initialisation details.
 * 
 * Note that although this factory exists, PDUs are still created via their
 * constructors in this code base - only PDU uses this factory at time of
 * writing.
 * 
 * Performance note: a faster, unsynchronized alternative is provided for
 * possible future use (see comments in source code), although its use may be
 * limited to Java >= 5.0.
 * 
 * @author Paolo Campanella, BulkSMS.com.
 * 
 */
public class PDUFactory {
	/**
	 * Performance note: pduList should be "effectively immutable"
	 * as defined by Brian Goetz in "Java Concurrency in Practice" sec.
	 * 3.5.4, and should thus be safe to convert to an unsynchronized
	 * List (or Map). This would allow unsynchronized access. However,
	 * the mention of "works only in Java 5.0 and later" for a similar
	 * situation here:
	 * http://www.ibm.com/developerworks/java/library/j-hashmap.html
	 * made me avoid making this change, to play it safe.
	 * 
	 *  An unsynchronized alternative is provided below, commented
	 *  out, for a possible future case where support for Java < 5.0
	 *  is no longer required.
	 */
	private static final Vector<PDU> pduList = new Vector<PDU>(30, 4);
	/**
	 * Create a list of instances of classes which can represent a PDU.
	 * This list is used in <code>createPDU</code> to create a PDU
	 * from a binary buffer.
	 */
	static {
	 	pduList.add(new BindTransmitter());
		pduList.add(new BindTransmitterResp());
		pduList.add(new BindReceiver());
		pduList.add(new BindReceiverResp());
		pduList.add(new BindTransciever());
		pduList.add(new BindTranscieverResp());
		pduList.add(new Unbind());
		pduList.add(new UnbindResp());
		pduList.add(new Outbind());
		pduList.add(new SubmitSM());
		pduList.add(new SubmitSMResp());
		pduList.add(new SubmitMultiSM());
		pduList.add(new SubmitMultiSMResp());
		pduList.add(new DeliverSM());
		pduList.add(new DeliverSMResp());
		pduList.add(new DataSM());
		pduList.add(new DataSMResp());
		pduList.add(new QuerySM());
		pduList.add(new QuerySMResp());
		pduList.add(new CancelSM());
		pduList.add(new CancelSMResp());
		pduList.add(new ReplaceSM());
		pduList.add(new ReplaceSMResp());
		pduList.add(new EnquireLink());
		pduList.add(new EnquireLinkResp());
		pduList.add(new AlertNotification());
		pduList.add(new GenericNack());
	}

	/**
	 * 
	 * @param commandId
	 * @return A new instance of PDU of the type requested, or null if commandId
	 *         is unknown.
	 */
	public static final PDU createPDU(int commandId) {
		int size = pduList.size();
		PDU pdu = null;
		PDU newInstance = null;

		for (int i = 0; i < size; i++) {
			pdu = (PDU)pduList.get(i);
			if (pdu != null) {
				if (pdu.getCommandId() == commandId) {
					try {
						newInstance = (PDU) (pdu.getClass().newInstance());
					} catch (IllegalAccessException e) {
						// can't be illegal access, we initialised
						// the list with instances of our classes
					} catch (InstantiationException e) {
						// can't be instantiation as we already instantiated
						// at least once, for both exception see help
						// for Class.newInstance()
					}
					return newInstance;
				}
			}
		}
		return null;
	}
}

/*
 * This version is over twice as fast for single-thread access,
 * and (more importantly) is also unsynchronised:
 * 
public class PDUFactory {
	private static Map pduList = new HashMap(30);
	static {
		pduList.put(new Integer(Data.BIND_TRANSMITTER), BindTransmitter.class);
		pduList.put(new Integer(Data.BIND_TRANSMITTER_RESP), BindTransmitterResp.class);
		pduList.put(new Integer(Data.BIND_RECEIVER), BindReceiver.class);
		pduList.put(new Integer(Data.BIND_RECEIVER_RESP), BindReceiverResp.class);
		pduList.put(new Integer(Data.BIND_TRANSCEIVER), BindTransciever.class);
		pduList.put(new Integer(Data.BIND_TRANSCEIVER_RESP), BindTranscieverResp.class);
		pduList.put(new Integer(Data.UNBIND), Unbind.class);
		pduList.put(new Integer(Data.UNBIND_RESP), UnbindResp.class);
		pduList.put(new Integer(Data.OUTBIND), Outbind.class);
		pduList.put(new Integer(Data.SUBMIT_SM), SubmitSM.class);
		pduList.put(new Integer(Data.SUBMIT_SM_RESP), SubmitSMResp.class);
		pduList.put(new Integer(Data.SUBMIT_MULTI), SubmitMultiSM.class);
		pduList.put(new Integer(Data.SUBMIT_MULTI_RESP), SubmitMultiSMResp.class);
		pduList.put(new Integer(Data.DELIVER_SM), DeliverSM.class);
		pduList.put(new Integer(Data.DELIVER_SM_RESP), DeliverSMResp.class);
		pduList.put(new Integer(Data.DATA_SM), DataSM.class);
		pduList.put(new Integer(Data.DATA_SM_RESP), DataSMResp.class);
		pduList.put(new Integer(Data.QUERY_SM), QuerySM.class);
		pduList.put(new Integer(Data.QUERY_SM_RESP), QuerySMResp.class);
		pduList.put(new Integer(Data.CANCEL_SM), CancelSM.class);
		pduList.put(new Integer(Data.CANCEL_SM_RESP), CancelSMResp.class);
		pduList.put(new Integer(Data.REPLACE_SM), ReplaceSM.class);
		pduList.put(new Integer(Data.REPLACE_SM_RESP), ReplaceSMResp.class);
		pduList.put(new Integer(Data.ENQUIRE_LINK), EnquireLink.class);
		pduList.put(new Integer(Data.ENQUIRE_LINK_RESP), EnquireLinkResp.class);
		pduList.put(new Integer(Data.ALERT_NOTIFICATION), AlertNotification.class);
		pduList.put(new Integer(Data.GENERIC_NACK), GenericNack.class);
	}

	public static final PDU createPDU(int commandId) {
		Class c = (Class) pduList.get(new Integer(commandId));
		if (c != null) {
			try {
				return (PDU) (c.newInstance());
				// Both these exceptions should be impossible in
				// normal operation (other than through a coding
				// error in PDU or its descendants, or this class):
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
*/
