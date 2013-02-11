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

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.smpp.Data;
import org.smpp.pdu.tlv.TLV;
import org.smpp.pdu.tlv.TLVException;
import org.smpp.pdu.tlv.TLVOctets;
import org.smpp.util.*;

/**
 * Class <code>PDU</code> is abstract base class for all classes which
 * represent a PDU. It contains methods for manipulating PDU header,
 * checking validity of PDU, automatic parsing and generation of optional
 * part of PDU, methods for creating instance of proper class representing
 * certain PDU based only in command id, methods for detection if the 
 * PDU is request or response PDU, automatic sequence number
 * assignment, etc. It also implements <code>setData</code> and
 * <code>getData</code> methods as the header and optional params
 * parsing and composition is the same for all PDUs. The derived
 * classes on turn implement functions <code>setBody</code> and
 * <code>getBody</code>.
 * <p>
 * The <code>PDU</code> has two descendants, <code>Request</code> and
 * <code>Response</code>, which serve as a base classes for concrete
 * PDU classes like SubmitSM, SubmitSMResp etc.
 *
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.9 $
 */
public abstract class PDU extends ByteData {
	/**
	 * This constant indicates that parsing of the buffer failed
	 * parsing of the header of PDU.
	 *
	 * @see #setData(ByteBuffer)
	 * @see #valid
	 */
	public static final byte VALID_NONE = 0;

	/**
	 * This constant indicates that parsing of the buffer passed
	 * parsing of the header of PDU but failed parsing of mandatory
	 * part of body.
	 *
	 * @see #setData(ByteBuffer)
	 * @see #valid
	 */
	public static final byte VALID_HEADER = 1;

	/**
	 * This constant indicates that parsing of the buffer passed
	 * parsing of the mandatory part of body of PDU but failed parsing
	 * of optional parameters.
	 *
	 * @see #setData(ByteBuffer)
	 * @see #valid
	 */
	public static final byte VALID_BODY = 2;

	/**
	 * This constant indicates that parsing of the buffer passed
	 * all parts of the PDU, i.e. headet, mandator and optional
	 * parameters.
	 *
	 * @see #setData(ByteBuffer)
	 * @see #valid
	 */
	public static final byte VALID_ALL = 3;

	/**
	 * This is counter of sequence numbers. Each time the method
	 * <code>assignSequenceNumber</code> is called, this counter
	 * is increased and the it's value is assigned as a sequence number
	 * of th PDU.
	 *
	 * @see #assignSequenceNumber()
	 */
	private static int sequenceNumber = 0;

	/**
	 * Indicates that the sequence number has been changed either by setting
	 * by method <code>setSequenceNumber(int) or by reading from buffer by
	 * method <code>setHeader</code>.
	 *
	 * @see #setSequenceNumber(int)
	 * @see #assignSequenceNumber()
	 */
	private boolean sequenceNumberChanged = false;

	/**
	 * This is the header of the PDU. It's only created when necessary.
	 * <code>PDU</code> class implements accessor methdos for setting
	 * and getting parameters of header like comand id, sequence
	 * number etc.
	 *
	 * @see #checkHeader()
	 * @see #setHeader(ByteBuffer)
	 * @see #getHeader()
	 */
	private PDUHeader header = null;

	/**
	 * This contains all optional parameters defined for particular
	 * concrete PDU. E.g. for submit_sm class <code>SubmitSM</code>
	 * puts  here all it's possible optional parameters. It is used
	 * to build a byte buffer from optional parameters as well as
	 * fill them from a buffer.
	 *
	 * @see #registerOptional(TLV)
	 * @see TLV
	 */
	private Vector<TLV> optionalParameters = new Vector<TLV>(10, 2);

	/**
	 * Contains optional parameters which aren't defined in the SMPP spec.
	 *
	 * @see #setExtraOptional(TLV)
	 * @see #setExtraOptional(short,ByteBuffer)
	 * @see #getExtraOptional(short)
	 * @see #registerExtraOptional(TLV)
	 */
	private Vector<TLV> extraOptionalParameters = new Vector<TLV>(1, 1);

	/**
	 * This indicates what stage was reached when parsing byte buffer
	 * in <code>setData</code> method.
	 *
	 * @see #VALID_NONE
	 * @see #VALID_HEADER
	 * @see #VALID_BODY
	 * @see #VALID_ALL
	 * @see #setData(ByteBuffer)
	 */
	private byte valid = VALID_ALL;

