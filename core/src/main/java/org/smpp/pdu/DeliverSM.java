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
package org.smpp.pdu;

import java.io.UnsupportedEncodingException;

import org.smpp.Data;
import org.smpp.util.*;
import org.smpp.pdu.Request;
import org.smpp.pdu.ValueNotSetException;
import org.smpp.pdu.tlv.*;

/**
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.1 $
 */
public class DeliverSM extends Request {
	// mandatory parameters
	private String serviceType = Data.DFLT_SRVTYPE;
	private Address sourceAddr = new Address();
	private Address destAddr = new Address();
	private byte esmClass = Data.DFLT_ESM_CLASS;
	private byte protocolId = Data.DFLT_PROTOCOLID;
	private byte priorityFlag = Data.DFLT_PRIORITY_FLAG;
	private String scheduleDeliveryTime = Data.DFLT_SCHEDULE; // not used
	private String validityPeriod = Data.DFLT_VALIDITY; // not used
	private byte registeredDelivery = Data.DFLT_REG_DELIVERY;
	private byte replaceIfPresentFlag = Data.DFTL_REPLACE_IFP; // not used
	private byte dataCoding = Data.DFLT_DATA_CODING;
	private byte smDefaultMsgId = Data.DFLT_DFLTMSGID; // not used
	private short smLength = Data.DFLT_MSG_LEN;
	private ShortMessage shortMessage = new ShortMessage(Data.SM_MSG_LEN);

	// optional parameters
	private TLVShort userMessageReference = new TLVShort(Data.OPT_PAR_USER_MSG_REF);
	private TLVShort sourcePort = new TLVShort(Data.OPT_PAR_SRC_PORT);
	//private TLVByte   sourceAddrSubunit     = new TLVByte(Data.OPT_PAR_SRC_ADDR_SUBUNIT);
	private TLVShort destinationPort = new TLVShort(Data.OPT_PAR_DST_PORT);
	//private TLVByte   destAddrSubunit       = new TLVByte(Data.OPT_PAR_DST_ADDR_SUBUNIT);
	private TLVShort sarMsgRefNum = new TLVShort(Data.OPT_PAR_SAR_MSG_REF_NUM);
	private TLVUByte sarTotalSegments = new TLVUByte(Data.OPT_PAR_SAR_TOT_SEG);
	private TLVUByte sarSegmentSeqnum = new TLVUByte(Data.OPT_PAR_SAR_SEG_SNUM);
	//private TLVByte   moreMsgsToSend        = new TLVByte(Data.OPT_PAR_MORE_MSGS);
	private TLVByte payloadType = new TLVByte(Data.OPT_PAR_PAYLOAD_TYPE);
	private TLVOctets messagePayload =
		new TLVOctets(Data.OPT_PAR_MSG_PAYLOAD, Data.OPT_PAR_MSG_PAYLOAD_MIN, Data.OPT_PAR_MSG_PAYLOAD_MAX);
	private TLVByte privacyIndicator = new TLVByte(Data.OPT_PAR_PRIV_IND);
	private TLVOctets callbackNum =
		new TLVOctets(Data.OPT_PAR_CALLBACK_NUM, Data.OPT_PAR_CALLBACK_NUM_MIN, Data.OPT_PAR_CALLBACK_NUM_MAX);
	// 4-19
	//private TLVByte   callbackNumPresInd    = new TLVByte(Data.OPT_PAR_CALLBACK_NUM_PRES_IND);
	//private TLVOctets callbackNumAtag       = new TLVOctets(Data.OPT_PAR_CALLBACK_NUM_ATAG,Data.OPT_PAR_CALLBACK_NUM_ATAG_MIN,Data.OPT_PAR_CALLBACK_NUM_ATAG_MAX); // 1-65
	private TLVOctets sourceSubaddress =
		new TLVOctets(Data.OPT_PAR_SRC_SUBADDR, Data.OPT_PAR_SRC_SUBADDR_MIN, Data.OPT_PAR_SRC_SUBADDR_MAX);
	// 2-23
	private TLVOctets destSubaddress =
		new TLVOctets(Data.OPT_PAR_DEST_SUBADDR, Data.OPT_PAR_DEST_SUBADDR_MIN, Data.OPT_PAR_DEST_SUBADDR_MAX);
	private TLVByte userResponseCode = new TLVByte(Data.OPT_PAR_USER_RESP_CODE);
	//private TLVByte   displayTime           = new TLVByte(Data.OPT_PAR_DISPLAY_TIME);
	//private TLVShort  smsSignal             = new TLVShort(Data.OPT_PAR_SMS_SIGNAL);
	//private TLVByte   msValidity            = new TLVByte(Data.OPT_PAR_MS_VALIDITY);
	//private TLVByte   msMsgWaitFacilities   = new TLVByte(Data.OPT_PAR_MSG_WAIT); // bit mask
	//private TLVByte   numberOfMessages      = new TLVByte(Data.OPT_PAR_NUM_MSGS);
	//private TLVEmpty  alertOnMsgDelivery    = new TLVEmpty(Data.OPT_PAR_ALERT_ON_MSG_DELIVERY);
	private TLVByte languageIndicator = new TLVByte(Data.OPT_PAR_LANG_IND);
	//private TLVByte   itsReplyType          = new TLVByte(Data.OPT_PAR_ITS_REPLY_TYPE);
	private TLVShort itsSessionInfo = new TLVShort(Data.OPT_PAR_ITS_SESSION_INFO);
	//private TLVByte   ussdServiceOp         = new TLVByte(Data.OPT_PAR_USSD_SER_OP);

