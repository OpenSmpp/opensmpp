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

import org.smpp.Data;
import org.smpp.util.*;
import org.smpp.pdu.Request;
import org.smpp.pdu.ValueNotSetException;
import org.smpp.pdu.tlv.*;

/**
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.1 $
 */
public class DataSM extends Request {
	// mandatory parameters
	private String serviceType = Data.DFLT_SRVTYPE;
	private Address sourceAddr = new Address(Data.SM_DATA_ADDR_LEN);
	private Address destAddr = new Address(Data.SM_DATA_ADDR_LEN);
	private byte esmClass = Data.DFLT_ESM_CLASS;
	private byte registeredDelivery = Data.DFLT_REG_DELIVERY;
	private byte dataCoding = Data.DFLT_DATA_CODING;

	// optional parameters
	private TLVShort userMessageReference = new TLVShort(Data.OPT_PAR_USER_MSG_REF);
	private TLVShort sourcePort = new TLVShort(Data.OPT_PAR_SRC_PORT);
	private TLVByte sourceAddrSubunit = new TLVByte(Data.OPT_PAR_SRC_ADDR_SUBUNIT);
	private TLVByte sourceNetworkType = new TLVByte(Data.OPT_PAR_SRC_NW_TYPE);
	private TLVByte sourceBearerType = new TLVByte(Data.OPT_PAR_SRC_BEAR_TYPE);
	private TLVByte sourceTelematicsId = new TLVByte(Data.OPT_PAR_SRC_TELE_ID);
	private TLVShort destinationPort = new TLVShort(Data.OPT_PAR_DST_PORT);
	private TLVByte destAddrSubunit = new TLVByte(Data.OPT_PAR_DST_ADDR_SUBUNIT);
	private TLVByte destNetworkType = new TLVByte(Data.OPT_PAR_DST_NW_TYPE);
	private TLVByte destBearerType = new TLVByte(Data.OPT_PAR_DST_BEAR_TYPE);
	private TLVShort destTelematicsId = new TLVShort(Data.OPT_PAR_DST_TELE_ID);
	private TLVShort sarMsgRefNum = new TLVShort(Data.OPT_PAR_SAR_MSG_REF_NUM);
	private TLVUByte sarTotalSegments = new TLVUByte(Data.OPT_PAR_SAR_TOT_SEG);
	private TLVUByte sarSegmentSeqnum = new TLVUByte(Data.OPT_PAR_SAR_SEG_SNUM);
	private TLVByte moreMsgsToSend = new TLVByte(Data.OPT_PAR_MORE_MSGS);
	private TLVInt qosTimeToLive = new TLVInt(Data.OPT_PAR_QOS_TIME_TO_LIVE);
	private TLVByte payloadType = new TLVByte(Data.OPT_PAR_PAYLOAD_TYPE);
	private TLVOctets messagePayload =
		new TLVOctets(Data.OPT_PAR_MSG_PAYLOAD, Data.OPT_PAR_MSG_PAYLOAD_MIN, Data.OPT_PAR_MSG_PAYLOAD_MAX);
	private TLVByte setDpf = new TLVByte(Data.OPT_PAR_SET_DPF);
	private TLVString receiptedMessageId =
		new TLVString(Data.OPT_PAR_RECP_MSG_ID, Data.OPT_PAR_RECP_MSG_ID_MIN, Data.OPT_PAR_RECP_MSG_ID_MAX);
	// 1-
	private TLVByte messageState = new TLVByte(Data.OPT_PAR_MSG_STATE);
	private TLVOctets networkErrorCode =
		new TLVOctets(Data.OPT_PAR_NW_ERR_CODE, Data.OPT_PAR_NW_ERR_CODE_MIN, Data.OPT_PAR_NW_ERR_CODE_MAX);
	// exactly 3
	private TLVByte privacyIndicator = new TLVByte(Data.OPT_PAR_PRIV_IND);
	private TLVOctets callbackNum =
		new TLVOctets(Data.OPT_PAR_CALLBACK_NUM, Data.OPT_PAR_CALLBACK_NUM_MIN, Data.OPT_PAR_CALLBACK_NUM_MAX);
	// 4-19
	private TLVByte callbackNumPresInd = new TLVByte(Data.OPT_PAR_CALLBACK_NUM_PRES_IND);
	private TLVOctets callbackNumAtag =
		new TLVOctets(
			Data.OPT_PAR_CALLBACK_NUM_ATAG,
			Data.OPT_PAR_CALLBACK_NUM_ATAG_MIN,
			Data.OPT_PAR_CALLBACK_NUM_ATAG_MAX);
	// 1-65
	private TLVOctets sourceSubaddress =
		new TLVOctets(Data.OPT_PAR_SRC_SUBADDR, Data.OPT_PAR_SRC_SUBADDR_MIN, Data.OPT_PAR_SRC_SUBADDR_MAX);
	// 2-23
	private TLVOctets destSubaddress =
		new TLVOctets(Data.OPT_PAR_DEST_SUBADDR, Data.OPT_PAR_DEST_SUBADDR_MIN, Data.OPT_PAR_DEST_SUBADDR_MAX);
	private TLVByte userResponseCode = new TLVByte(Data.OPT_PAR_USER_RESP_CODE);
	private TLVByte displayTime = new TLVByte(Data.OPT_PAR_DISPLAY_TIME);
	private TLVShort smsSignal = new TLVShort(Data.OPT_PAR_SMS_SIGNAL);
	private TLVByte msValidity = new TLVByte(Data.OPT_PAR_MS_VALIDITY);
	private TLVByte msMsgWaitFacilities = new TLVByte(Data.OPT_PAR_MSG_WAIT); // bit mask
	private TLVByte numberOfMessages = new TLVByte(Data.OPT_PAR_NUM_MSGS);
	private TLVEmpty alertOnMsgDelivery = new TLVEmpty(Data.OPT_PAR_ALERT_ON_MSG_DELIVERY);
	private TLVByte languageIndicator = new TLVByte(Data.OPT_PAR_LANG_IND);
	private TLVByte itsReplyType = new TLVByte(Data.OPT_PAR_ITS_REPLY_TYPE);
	private TLVShort itsSessionInfo = new TLVShort(Data.OPT_PAR_ITS_SESSION_INFO);