	/**
	 * Application developers can attach application specific data to an instance
	 * of PDU or derived class. This facility can be used to carry data
	 * over different components  of the application in the PDU without explicit
	 * development of PDU to data mapping functionality.
	 * Typical use would be attaching an information about data from which a
	 * Request was created and after receiving a Response to that Request
	 * these data can be used for controling the proper reaction to the Response.
	 * If you use string gey, be carefull and choose a key which would be expected
	 * to be unique, fo example your class name qualified with full package name
	 * and with additional key description name appended.
	 * @see #setApplicationSpecificInfo(Object,Object)
	 * @see #getApplicationSpecificInfo(Object)
	 * @see #removeApplicationSpecificInfo(Object)
	 */
	private Dictionary<Object, Object> applicationSpecificInfo = null;

	/**
	 * Default constructor, what else.
	 */
	public PDU() {
		super();
	}

	/**
	 * Initialises PDU with given command id. Derived classes should
	 * provide correct command id for their type, e.g. SubmitSM should
	 * provide <code>Data.SUBMIT_SM</code>, which is equal to 4 (as defined
	 * in SMPP 3.4 spec.)
	 *
	 * @param commandId the numerical id of the PDU as specified in SMPP
	 *                  specification
	 */
	public PDU(int commandId) {
		super();
		checkHeader();
		setCommandId(commandId);
	}

	/**
	 * Default method for seting mandatory parameters of the PDU.
	 * Derived classes should overwrite this method if they want
	 * to fill their member variables with data from the binary
	 * data buffer.
	 *
	 * @param buffer the buffer with the PDU's data as received from SMSC
	 * @see #setData(ByteBuffer)
	 */
	public void setBody(ByteBuffer buffer)
		throws NotEnoughDataInByteBufferException, TerminatingZeroNotFoundException, PDUException {
	}

	/**
	 * Default method for composing binary representation of
	 * the mandatory part of the PDU. Derived classes should overwrite this
	 * method with composition of buffer from their member variables.
	 *
	 * @see #getData()
	 */
	public ByteBuffer getBody() throws ValueNotSetException {
		return null;
	}

	/**
	 * This method indicates that the object represents PDU which can
	 * and should be responded to. For example for <code>SubmitSM</code>
	 * class this should return <code>true</code>, while for
	 * <code>SubmitSMResp</code> or <code>AlertNotification</code> classes
	 * this should return <code>false</code>. This method is overwritten in
	 * class <code>Request</code> as most "request" PDUs have response
	 * counterparts. (Exception to this rule is above mentioned
	 * <code>AlertNotification</code> which doesn't have response.)
	 * @return if the PDU can have a response
	 * @see Request#canResponse()
	 * @see Response#canResponse()
	 * @see AlertNotification#canResponse()
	 */
	public boolean canResponse() {
		return false;
	}

	/**
	 * Returns if the object represents PDU which is a request.
	 * E.g. classes derived from <code>Request</code> class return
	 * <code>true</code>.
	 * @return if the PDU represents request
	 */
	public abstract boolean isRequest();

	/**
	 * Returns if the object represents PDU which is response.
	 * E.g. classes derived from <code>Response</code> class return
	 * <code>true</code>.
	 * @return if the PDU represents response
	 */
	public abstract boolean isResponse();

	/**
	 * Assigns newly generated sequence number if the sequence number
	 * hasn't been assigned yet. Doesn't have any effect if the sequence
	 * number was already assigned.
	 *
	 * @see #assignSequenceNumber(boolean)
	 * @see #setSequenceNumber(int)
	 * @see #setHeader(ByteBuffer)
	 */
	public void assignSequenceNumber() {
		assignSequenceNumber(false);
	}

	/**
	 * Assigns newly generated sequence number. If the sequence
	 * number was previously set by <code>setSequenceNumber</code> method or
	 * from byte buffer in <code>setHeader</code>, this method only assigns the
	 * number if the parameter <code>always</code> is true.
	 *
	 * @param always if the number has to be assigned even if it was already assigned
	 * @see #setSequenceNumber(int)
	 * @see #setHeader(ByteBuffer)
	 */
	public void assignSequenceNumber(boolean always) {
		if ((!sequenceNumberChanged) || always) {
			setSequenceNumber(nextSequenceNumber());
		}
	}

	/**
	 * Method created to enforce synchronization.
	 * 
	 * @return the next sequence number.
	 */
	static private synchronized int nextSequenceNumber()
	{
		// & 0x7FFFFFFF: cater for integer overflow
		// Allowed range is 0x01 to 0x7FFFFFFF. This
		// will still result in a single invalid value
		// of 0x00 every ~2 billion PDUs (not too bad):
		return (++sequenceNumber) & 0x7FFFFFFF;
	}


