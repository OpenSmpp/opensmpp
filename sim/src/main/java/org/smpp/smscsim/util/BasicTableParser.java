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
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.smpp.SmppObject;
import org.smpp.debug.Debug;

/**
 * Implements the <code>TableParser</code>. The expected format of the file
 * is as follows:<br>
 * <ul>
 *   <li>every line in the stream contains either attribute or
 *       comment or is empty</li>
 *   <li>each attribute is on one line</li>
 *   <li>attribute is in form <code>name "=" value</code><br>
 *       Example:<br><code>
 *         name=javier
 *       </code>
 *   </li>
 *   <li>Comment starts with hash <code>#</code> character in the first
 *       column of the line. If you need more lines of comment, each line
 *       of the comment must start with hash.</li>
 *   <li>record consists from several consecutive attributes; no emplty line
 *       or comment is allowed within one record</li>
 *   <li>records are delimited by one or more empty line or comments<br>
 *       Example:<br><pre>
 *# This is a comment
 *# The following line is an empty line
 *
 *# The first record follows
 *name=Charles
 *password=slercha
 *access=guest
 *
 *# And the second record...
 *name=thomas
 *password=TheOmas
 *# Note that comment doesn't (!) delimit records
 *# so you can comment on single attributes within the record
 *type=admin
 *access=full
 *
 *# And this is the third record.
 *name=peter
 *password=agent007
 *#Ok, this should be enough for demonstration.
 *
 *</pre></li>
 * </ul>
 * Note that if you read the file and then write it, the comments aren't
 * preserved.
 *
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.2 $
 */
public class BasicTableParser implements TableParser {
	/*
	  End of line chars definitions
	*/
	static final char CR = '\r';
	static final char LF = '\n';
	static final String LINE_END = "\r\n";

	/**
	 * The characters which can be used as delimiters between name of
	 * the attribute and it's value.
	 */
	static final String ATTR_DELIMS = "=:";

	static final String COMMENT_CHARS = "#;";

	Table table;
	InputStreamReader in;
	OutputStreamWriter out;
	char c, pending;
	boolean pendingChar = false;
	String line = "";
	Record record;
	boolean pendingRecord = false;

	private Debug debug = SmppObject.getDebug();

	/**
	 * This constructor passes to the parser a table to work on.
	 */
	public BasicTableParser(Table table) {
		this.table = table;
	}

	/**
	 * Parses the input stream and fills the table with data from the stream.
	 * @param is the input stream to read the data from
	 * @see #compose(OutputStream)
	 */
	public void parse(InputStream is) throws IOException {
		debug.write("going to parse from input stream");
		in = new InputStreamReader(is);
		prepareRecord();
		while (!eof()) {
			getLine();
			if (isEmpty()) {
				debug.write("got empty line");
				finaliseRecord(true);
			} else if (!isComment()) {
				parseAttribute(line);
			} else {
				debug.write("got comment line " + line);
			}
		}
		finaliseRecord(false);
	}

	/**
	 * Writes to the output stream formatted content of the table.
	 * @param os the output stream to write to
	 * @see #parse(InputStream)
	 */
	public void compose(OutputStream os) throws IOException {
		out = new OutputStreamWriter(os);
		Record record;
		Attribute attribute;
		int recCount;
		int attrCount;
		synchronized (table) {
			recCount = table.count();
			for (int ri = 0; ri < recCount; ri++) {
				record = table.get(ri);
				synchronized (record) {
					attrCount = record.count();
					for (int ai = 0; ai < attrCount; ai++) {
						attribute = record.get(ai);
						synchronized (attribute) {
							line = attribute.getName() + "=" + attribute.getValue();
						}
						out.write(line);
						out.write(LINE_END);
					}
				}
				if ((ri + 1) < recCount) {
					// if this wasn't last record, write empty line
					// as delimiter between users
					out.write(LINE_END);
				}
			}
		}
		out.flush();
	}

	/**
	 * Called whenever end of record was reached. If there is a record
	 * which hasn't been inserted to the table yet, it's inserted by this
	 * method.
	 */
	void finaliseRecord(boolean prepareNext) {
		if (pendingRecord) {
			debug.write("finished record, adding to table");
			table.add(record);
			pendingRecord = false;
			if (prepareNext) {
				prepareRecord();
			}
		}
	}

	/**
	 * Creates new record to add the newly read attributes to.
	 */
	void prepareRecord() {
		record = new Record();
	}

	/**
	 * Parses attribute and inserts it into the record.
	 */
	void parseAttribute(String attr) {
		int attrLen = attr.length();
		int currPos = 0;
		debug.write("going to parse attribute " + attr);
		while ((currPos < attrLen) && (ATTR_DELIMS.indexOf(attr.charAt(currPos)) == -1)) {
			currPos++;
		}
		String name = attr.substring(0, currPos);
		String value = attr.substring(currPos + 1, attrLen);
		record.set(name, value);
		pendingRecord = true;
	}

	/**
	 * Returns if end of the stream was already reached.
	 */
	boolean eof() throws IOException {
		return !in.ready();
	}

	/**
	 * Returns if on the current position in the stream there is end of line
	 * character.
	 */
	boolean eol() {
		return (c == CR) || (c == LF);
	}

	/**
	 * Returns if the current line is empty, i.e. doesn't contain any character
	 * including whitespace.
	 */
	boolean isEmpty() {
		return line.length() == 0;
	}

	/**
	 * Returns if the current line contains a comment text.
	 */
	boolean isComment() {
		return isEmpty() ? false : COMMENT_CHARS.indexOf(line.charAt(0)) != -1;
	}

	/**
	 * Reads one line from the input stream and stores it into <code>line</code>
	 * variable.
	 * @see #get()
	 * @see #unget()
	 */
	void getLine() throws IOException {
		line = "";
		do {
			get();
			if (!eol()) {
				line += c;
			}
		} while (!eof() && !eol());
		if (!eof()) {
			// then it must have been eol => we are trying
			// to skip another potential line delim
			if (c == CR) {
				// then we could have CRLF
				get();
				if (c != LF) {
					// no CRLF
					unget();
				}
			} else {
				// nothing as LF is ok
			}
		}
	}

	/**
	 * Reads one character from input stream or gets a pending character
	 * read before.
	 * @see #getLine()
	 * @see #unget()
	 */
	void get() throws IOException {
		if (pendingChar) {
			c = pending;
			pendingChar = false;
		} else {
			c = (char) in.read();
		}
	}

	/**
	 * If necessary, one and only one character can be 'unget' by this method.
	 * This character becomes pending character and will be get by next call to
	 * method <code>get</code>.
	 * @see #get()
	 * @see #getLine()
	 */
	void unget() {
		pending = c;
		pendingChar = true;
	}

}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.1  2003/07/23 00:28:39  sverkera
 * Imported
 *
 */
