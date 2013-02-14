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

/**
 * Represents a PDU request. All classes which are used as SMPP requests are
 * derived from this class.
 *
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.1 $
 */
public abstract class Request extends PDU {
	/**
	 * This method should instantiate a response corresponding to the request.
	 */
	protected abstract Response createResponse();

	/** Create a request PDU with default parameters. */
	public Request() {
	}

	/**
	 * Create request PDU with given command id.
	 * Derived classes usually uses <code>super(THE_COMMAND_ID)</code>
	 * where the <code>THE_COMMAND_ID</code> is the command id of the 
	 * PDU the derived class represents.
	 */
	public Request(int commandId) {
		super(commandId);
	}

	/**
	 * This method is used to create generate a response corresponding to
	 * this request. It creates the response using <code>createResponse</code>
	 * and then sets the sequence number of the response to the sequence
	 * number of this request. This way is ensured automatic matching
	 * of the response to the request.
	 * The created response is set this as the original request.
	 * @see #createResponse()
	 * @see Response#setOriginalRequest(Request)
	 */
	public Response getResponse() {
		Response response = createResponse();
		response.setSequenceNumber(getSequenceNumber());
		response.setOriginalRequest(this);
		return response;
	}

	/**
	 * Returns the command id of the corresponing response.
	 */
	public int getResponseCommandId() {
		Response response = createResponse();
		return response.getCommandId();
	}

	/**
	 * Returns true. If the derived class cannot respond, then it must
	 * overwrite this function to return false.
	 * @see PDU#canResponse()
	 */
	public boolean canResponse() {
		return true;
	}

	/**
	 * Returns true. 
	 * @see PDU#isRequest()
	 */
	public boolean isRequest() {
		return true;
	}

	/** Returns false.
	 * @see PDU#isResponse()
	 */
	public boolean isResponse() {
		return false;
	}
}
/*
 * $Log: not supported by cvs2svn $
 * 
 * Old changelog:
 * 02-10-01 ticp@logica.com comments added, indentation changed -> spaces
 * 09-10-01 ticp@logica.com added registration of the request as original request
 *						    to the response created by getResponse
 */