	private TLVOctets networkErrorCode =
		new TLVOctets(Data.OPT_PAR_NW_ERR_CODE, Data.OPT_PAR_NW_ERR_CODE_MIN, Data.OPT_PAR_NW_ERR_CODE_MAX);
	// exactly 3
	private TLVByte messageState = new TLVByte(Data.OPT_PAR_MSG_STATE);
	private TLVString receiptedMessageId =
		new TLVString(Data.OPT_PAR_RECP_MSG_ID, Data.OPT_PAR_RECP_MSG_ID_MIN, Data.OPT_PAR_RECP_MSG_ID_MAX);
	// 1-

	public DeliverSM() {
		super(Data.DELIVER_SM);

		registerOptional(userMessageReference);
		registerOptional(sourcePort);
		registerOptional(destinationPort);
		registerOptional(sarMsgRefNum);
		registerOptional(sarTotalSegments);
		registerOptional(sarSegmentSeqnum);
		registerOptional(payloadType);
		registerOptional(messagePayload);
		registerOptional(privacyIndicator);
		registerOptional(callbackNum);
		registerOptional(sourceSubaddress);
		registerOptional(destSubaddress);
		registerOptional(userResponseCode);
		registerOptional(languageIndicator);
		registerOptional(itsSessionInfo);
		registerOptional(networkErrorCode);
		registerOptional(messageState);
		registerOptional(receiptedMessageId);
	}

	protected Response createResponse() {
		return new DeliverSMResp();
	}

	public void setBody(ByteBuffer buffer)
		throws NotEnoughDataInByteBufferException, TerminatingZeroNotFoundException, PDUException {
		@SuppressWarnings("unused")
		String dummyStr;
		@SuppressWarnings("unused")
		byte dummyByte;
		setServiceType(buffer.removeCString());
		sourceAddr.setData(buffer);
		destAddr.setData(buffer);
		setEsmClass(buffer.removeByte());
		setProtocolId(buffer.removeByte());
		setPriorityFlag(buffer.removeByte());
		dummyStr = buffer.removeCString(); // default scheduleDeliveryTime
		dummyStr = buffer.removeCString(); // default validityPeriod
		setRegisteredDelivery(buffer.removeByte());
		dummyByte = buffer.removeByte(); // default replaceIfPresentFlag
		setDataCoding(buffer.removeByte());
		dummyByte = buffer.removeByte(); // default smDefaultMsgId
		setSmLength(decodeUnsigned(buffer.removeByte()));
		shortMessage.setData(buffer.removeBuffer(getSmLength()));
	}

	public ByteBuffer getBody() {
		ByteBuffer buffer = new ByteBuffer();
		buffer.appendCString(getServiceType());
		buffer.appendBuffer(getSourceAddr().getData());
		buffer.appendBuffer(getDestAddr().getData());
		buffer.appendByte(getEsmClass());
		buffer.appendByte(getProtocolId());
		buffer.appendByte(getPriorityFlag());
		buffer.appendCString(getScheduleDeliveryTime());
		buffer.appendCString(getValidityPeriod());
		buffer.appendByte(getRegisteredDelivery());
		buffer.appendByte(getReplaceIfPresentFlag());
		buffer.appendByte(getDataCoding());
		buffer.appendByte(getSmDefaultMsgId());
		buffer.appendByte(encodeUnsigned(getSmLength()));
		buffer.appendBuffer(shortMessage.getData());
		return buffer;
	}

	public void setServiceType(String value) throws WrongLengthOfStringException {
		checkCString(value, Data.SM_SRVTYPE_LEN);
		serviceType = value;
	}

	//not used in deliver_sm
	//public void setScheduleDeliveryTime(String value)
	//throws WrongDateFormatException {
	//    checkDate(value);
	//    scheduleDeliveryTime = value;
	//}

	//not used in deliver_sm
	//public void setValidityPeriod(String value)
	//throws WrongDateFormatException {
	//    checkDate(value);
	//    validityPeriod = value;
	//}