	/**
	 * If the sequence number was previously set to a value, this function
	 * resets that fact. I.e. if the PDU is re-used for another say submit,
	 * then after calling of this function a new sequence number will
	 * assigned to it the PDU despite of the fact that there was another one
	 * assigned to it before.
	 */
	public void resetSequenceNumber() {
		setSequenceNumber(0);
		sequenceNumberChanged = false;
	}

	/**
	 * Parses the binary buffer to get the PDUs header, fields from mandatory
	 * part and fields from the optional part.<br>
	 * The header and optional part are parsed common way for all PDUs
	 * using functions <code>setHeader</code> and <code>setOptionalBody</code>
	 * the mandatory body is parsed by the derived classes in
	 * <code>setBody</code> function. If parsing throws an exception, the PDU's
	 * <code>getValid</code> function returns the phase which was correct
	 * last.<br>
	 * The buffer can contain more than one PDU, then only one PDU is taken
	 * from the buffer and the rest remains unaltered.
	 * @param buffer the buffer containg the PDU binary data which are source
	 *               for the content of the fields of this PDU
	 * @see #setHeader(ByteBuffer)
	 * @see #setBody(ByteBuffer)
	 * @see #setOptionalBody(ByteBuffer)
	 * @see #getValid()
	 * @see #getData()
	 */
	public void setData(ByteBuffer buffer) throws InvalidPDUException, PDUException {
		int initialBufLen = buffer.length();
		try {
			setValid(VALID_NONE);
			// first try read header
			if (buffer.length() < Data.PDU_HEADER_SIZE) {
				if (debug.active(DPDU)) {
					debug.write(DPDU, "PDU.setData() not enough data for header in the buffer " + buffer.getHexDump());
				}
			}

			// get the header from the buffer
			ByteBuffer headerBuf = buffer.removeBytes(Data.PDU_HEADER_SIZE);
			if (debug.active(DPDU)) {
				debug.write(DPDU, "PDU.setData() parsing header " + headerBuf.getHexDump());
			}
			setHeader(headerBuf);
			setValid(VALID_HEADER);
			// now read pdu's body for hex dump
			if (debug.active(DPDU)) {
				if (getCommandLength() > Data.PDU_HEADER_SIZE) {
					ByteBuffer tempBodyBuf = buffer.readBytes(getCommandLength() - Data.PDU_HEADER_SIZE);
					debug.write(DPDU, "PDU.setData() parsing body " + tempBodyBuf.getHexDump());
				} else {
					debug.write(DPDU, "PDU.setData() no data for body");
				}
			}
			// parse the body
			setBody(buffer);
			setValid(VALID_BODY);
			if ((initialBufLen - buffer.length()) < getCommandLength()) {
				// i.e. parsed less than indicated by command length =>
				// must have optional parameters
				int optionalLength = getCommandLength() + buffer.length() - initialBufLen;
				try {
					debug.write(DPDU, "have " + optionalLength + " bytes left.");
					ByteBuffer optionalBody = buffer.removeBuffer(optionalLength);
					setOptionalBody(optionalBody);
				} catch(Exception e) {
					debug.write(DPDU, "Parsing optional parameters failed: " + e.getMessage());
				}
			}
			setValid(VALID_ALL);
		} catch (NotEnoughDataInByteBufferException e) {
			throw new InvalidPDUException(this, e);
		} catch (TerminatingZeroNotFoundException e) {
			throw new InvalidPDUException(this, e);
		} catch (PDUException e) {
			e.setPDU(this);
			throw e;
		} catch (Exception e) {
			// transform generic exception into InvalidPDUException
			throw new InvalidPDUException(this, e);
		}
		if (buffer.length() != (initialBufLen - getCommandLength())) {
			// i.e. we've parsed out different number of bytes
			// than is specified by command length in the pdu's header
			throw new InvalidPDUException(this, "The parsed size of the message is not equal to command_length.");
		}
	}

	/**
	 * Construct the binary PDU for sending to SMSC.
	 * First creates the mandatory part using <code>getBody</code> method,
	 * which has to be implemented in the derived classes (if they 
	 * represent PDU with body), then creates the optional part of the PDU
	 * using <code>getOptionalBody</code>, which is common for all PDUs.
	 * Calculates the size of the PDU and then creates the header using
	 * <code>getHeader</code> and returns the full binary PDU.
	 * @see #getBody()
	 * @see #getOptionalBody(Vector)
	 * @see #getHeader()
	 * @see #setData(ByteBuffer)
	 */
	public ByteBuffer getData() throws ValueNotSetException {
		// prepare all body
		ByteBuffer bodyBuf = new ByteBuffer();
		bodyBuf.appendBuffer(getBody());
		bodyBuf.appendBuffer(getOptionalBody());
		// get its size and add size of the header; set the result as length
		setCommandLength(bodyBuf.length() + Data.PDU_HEADER_SIZE);
		ByteBuffer pduBuf = getHeader();
		pduBuf.appendBuffer(bodyBuf);
		if (debug.active(DPDU)) {
			debug.write(DPDU, "PDU.getData() build up data " + pduBuf.getHexDump());
		}
		return pduBuf;
	}