	public DataSM() {
		super(Data.DATA_SM);

		registerOptional(userMessageReference);
		registerOptional(sourcePort);
		registerOptional(sourceAddrSubunit);
		registerOptional(sourceNetworkType);
		registerOptional(sourceBearerType);
		registerOptional(sourceTelematicsId);
		registerOptional(destinationPort);
		registerOptional(destAddrSubunit);
		registerOptional(destNetworkType);
		registerOptional(destBearerType);
		registerOptional(destTelematicsId);
		registerOptional(sarMsgRefNum);
		registerOptional(sarTotalSegments);
		registerOptional(sarSegmentSeqnum);
		registerOptional(moreMsgsToSend);
		registerOptional(qosTimeToLive);
		registerOptional(payloadType);
		registerOptional(messagePayload);
		registerOptional(setDpf);
		registerOptional(receiptedMessageId);
		registerOptional(messageState);
		registerOptional(networkErrorCode);
		registerOptional(privacyIndicator);
		registerOptional(callbackNum);
		registerOptional(callbackNumPresInd);
		registerOptional(callbackNumAtag);
		registerOptional(sourceSubaddress);
		registerOptional(destSubaddress);
		registerOptional(userResponseCode);
		registerOptional(displayTime);
		registerOptional(smsSignal);
		registerOptional(msValidity);
		registerOptional(msMsgWaitFacilities);
		registerOptional(numberOfMessages);
		registerOptional(alertOnMsgDelivery);
		registerOptional(languageIndicator);
		registerOptional(itsReplyType);
		registerOptional(itsSessionInfo);
	}

	protected Response createResponse() {
		return new DataSMResp();
	}

	public void setBody(ByteBuffer buffer)
		throws NotEnoughDataInByteBufferException, TerminatingZeroNotFoundException, PDUException {
		setServiceType(buffer.removeCString());
		sourceAddr.setData(buffer);
		destAddr.setData(buffer);
		setEsmClass(buffer.removeByte());
		setRegisteredDelivery(buffer.removeByte());
		setDataCoding(buffer.removeByte());
	}

	public ByteBuffer getBody() {
		ByteBuffer buffer = new ByteBuffer();
		buffer.appendCString(getServiceType());
		buffer.appendBuffer(getSourceAddr().getData());
		buffer.appendBuffer(getDestAddr().getData());
		buffer.appendByte(getEsmClass());
		buffer.appendByte(getRegisteredDelivery());
		buffer.appendByte(getDataCoding());
		return buffer;
	}