	public void setShortMessage(String value) throws WrongLengthOfStringException {
		shortMessage.setMessage(value);
		setSmLength((short) shortMessage.getLength());
	}

	public void setShortMessage(String value, String encoding)
		throws WrongLengthOfStringException, UnsupportedEncodingException {
		shortMessage.setMessage(value, encoding);
		setSmLength((short) shortMessage.getLength());
	}

	public void setShortMessageData(ByteBuffer buffer) throws PDUException, NotEnoughDataInByteBufferException, TerminatingZeroNotFoundException {
		setSmLength((short) buffer.length());
		shortMessage.setData(buffer);
	}

	public void setSourceAddr(Address value) {
		sourceAddr = value;
	}
	public void setSourceAddr(String address) throws WrongLengthOfStringException {
		setSourceAddr(new Address(address));
	}
	public void setSourceAddr(byte ton, byte npi, String address) throws WrongLengthOfStringException {
		setSourceAddr(new Address(ton, npi, address));
	}

	public void setDestAddr(Address value) {
		destAddr = value;
	}
	public void setDestAddr(String address) throws WrongLengthOfStringException {
		setDestAddr(new Address(address));
	}
	public void setDestAddr(byte ton, byte npi, String address) throws WrongLengthOfStringException {
		setDestAddr(new Address(ton, npi, address));
	}

	public void setEsmClass(byte value) {
		esmClass = value;
	}
	public void setProtocolId(byte value) {
		protocolId = value;
	}
	public void setPriorityFlag(byte value) {
		priorityFlag = value;
	}
	public void setRegisteredDelivery(byte value) {
		registeredDelivery = value;
	}
	//not used in deliver_sm
	//public void setReplaceIfPresentFlag(byte value)   { replaceIfPresentFlag = value; }
	public void setDataCoding(byte value) {
		dataCoding = value;
	}
	//not used in deliver_sm
	//public void setSmDefaultMsgId(byte value)         { smDefaultMsgId = value; }
	// setSmLength() is private as it's set to length of the message
	private void setSmLength(short value) {
		smLength = value;
	}

	public String getServiceType() {
		return serviceType;
	}
	public String getScheduleDeliveryTime() {
		return scheduleDeliveryTime;
	}
	public String getValidityPeriod() {
		return validityPeriod;
	}
	public String getShortMessage() {
		return shortMessage.getMessage();
	}
	public String getShortMessage(String encoding) throws UnsupportedEncodingException {
		return shortMessage.getMessage(encoding);
	}
	public ByteBuffer getShortMessageData() {
		return shortMessage.getData();
	}
	public Address getSourceAddr() {
		return sourceAddr;
	}
	public Address getDestAddr() {
		return destAddr;
	}
	public byte getEsmClass() {
		return esmClass;
	}
	public byte getProtocolId() {
		return protocolId;
	}
	public byte getPriorityFlag() {
		return priorityFlag;
	}
	public byte getRegisteredDelivery() {
		return registeredDelivery;
	}
	public byte getReplaceIfPresentFlag() {
		return replaceIfPresentFlag;
	}
	public byte getDataCoding() {
		return dataCoding;
	}
	public byte getSmDefaultMsgId() {
		return smDefaultMsgId;
	}
	public short getSmLength() {
		return smLength;
	}

	public boolean hasUserMessageReference() {
		return userMessageReference.hasValue();
	}
	public boolean hasSourcePort() {
		return sourcePort.hasValue();
	}
	public boolean hasDestinationPort() {
		return destinationPort.hasValue();
	}
	public boolean hasSarMsgRefNum() {
		return sarMsgRefNum.hasValue();
	}
	public boolean hasSarTotalSegments() {
		return sarTotalSegments.hasValue();
	}
	public boolean hasSarSegmentSeqnum() {
		return sarSegmentSeqnum.hasValue();
	}
	public boolean hasPayloadType() {
		return payloadType.hasValue();
	}
	public boolean hasMessagePayload() {
		return messagePayload.hasValue();
	}
	public boolean hasPrivacyIndicator() {
		return privacyIndicator.hasValue();
	}
	public boolean hasCallbackNum() {
		return callbackNum.hasValue();
	}
	public boolean hasSourceSubaddress() {
		return sourceSubaddress.hasValue();
	}
	public boolean hasDestSubaddress() {
		return destSubaddress.hasValue();
	}
	public boolean hasUserResponseCode() {
		return userResponseCode.hasValue();
	}
	public boolean hasLanguageIndicator() {
		return languageIndicator.hasValue();
	}
	public boolean hasItsSessionInfo() {
		return itsSessionInfo.hasValue();
	}
	public boolean hasNetworkErrorCode() {
		return networkErrorCode.hasValue();
	}
	public boolean hasMessageState() {
		return messageState.hasValue();
	}
	public boolean hasReceiptedMessageId() {
		return receiptedMessageId.hasValue();
	}