	/** Sets if the PDU contains correctly formated data.  */
	public void setValid(byte valid) {
		this.valid = valid;
	}

	/** Returns if the PDU contains correctly formated data. */
	public byte getValid() {
		return valid;
	}

	/**
	 * Returns if the parsing of the binary PDU data successfully parsed
	 * complete PDU.
	 */
	public boolean isValid() {
		return getValid() == VALID_ALL;
	}

	/**
	 * Returns if the parsing of the binary PDU data ended before parsing
	 * of the header.
	 */
	public boolean isInvalid() {
		return getValid() == VALID_NONE;
	}

	/** Returns if at least the header of the binary PDU data was correct. */
	public boolean isHeaderValid() {
		return getValid() >= VALID_HEADER;
	}

	/**
	 * Parses the header from the buffer.
	 * Also sets the flag that the sequence number was changed as it was read
	 * from the binary buffer.
	 * @param buffer the buffer which contains the PDU
	 * @see PDUHeader#setData(ByteBuffer)
	 */
	private void setHeader(ByteBuffer buffer) throws NotEnoughDataInByteBufferException {
		checkHeader();
		header.setData(buffer);
		sequenceNumberChanged = true;
	}

	/** Creates the binary PDU header from the header fields of the PDU. */
	private ByteBuffer getHeader() {
		checkHeader();
		return header.getData();
	}

	/**
	 * Parses the binary buffer and obtains all optional parameters
	 * which the buffer contains, sets the optional parameter fields.
	 * The optional parameter fields which the PDU can contain must be
	 * registered by the derived class using <code>registerOptional</code>.
	 * Or there can be extra optional parameters with application/smsc specific tags,
	 * which aren't defined in the SMPP specification.
	 * The optional parameters defined in SMPP are accessible using appropriate
	 * getter functions in the derived PDU classes, the extra optional
	 * parameters are accessible with generic function <code>getExtraOptional</code>.
	 * The buffer can't contain another data then the optional parameters.
	 *
	 * @param buffer the buffer with the optional parameters
	 * @exception UnexpectedOptionalParameterException if the optional
	 *            parameter read from the buffer cna't be contained
	 *            int this PDU
	 * @see #getOptionalBody()
	 * @see #getExtraOptional(short)
	 * @see TLV
	 */
	private void setOptionalBody(ByteBuffer buffer)
		throws NotEnoughDataInByteBufferException, UnexpectedOptionalParameterException, TLVException {
		short tag;
		short length;
		ByteBuffer tlvHeader;
		ByteBuffer tlvBuf;
		TLV tlv = null;
		while (buffer.length() > 0) {
			// we prepare buffer with one parameter
			tlvHeader = buffer.readBytes(Data.TLV_HEADER_SIZE);
			tag = tlvHeader.removeShort();
			tlv = findOptional(optionalParameters, tag);
			if (tlv == null) {
				// ok, got extra optional parameter not defined in SMPP spec
				// will keep it as octets
				tlv = new TLVOctets(tag);
				registerExtraOptional(tlv);
			}
			length = tlvHeader.removeShort();
			tlvBuf = buffer.removeBuffer(Data.TLV_HEADER_SIZE + length);
			tlv.setData(tlvBuf);
		}
	}

	/**
	 * Creates buffer with all the optional parameters which have set
	 * their value, both from the optional parameters defined in SMPP
	 * and extra optional parameters.
	 * @see #setOptionalBody(ByteBuffer)
	 * @see TLV#hasValue()
	 * @see TLV#getData()
	 * @see TLV
	 */
	private ByteBuffer getOptionalBody() throws ValueNotSetException {
		ByteBuffer optBody = new ByteBuffer();
		optBody.appendBuffer(getOptionalBody(optionalParameters));
		optBody.appendBuffer(getOptionalBody(extraOptionalParameters));
		return optBody;
	}

