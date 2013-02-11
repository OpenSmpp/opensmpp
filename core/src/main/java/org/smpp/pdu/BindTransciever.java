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

/**
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.1 $
 */
public class BindTransciever extends BindRequest {
	public BindTransciever() {
		super(Data.BIND_TRANSCEIVER);
	}

	protected Response createResponse() {
		return new BindTranscieverResp();
	}

	public boolean isTransmitter() {
		return true;
	}

	public boolean isReceiver() {
		return true;
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