	public void setUserMessageReference(short value) {
		userMessageReference.setValue(value);
	}
	public void setSourcePort(short value) {
		sourcePort.setValue(value);
	}
	public void setDestinationPort(short value) {
		destinationPort.setValue(value);
	}
	public void setSarMsgRefNum(short value) {
		sarMsgRefNum.setValue(value);
	}
	public void setSarTotalSegments(short value) throws IntegerOutOfRangeException {
		sarTotalSegments.setValue(value);
	}
	public void setSarSegmentSeqnum(short value) throws IntegerOutOfRangeException {
		sarSegmentSeqnum.setValue(value);
	}
	public void setPayloadType(byte value) {
		payloadType.setValue(value);
	}
	public void setMessagePayload(ByteBuffer value) {
		messagePayload.setValue(value);
	}
	public void setPrivacyIndicator(byte value) {
		privacyIndicator.setValue(value);
	}
	public void setCallbackNum(ByteBuffer value) {
		callbackNum.setValue(value);
	}
	public void setSourceSubaddress(ByteBuffer value) {
		sourceSubaddress.setValue(value);
	}
	public void setDestSubaddress(ByteBuffer value) {
		destSubaddress.setValue(value);
	}
	public void setUserResponseCode(byte value) {
		userResponseCode.setValue(value);
	}
	public void setLanguageIndicator(byte value) {
		languageIndicator.setValue(value);
	}
	public void setItsSessionInfo(short value) {
		itsSessionInfo.setValue(value);
	}
	public void setNetworkErrorCode(ByteBuffer value) {
		networkErrorCode.setValue(value);
	}
	public void setMessageState(byte value) {
		messageState.setValue(value);
	}
	public void setReceiptedMessageId(String value) throws WrongLengthException {
		receiptedMessageId.setValue(value);
	}

	public short getUserMessageReference() throws ValueNotSetException {
		return userMessageReference.getValue();
	}

	public short getSourcePort() throws ValueNotSetException {
		return sourcePort.getValue();
	}

	public short getDestinationPort() throws ValueNotSetException {
		return destinationPort.getValue();
	}

	public short getSarMsgRefNum() throws ValueNotSetException {
		return sarMsgRefNum.getValue();
	}

	public short getSarTotalSegments() throws ValueNotSetException {
		return sarTotalSegments.getValue();
	}

	public short getSarSegmentSeqnum() throws ValueNotSetException {
		return sarSegmentSeqnum.getValue();
	}

	public byte getPayloadType() throws ValueNotSetException {
		return payloadType.getValue();
	}

	public ByteBuffer getMessagePayload() throws ValueNotSetException {
		return messagePayload.getValue();
	}

	public byte getPrivacyIndicator() throws ValueNotSetException {
		return privacyIndicator.getValue();
	}

	public ByteBuffer callbackNum() throws ValueNotSetException {
		return callbackNum.getValue();
	}

	public ByteBuffer getSourceSubaddress() throws ValueNotSetException {
		return sourceSubaddress.getValue();
	}

	public ByteBuffer getDestSubaddress() throws ValueNotSetException {
		return destSubaddress.getValue();
	}

	public byte getUserResponseCode() throws ValueNotSetException {
		return userResponseCode.getValue();
	}

	public byte getLanguageIndicator() throws ValueNotSetException {
		return languageIndicator.getValue();
	}

	public short getItsSessionInfo() throws ValueNotSetException {
		return itsSessionInfo.getValue();
	}

	public ByteBuffer getNetworkErrorCode() throws ValueNotSetException {
		return networkErrorCode.getValue();
	}

	public byte getMessageState() throws ValueNotSetException {
		return messageState.getValue();
	}

	public String getReceiptedMessageId() throws ValueNotSetException {
		return receiptedMessageId.getValue();
	}

	public String debugString() {
		String dbgs = "(deliver: ";
		dbgs += super.debugString();
		dbgs += getSourceAddr().debugString();
		dbgs += " ";
		dbgs += getDestAddr().debugString();
		dbgs += " ";
		dbgs += shortMessage.debugString();
		dbgs += " ";
		dbgs += debugStringOptional();
		dbgs += ") ";
		return dbgs;
	}
}
/*
 * $Log: not supported by cvs2svn $
 * 
 * Old changelog:
 * 31-10-01 ticp@logica.com message length now stored and returned as short to
 *                          accomodate the lengths > 127 (they're stored as
 *                          negative values in byte vars)
 * 31-10-01 ticp@logica.com SAR fields now correctly return values > 127
 * 20-11-01 ticp@logica.com added support for multibyte string encoding
 *                          for short message
 */