	/**
	 * Creates buffer with all the optional parameters contained in 
	 * the <code>optionalParameters</code> list which have set
	 * their value. For getting data of the optional parameter calls
	 * a method <code>getData</code> of the <code>TLV</code> class.
	 * @see #setOptionalBody(ByteBuffer)
	 * @see TLV#hasValue()
	 * @see TLV#getData()
	 * @see TLV
	 */
	private ByteBuffer getOptionalBody(Vector<TLV> optionalParameters) throws ValueNotSetException {
		ByteBuffer optBody = new ByteBuffer();
		int size = optionalParameters.size();
		TLV tlv = null;
		for (int i = 0; i < size; i++) {
			tlv = optionalParameters.get(i);
			if ((tlv != null) && tlv.hasValue()) {
				optBody.appendBuffer(tlv.getData());
			}
		}
		return optBody;
	}

	/**
	 * Registeres a TLV as an optional parameter which can be containd in
	 * the PDU. Derived classes are expected that they will define appropriate
	 * TLVs and register them using this method. Only registered TLVs
	 * can be read as optional parameters from binary PDU received from SMSC.
	 * @param tlv the TLV to be registered as an optional parameter
	 * @see TLV
	 */
	protected void registerOptional(TLV tlv) {
		if (tlv != null) {
			optionalParameters.add(tlv);
		}
	}

	/**
	 * Registeres a TLV as an extra optional parameter which can be contained in
	 * the PDU. Extra optional parameter is TLV not defined in SMPP spec.
	 * The reason for this method is that if you know what type and size
	 * certain extra optional parameter has to be, then you can create an instance
	 * of appropriate TLV class, e.g. <code>TLVInt</code> class and register it as
	 * the carrying TLV instead of using the generic <code>TLVOctets</code> class.
	 * @param tlv the TLV to be registered as an extra optional parameter
	 * @see TLV
	 */
	protected void registerExtraOptional(TLV tlv) {
		if (tlv != null) {
			extraOptionalParameters.add(tlv);
		}
	}

	/**
	 * Searches for registered or extra TLV with the given TLV tag.
	 * Returns the found TLV or null if not found. Used when parsing
	 * the optional part of the binary PDU data.
	 * @param tag the tag of the TLV required
	 * @return the found TLV
	 * @see #setOptionalBody(ByteBuffer)
	 * @see #registerOptional(TLV)
	 * @see #registerExtraOptional(TLV)
	 * @see TLV
	 */
	private TLV findOptional(Vector<TLV> optionalParameters, short tag) {
		int size = optionalParameters.size();
		TLV tlv = null;
		for (int i = 0; i < size; i++) {
			tlv = optionalParameters.get(i);
			if (tlv != null) {
				if (tlv.getTag() == tag) {
					return tlv;
				}
			}
		}
		return null;
	}

	/**
	 * Replaces the TLV in the extra optional parameters list with the
	 * new tlv provided as a parameter. If the tlv doesn't exist in the list,
	 * adds it to the list.
	 */
	private void replaceExtraOptional(TLV tlv) {
		int size = extraOptionalParameters.size();
		TLV existing = null;
		short tlvTag = tlv.getTag();
		for (int i = 0; i < size; i++) {
			existing = extraOptionalParameters.get(i);
			if ((existing != null) && (existing.getTag() == tlvTag)) {
				extraOptionalParameters.set(i, tlv);
				return;
			}
		}
		registerExtraOptional(tlv); // the optional param wasn't found
	}

	/**
	 * Sets the extra optional parameter.
	 */
	public void setExtraOptional(TLV tlv) {
		replaceExtraOptional(tlv);
	}

	/**
	 * Creates new generic extra optional parameter with tag and data provided;
	 * uses TLVOctets for the parameter.
	 */
	public void setExtraOptional(short tag, ByteBuffer data) throws TLVException {
		TLV tlv = new TLVOctets(tag, data);
		setExtraOptional(tlv);
	}

	/**
	 * Finds and returns extra optional parameter with the provided 
	 * tag; if not found returns null.
	 */
	public TLV getExtraOptional(short tag) {
		TLV tlv = findOptional(extraOptionalParameters, tag);
		return tlv;
	}

	/** Checks if the header field is null and if not, creates it. */
	private void checkHeader() {
		if (header == null) {
			header = new PDUHeader();
		}
	}

	/**
	 * Returns the length of this PDU.
	 * The length is only valid if you probe the PDU which was parsed from
	 * binary data received from SMSC. If you created the PDU and filled in the
	 * filds of the PDU, the length returned by this function will be very
	 * likely incorrect.
	 */
	public int getCommandLength() {
		checkHeader();
		return header.getCommandLength();
	}