	public void setServiceType(String value) throws WrongLengthOfStringException {
		checkCString(value, Data.SM_SRVTYPE_LEN);
		serviceType = value;
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
	public void setRegisteredDelivery(byte value) {
		registeredDelivery = value;
	}
	public void setDataCoding(byte value) {
		dataCoding = value;
	}

	public String getServiceType() {
		return serviceType;
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
	public byte getRegisteredDelivery() {
		return registeredDelivery;
	}
	public byte getDataCoding() {
		return dataCoding;
	}

	public boolean hasUserMessageReference() {
		return userMessageReference.hasValue();
	}
	public boolean hasSourcePort() {
		return sourcePort.hasValue();
	}
	public boolean hasSourceAddrSubunit() {
		return sourceAddrSubunit.hasValue();
	}
	public boolean hasSourceNetworkType() {
		return sourceNetworkType.hasValue();
	}
	public boolean hasSourceBearerType() {
		return sourceBearerType.hasValue();
	}
	public boolean hasSourceTelematicsId() {
		return sourceTelematicsId.hasValue();
	}
	public boolean hasDestinationPort() {
		return destinationPort.hasValue();
	}
	public boolean hasDestAddrSubunit() {
		return destAddrSubunit.hasValue();
	}
	public boolean hasDestNetworkType() {
		return destNetworkType.hasValue();
	}
	public boolean hasDestBearerType() {
		return destBearerType.hasValue();
	}
	public boolean hasDestTelematicsId() {
		return destTelematicsId.hasValue();
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
	public boolean hasMoreMsgsToSend() {
		return moreMsgsToSend.hasValue();
	}
	public boolean hasQosTimeToLive() {
		return qosTimeToLive.hasValue();
	}
	public boolean hasPayloadType() {
		return payloadType.hasValue();
	}
	public boolean hasMessagePayload() {
		return messagePayload.hasValue();
	}
	public boolean hasSetDpf() {
		return setDpf.hasValue();
	}
	public boolean hasReceiptedMessageId() {
		return receiptedMessageId.hasValue();
	}
	public boolean hasMessageState() {
		return messageState.hasValue();
	}
	public boolean hasNetworkErrorCode() {
		return networkErrorCode.hasValue();
	}
	public boolean hasPrivacyIndicator() {
		return privacyIndicator.hasValue();
	}
	public boolean hasCallbackNum() {
		return callbackNum.hasValue();
	}
	public boolean hasCallbackNumPresInd() {
		return callbackNumPresInd.hasValue();
	}
	public boolean hasCallbackNumAtag() {
		return callbackNumAtag.hasValue();
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
	public boolean hasDisplayTime() {
		return displayTime.hasValue();
	}
	public boolean hasSmsSignal() {
		return smsSignal.hasValue();
	}
	public boolean hasMsValidity() {
		return msValidity.hasValue();
	}
	public boolean hasMsMsgWaitFacilities() {
		return msMsgWaitFacilities.hasValue();
	}
	public boolean hasNumberOfMessages() {
		return numberOfMessages.hasValue();
	}
	public boolean hasAlertOnMsgDelivery() {
		return alertOnMsgDelivery.hasValue();
	}
	public boolean hasLanguageIndicator() {
		return languageIndicator.hasValue();
	}
	public boolean hasItsReplyType() {
		return itsReplyType.hasValue();
	}
	public boolean hasItsSessionInfo() {
		return itsSessionInfo.hasValue();
	}

	public void setUserMessageReference(short value) {
		userMessageReference.setValue(value);
	}
	public void setSourcePort(short value) {
		sourcePort.setValue(value);
	}
	public void setSourceAddrSubunit(byte value) {
		sourceAddrSubunit.setValue(value);
	}
	public void setSourceNetworkType(byte value) {
		sourceNetworkType.setValue(value);
	}
	public void setSourceBearerType(byte value) {
		sourceBearerType.setValue(value);
	}
	public void setSourceTelematicsId(byte value) {
		sourceTelematicsId.setValue(value);
	}
	public void setDestinationPort(short value) {
		destinationPort.setValue(value);
	}
	public void setDestAddrSubunit(byte value) {
		destAddrSubunit.setValue(value);
	}
	public void setDestNetworkType(byte value) {
		destNetworkType.setValue(value);
	}
	public void setDestBearerType(byte value) {
		destBearerType.setValue(value);
	}
	public void setDestTelematicsId(short value) {
		destTelematicsId.setValue(value);
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
	public void setMoreMsgsToSend(byte value) {
		moreMsgsToSend.setValue(value);
	}
	public void setQosTimeToLive(int value) {
		qosTimeToLive.setValue(value);
	}
	public void setPayloadType(byte value) {
		payloadType.setValue(value);
	}
	public void setMessagePayload(ByteBuffer value) {
		messagePayload.setValue(value);
	}
	public void setSetDpf(byte value) {
		setDpf.setValue(value);
	}
	public void setReceiptedMessageId(String value) throws WrongLengthException {
		receiptedMessageId.setValue(value);
	}
	public void setMessageState(byte value) {
		messageState.setValue(value);
	}
	public void setNetworkErrorCode(ByteBuffer value) {
		networkErrorCode.setValue(value);
	}
	public void setPrivacyIndicator(byte value) {
		privacyIndicator.setValue(value);
	}
	public void setCallbackNum(ByteBuffer value) {
		callbackNum.setValue(value);
	}
	public void setCallbackNumPresInd(byte value) {
		callbackNumPresInd.setValue(value);
	}
	public void setCallbackNumAtag(ByteBuffer value) {
		callbackNumAtag.setValue(value);
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
	public void setDisplayTime(byte value) {
		displayTime.setValue(value);
	}
	public void setSmsSignal(short value) {
		smsSignal.setValue(value);
	}
	public void setMsValidity(byte value) {
		msValidity.setValue(value);
	}
	public void setMsMsgWaitFacilities(byte value) {
		msMsgWaitFacilities.setValue(value);
	}
	public void setNumberOfMessages(byte value) {
		numberOfMessages.setValue(value);
	}
	public void setAlertOnMsgDelivery(boolean value) {
		alertOnMsgDelivery.setValue(value);
	}
	public void setLanguageIndicator(byte value) {
		languageIndicator.setValue(value);
	}
	public void setItsReplyType(byte value) {
		itsReplyType.setValue(value);
	}
	public void setItsSessionInfo(short value) {
		itsSessionInfo.setValue(value);
	}

	public short getUserMessageReference() throws ValueNotSetException {
		return userMessageReference.getValue();
	}

	public short getSourcePort() throws ValueNotSetException {
		return sourcePort.getValue();
	}

	public byte getSourceAddrSubunit() throws ValueNotSetException {
		return sourceAddrSubunit.getValue();
	}

	public byte getSourceNetworkType() throws ValueNotSetException {
		return sourceNetworkType.getValue();
	}

	public byte getSourceBearerType() throws ValueNotSetException {
		return sourceBearerType.getValue();
	}

	public byte getSourceTelematicsId() throws ValueNotSetException {
		return sourceTelematicsId.getValue();
	}

	public short getDestinationPort() throws ValueNotSetException {
		return destinationPort.getValue();
	}

	public byte getDestAddrSubunit() throws ValueNotSetException {
		return destAddrSubunit.getValue();
	}

	public byte getDestNetworkType() throws ValueNotSetException {
		return destNetworkType.getValue();
	}

	public byte getDestBearerType() throws ValueNotSetException {
		return destBearerType.getValue();
	}

	public short getDestTelematicsId() throws ValueNotSetException {
		return destTelematicsId.getValue();
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

	public byte getMoreMsgsToSend() throws ValueNotSetException {
		return moreMsgsToSend.getValue();
	}

	public int getQosTimeToLive() throws ValueNotSetException {
		return qosTimeToLive.getValue();
	}

	public byte getPayloadType() throws ValueNotSetException {
		return payloadType.getValue();
	}

	public ByteBuffer getMessagePayload() throws ValueNotSetException {
		return messagePayload.getValue();
	}

	public byte getSetDpf() throws ValueNotSetException {
		return setDpf.getValue();
	}

	public String getReceiptedMessageId() throws ValueNotSetException {
		return receiptedMessageId.getValue();
	}

	public byte getMessageState() throws ValueNotSetException {
		return messageState.getValue();
	}

	public ByteBuffer getNetworkErrorCode() throws ValueNotSetException {
		return networkErrorCode.getValue();
	}

	public byte getPrivacyIndicator() throws ValueNotSetException {
		return privacyIndicator.getValue();
	}

	public ByteBuffer callbackNum() throws ValueNotSetException {
		return callbackNum.getValue();
	}

	public byte getCallbackNumPresInd() throws ValueNotSetException {
		return callbackNumPresInd.getValue();
	}

	public ByteBuffer getCallbackNumAtag() throws ValueNotSetException {
		return callbackNumAtag.getValue();
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

	public byte getDisplayTime() throws ValueNotSetException {
		return displayTime.getValue();
	}

	public short getSmsSignal() throws ValueNotSetException {
		return smsSignal.getValue();
	}

	public byte getMsValidity() throws ValueNotSetException {
		return msValidity.getValue();
	}

	public byte getMsMsgWaitFacilities() throws ValueNotSetException {
		return msMsgWaitFacilities.getValue();
	}

	public byte getNumberOfMessages() throws ValueNotSetException {
		return numberOfMessages.getValue();
	}

	public boolean getAlertOnMsgDelivery() throws ValueNotSetException {
		return alertOnMsgDelivery.getValue();
	}

	public byte getLanguageIndicator() throws ValueNotSetException {
		return languageIndicator.getValue();
	}

	public byte getItsReplyType() throws ValueNotSetException {
		return itsReplyType.getValue();
	}

	public short getItsSessionInfo() throws ValueNotSetException {
		return itsSessionInfo.getValue();
	}

	public String debugString() {
		String dbgs = "(data: ";
		dbgs += super.debugString();
		dbgs += getSourceAddr().debugString();
		dbgs += " ";
		dbgs += getDestAddr().debugString();
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
 * 10-10-01 ticp@logica.com max address lengths fixed according smpp spec
 * 31-10-01 ticp@logica.com SAR fields now correctly return values > 127
 */
