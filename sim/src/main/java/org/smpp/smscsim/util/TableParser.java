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
package org.smpp.smscsim.util;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * The <code>TableParser</code> is an interface for parsing and exportin
 * of <code>Table</code> data from and to data streams.
 *
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.1 $
 */
public interface TableParser {
	/**
	 * Should write the table to the output stream.
	 * @param os the output stream to write to
	 */
	public void compose(OutputStream os) throws IOException;

	/**
	 * Should read the table from the input stream.
	 * @param is the stream to read the table from
	 */
	public void parse(InputStream is) throws IOException;
}
/*
 * $Log: not supported by cvs2svn $
 */