	/**
	 * Returns the command id of the PDU object.
	 * The command id can be either read from the binary data received from SMSC
	 * or can be set by derived class as the command id of the PDU which is
	 * represented by the class.
	 */
	public int getCommandId() {
		checkHeader();
		return header.getCommandId();
	}

	/** Returns the command status of the PDU. */
	public int getCommandStatus() {
		checkHeader();
		return header.getCommandStatus();
	}

	/**
	 * Returns the sequence number of the PDU.
	 * If the PDU is created by yourself and not parsed from binary data
	 * received from SMSC, then the sequence number will be very likely
	 * incorrect until you didn't set it previously by calling function
	 * <code>assignSequenceNumber</code> or <code>setSequenceNumber</code>.
	 * @see #assignSequenceNumber()
	 * @see #setSequenceNumber(int)
	 */
	public int getSequenceNumber() {
		checkHeader();
		return header.getSequenceNumber();
	}

	/**
	 * Sets the length of the PDU. Don't do it manually, it won't have
	 * any wffect.
	 */
	public void setCommandLength(int cmdLen) {
		checkHeader();
		header.setCommandLength(cmdLen);
	}

	/** Sets the command id. */
	public void setCommandId(int cmdId) {
		checkHeader();
		header.setCommandId(cmdId);
	}

	/**
	 * Sets the command status, i.e. error status of the PDU.
	 */
	public void setCommandStatus(int cmdStatus) {
		checkHeader();
		header.setCommandStatus(cmdStatus);
	}

	/**
	 * Sets the sequence number of the PDU. 
	 * For PDUs whic are about to be sent to SMSC the sequence number is
	 * generated automatically in the <code>Transmitter</code> class and
	 * under normal cicumstances there is no need to set it explicitly.
	 */
	public void setSequenceNumber(int seqNr) {
		checkHeader();
		header.setSequenceNumber(seqNr);
		sequenceNumberChanged = true;
	}

	/** If the command status of the PDU is ESME_ROK. */
	public boolean isOk() {
		return getCommandStatus() == Data.ESME_ROK;
	}

	/**
	 * If the PDU carries generic_nack despite of the class of the object.<br>
	 * Under some wierd conditions there is need to "encapsulate" generic
	 * negative acknowledge information into instance of class different
	 * from <code>GenericNack</code> class. For example synchronous
	 * call to the <code>Session</code>'s <code>submit</code> method
	 * should return instance of <code>SubmitSMResp</code> class.
	 * If the from some reason the SMSC returns generic_nack as a response
	 * to the submit_sm, then the library must somehow overcome the type
	 * checking for the return type and thus it creates instance of
	 * <code>SubmitSMResp</code> but then it sets the command id
	 * the id for generic_nack.
	 * @return of the object carries generic_nack pdu rather than pdu
	 *         as could be indicated from the object's class
	 * @see org.smpp.Session#checkResponse(PDU,Response)
	 */
	public boolean isGNack() {
		return getCommandId() == Data.GENERIC_NACK;
	}

	/**
	 * This method gets the buffer and returns an instance of class
	 * corresponding to the type of the PDU which was in the buffer; the
	 * fields of the returned PDU are set to the data from the buffer.<br>
	 * Cool, isn't it.
	 * @see #createPDU(int)
	 */
	public static final PDU createPDU(ByteBuffer buffer)
		throws
			HeaderIncompleteException,
			MessageIncompleteException,
			UnknownCommandIdException,
			InvalidPDUException,
			TLVException,
			PDUException {
		ByteBuffer headerBuf = null;
		try {
			// readBytes just reads bytes from the buffer but
			// doesn't alter the buffer
			headerBuf = buffer.readBytes(Data.PDU_HEADER_SIZE);
		} catch (NotEnoughDataInByteBufferException e) {
			// incomplete header
			throw new HeaderIncompleteException();
		}
		PDUHeader header = new PDUHeader();
		try {
			header.setData(headerBuf);
		} catch (NotEnoughDataInByteBufferException e) {
			// must be enough, we've checked it above
		}
		if (buffer.length() < header.getCommandLength()) {
			// not enough data in the buffer => throwing
			// Receiver must wait for more data
			throw new MessageIncompleteException();
		}
		PDU pdu = createPDU(header.getCommandId());
		if (pdu != null) {
			// paolo@bulksms.com: more consistent & safe to remove whatever we plan
			// to read from the buffer here already - stops problems with our parsing,
			// for instance, reading _too much_ data from the buffer because of an
			// invalid PDU (via, say, removeCString() in ByteBuffer):
			ByteBuffer thisMessageBuffer = null;
			try {
				thisMessageBuffer = buffer.removeBuffer(header.getCommandLength());
			} catch (NotEnoughDataInByteBufferException e) {} // can't fail
			pdu.setData(thisMessageBuffer);
			return pdu;
		} else {
			// if not found, throw
			throw new UnknownCommandIdException(header);
		}
	}

	/**
	 * Creates an empty instance of class which represents the PDU
	 * with given command id.
	 */
	public static final PDU createPDU(int commandId) {
		// Now routed to PDUFactory to solve deadlock (bug #1029141).
		return PDUFactory.createPDU(commandId);
	}

	public String debugString() {
		String dbgs = "(pdu: ";
		dbgs += super.debugString();
		dbgs += Integer.toString(getCommandLength());
		dbgs += " ";
		dbgs += Integer.toHexString(getCommandId());
		dbgs += " ";
		dbgs += Integer.toHexString(getCommandStatus());
		dbgs += " ";
		if (sequenceNumberChanged) {
			dbgs += Integer.toString(getSequenceNumber());
		} else {
			// it's likely that this will be the assigned seq number
			// (in simple cases :-)
			dbgs += "[" + (sequenceNumber + 1) + "]";
		}
		dbgs += ") ";
		return dbgs;
	}

	/** Returns debug string from provided optional parameters. */
	protected String debugStringOptional(String label, Vector<TLV> optionalParameters) {
		String dbgs = "";
		int size = optionalParameters.size();
		if (size > 0) {
			dbgs += "(" + label + ": ";
			TLV tlv = null;
			for (int i = 0; i < size; i++) {
				tlv = optionalParameters.get(i);
				if ((tlv != null) && (tlv.hasValue())) {
					dbgs += tlv.debugString();
					dbgs += " ";
				}
			}
			dbgs += ") ";
		}
		return dbgs;
	}

	/** Returns debug string of all optional parameters. */
	protected String debugStringOptional() {
		String dbgs = "";
		dbgs += debugStringOptional("opt", optionalParameters);
		dbgs += debugStringOptional("extraopt", extraOptionalParameters);
		return dbgs;
	}

	/**
	 * Compares two PDU. Two PDUs are equal if their sequence number is equal
	 * ( and now, if their command ids match as well).
	 */
	public boolean equals(Object object) {
		if ((object != null) && (object instanceof PDU)) {
			PDU pdu = (PDU) object;
			return (pdu.getSequenceNumber() == getSequenceNumber() &&
					pdu.getCommandId() == getCommandId());
		} else {
			return false;
		}
	}

	/**
	 * Sets the PDU extra information <code>value</code> with key
	 * <code>key</code>.
	 * This information is not sent anywhere nor the library uses this information
	 * in any means. It's intended for use by applicaitons to pass 
	 * information about the PDU from one part of the application to another
	 * without need of extra PDU management.
	 */
	public void setApplicationSpecificInfo(Object key, Object value) {
		if (applicationSpecificInfo == null) {
			applicationSpecificInfo = new Hashtable<Object, Object>();
		}
		debug.write(
			DPDU,
			"setting app spec info key=\"" + key + "\" value=\"" + (value == null ? "null" : value) + "\"");
		applicationSpecificInfo.put(key, value);
	}

	/**
	 * Sets the PDU extra information as a copy of existing Dictionary.
	 * The Dictionary is shallow copied, i.e. new container is created for the PDU
	 * and elements are put into the new container without copying.
	 * @see #setApplicationSpecificInfo(Object,Object)
	 * @see #cloneApplicationSpecificInfo(Dictionary)
	 */
	public void setApplicationSpecificInfo(Dictionary<Object, Object> applicationSpecificInfo) {
		this.applicationSpecificInfo = cloneApplicationSpecificInfo(applicationSpecificInfo);
	}

	/**
	 * Returns extra information with key <code>key</code>. If the information
	 * is not found for this PDU, returns <code>null</code>.
	 * @see #setApplicationSpecificInfo(Object,Object)
	 */
	public Object getApplicationSpecificInfo(Object key) {
		Object value = null;
		if (applicationSpecificInfo != null) {
			value = applicationSpecificInfo.get(key);
		}
		debug.write(
			DPDU,
			"getting app spec info key=\"" + key + "\" value=\"" + (value == null ? "null" : value) + "\"");
		return value;
	}

	/**
	 * Returns all the extra information related to this PDU. If there is no
	 * information found for this PDU, returns <code>null</code>.
	 * @see #setApplicationSpecificInfo(Object,Object)
	 */
	public Dictionary<Object, Object> getApplicationSpecificInfo() {
		return cloneApplicationSpecificInfo(applicationSpecificInfo);
	}

	/**
	 * Removes an extra information with the given <code>key</code>
	 * from this PDU.
	 * @see #setApplicationSpecificInfo(Object,Object)
	 */
	public void removeApplicationSpecificInfo(Object key) {
		if (applicationSpecificInfo != null) {
			applicationSpecificInfo.remove(key);
		}
	}

	/**
	 * Creates a shallow copy of the provided Dictionary, i.e. new container
	 * structure is created, but the keys and elements from the original
	 * Dictionary aren't cloned, htey are only put to the new structure.
	 */
	private Dictionary<Object, Object> cloneApplicationSpecificInfo(Dictionary<Object, Object> info) {
		Dictionary<Object, Object> newInfo = null;
		if (info != null) {
			newInfo = new Hashtable<Object, Object>();
			Enumeration<Object> keys = info.keys();
			Object key;
			Object value;
			while (keys.hasMoreElements()) {
				key = keys.nextElement();
				value = info.get(key);
				newInfo.put(key, value);
			}
		}
		return newInfo;
	}

}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.8  2007/01/17 10:52:19  paoloc
 * Fixed bug #1584984: PDU sequence number integer overflow
 *
 * Revision 1.7  2006/02/22 16:40:50  paoloc
 * Updated documentation for equals()
 *
 * Revision 1.6  2005/09/25 10:12:55  paoloc
 * Fixed bug 872640, probably introduced during refactoring from original Logica code - bracketing error, which would  cause certain code to run only if debugging was active.
 *
 * Revision 1.5  2004/09/10 23:10:54  sverkera
 * Corrected issue with optional parameters
 *
 * Revision 1.4  2004/09/04 08:55:20  paoloc
 * Now immediately removes the amount of data implied by command_length before parsing further - before, it was possible for parsing to read beyond the end of the PDU, via ByteBuffer methods like removeCString(). Of course, this sort of thing only happens when invalid PDUs are being parsed.
 *
 * Revision 1.3  2003/12/16 15:06:48  sverkera
 * Bugfix from smsforum.net
 * If an smpp connection established with the SMSC is idle,
 * the smsc sends enquire_link to the esme periodically, these
 * pdus are queue up in the receiver queue. After a bunch of these
 * pdus queue up, if you send a submit and the sequence number of this
 * submit request matches any of the sequence numbers of the enquire_link pdus already in the queue, this code returns the wrong pdu as the response to the submit request and gives a false impression that it failed.
 *
 * Revision 1.2  2003/07/24 14:30:36  sverkera
 * Solved a bug which caused exception to be thrown when there are no body
 *
 * Revision 1.1  2003/07/23 00:28:39  sverkera
 * Imported
 *
 * 
 * Old changelog:
 * 13-07-01 ticp@logica.com added assignSequenceNumber(boolean) to allow 
 *						    assigning seq nr even if the seq nr has been
 *						    already changed
 * 13-07-01 ticp@logica.com debug of buffer in hex is now printed in setData
 *						    and getData methods
 * 23-08-01 ticp@logica.com in setData() added test if the data following
 *						    mandatory params can be optional params, i.e.
 *						    if command length indicates that there are more data
 *						    after parsing mandatory params. before if the buffer
 *						    contained more data (e.g. more than 1 pdu) the
 *						    additional data were parsed as opt params even
 *						    if they were beyond the command length
 * 23-08-01 ticp@logica.com cosmetic change: tabs replaced by spaces
 * 26-09-01 ticp@logica.com method resetSequenceNumber() added which causes
 *						    that the PDU 'forgets' that it's sequence number
 *						    was already set, so if submitted again it'll get new one
 * 02-10-01 ticp@logica.com comments added
 * 02-10-01 ticp@logica.com debug now belongs to DPDU and DPDUD groups
 * 02-10-01 ticp@logica.com fixed bug in setData which could cause the optional
 *						    params to be read beyond the indicated length of PDU
 * 02-10-01 ticp@logica.com equals now checks if the object to be compared to
 *						    is instance of PDU
 * 09-10-01 ticp@logica.com added possibility to attach to PDU some application
 *						    specific data
 * 16-10-01 ticp@logica.com added checking if debug's group is active when logging
 *						    the buffer's hex dump in setData and getData
 * 16-10-01 ticp@logica.com improved parsing of the pdu for the sake of debug
 *						    debug speed improvement (in setData())
 * 20-11-01 ticp@logica.com added support for additional (extra) optional
 *						    parameters which aren't defined in the specs
 */
