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
import java.text.SimpleDateFormat;
import java.text.ParseException;

import org.smpp.Data;
import org.smpp.SmppObject;
import org.smpp.util.*;

/**
 * Base class for all object which can be transformed to sequence of bytes
 * and which can be re-read from sequence of bytes. The sequence of bytes
 * is represented by <code>ByteBuffer</code>.
 * Every descendant of this class must implement <code>setData</code> and
 * <code>getData</code> functions.
 * Apart from abstract methods this class contains static methods for checking
 * of validity of certain values like if the length of string is
 * within valid boundary, if the date format is valid etc.
 *
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Id: ByteData.java 72 2008-07-15 19:43:00Z sverkera $
 * @see #setData(ByteBuffer)
 * @see #getData()
 */
public abstract class ByteData extends SmppObject {
	/**
	 * Defines a format of part of the smpp defined date format
	 * which is parseable by Java SimpleDateFormat parser.
	 * The rest (nnp) is parsed 'manually'.
	 */
	private static final String SMPP_TIME_DATE_FORMAT = "yyMMddHHmmss";

	/**
	 * The formatter object used for checking if the format of the datetime
	 * string is correct.
	 */
	private static SimpleDateFormat dateFormatter;

	/**
	 * Controls checking of the date-time format in the library.
	 * If this variable to is set to <code>true</code> the library will check if
	 * the date is correctly formated according SMPP spec; if it's
	 * <code>false</code>, then the date will be sent without checking in
	 * the library and the check will be done by the SMSC. Default
	 * is <code>true</code>.
	 * Note that whatever is the setting, the library will still check
	 * the length of the date-time string.
	 */
	private static boolean libraryCheckDateFormat = true;

	/**
	 * Static initialiser initialises the <code>dateFormatter</code>
	 * with format specified for SMPP Date/Time format and sets
	 * other formatter parameters.
	 */
	static {
		dateFormatter = new SimpleDateFormat(SMPP_TIME_DATE_FORMAT);
		dateFormatter.setLenient(false);
	}

	/**
	* This abstract method should parse the buffer with binary data
	* passed as parameter into member variables.
	*
	* @param buffer the data which should contain binary representation of
	*               the class
	* @see #getData()
	* @throws PDUException some data in the buffer were invalid
	* @throws NotEnoughDataInByteBufferException expected more data in
	          the buffer
	* @throws TerminatingZeroNotFoundException the c-string in buffer
	*         wasn't terminated with 0  zero
	*/
	public abstract void setData(ByteBuffer buffer)
		throws PDUException, NotEnoughDataInByteBufferException, TerminatingZeroNotFoundException;

	/**
	* This abstract method should create a binary buffer from it's member
	* variables.
	*
	* @return the binary data buffer created from member variables
	* @see #setData(ByteBuffer)
	* @throws ValueNotSetException thrown from a TLV if the value was
	*         requested but never set
	* @see org.smpp.pdu.tlv.TLV
	* @see org.smpp.pdu.ValueNotSetException
	*/
	public abstract ByteBuffer getData() throws ValueNotSetException;

	/**
	 * Default constructor. Only this is present as this
	 * abstract class doesn't have any member variables.
	 */
	public ByteData() {
	}

	/**
	 * Checks if the length of string is less or equal than the provided
	 * maximum.
	 *
	 * @param string the string to check
	 * @param max    the maximal length of the string
	 * @exception WrongLengthOfStringException thrown if the string is longer
	 *            than the provided max length
	 */
	protected static void checkString(String string, int max) throws WrongLengthOfStringException {
		checkString(string, 0, max);
	}

	/**
	 * Checks if the length of the data of the string is less or equal than
	 * the provided maximum; necessery for multibyte encodings. 
	 * Note that the length checked is the length wfter transforming
	 * the string to series of octets, so two-byte strings will efectively
	 * require space (max size) two-times the length of the string.
	 * @param string the string to check
	 * @param max    the maximal length of the string
	 * @param encoding the encoding of the string
	 * @exception WrongLengthOfStringException thrown if the string is longer
	 *            than the provided max length
	 * @exception UnsupportedEncodingException the required encoding isn't
	 *            supported
	 */
	protected static void checkString(String string, int max, String encoding)
		throws WrongLengthOfStringException, UnsupportedEncodingException {
		checkString(string, 0, max, encoding);
	}

	/**
	 * Checks if the length of string is greater or equal than provided
	 * minimum and less or equal than the provided maximum.
	 *
	 * @param string the string to check
	 * @param min    the minimal length of the string
	 * @param max    the maximal length of the string
	 * @throws WrongLengthOfStringException thrown if the string is shorter
	 *         than min length or longer than max length
	 */
	protected static void checkString(String string, int min, int max) throws WrongLengthOfStringException {
		int length = string == null ? 0 : string.length();
		checkString(min, length, max);
	}

	/**
	 * Checks if the length of the data of the string is greater or equal
	 * than provided minimum and less or equal than the provided maximum;
	 * necessery for multibyte encodings.
	 *
	 * @param string the string to check
	 * @param min    the minimal length of the string
	 * @param max    the maximal length of the string
	 * @param encoding the encoding of the string
	 * @throws WrongLengthOfStringException thrown if the string is shorter
	 *         than min length or longer than max length
	 * @exception UnsupportedEncodingException the required encoding isn't
	 *            supported
	 */
	protected static void checkString(String string, int min, int max, String encoding)
		throws WrongLengthOfStringException, UnsupportedEncodingException {
		byte[] stringBytes = string.getBytes(encoding);
		int length = stringBytes == null ? 0 : stringBytes.length;
		checkString(min, length, max);
	}

	/**
	 * Checks if the integer value representing length is within provided valid
	 * length.
	 *
	 * @param min minimal possible length
	 * @param length the length to check
	 * @param max maximal possible length
	 * @throws  thrown if the value is out of bounds
	 */
	protected static void checkString(int min, int length, int max) throws WrongLengthOfStringException {
		if ((length < min) || (length > max)) {
			throw new WrongLengthOfStringException(min, max, length);
		}
	}

	/**
	 * Checks if the length of the string plus 1 for terminating zero
	 * is less or equal than provided maximum.
	 *
	 * @param string the string to check
	 * @param max    the maximal length of the string with added term. zero
	 * @exception WrongLengthOfStringException thrown if string with added
	 *            terminating zero would be longer than the maximum
	 */
	protected static void checkCString(String string, int max) throws WrongLengthOfStringException {
		checkCString(string, 1, max); // min = empty + 1 for zero
	}

	/**
	 * Checks if the length of the string plus 1 for terminating zero
	 * is greater or equal than provided minimum and less or equal than
	 * provided maximum.
	 *
	 * @param string the string to check
	 
	 * @param min    the minimal length of the string with added term. zero
	 * @param max    the maximal length of the string with added term. zero
	 * @throws WrongLengthOfStringException thrown if string with added
	 *         terminating zero would be shorter than minimum or longer than
	 *         the maximum
	 */
	protected static void checkCString(String string, int min, int max) throws WrongLengthOfStringException {
		int count = string == null ? 1 : (string.length() + 1); // 1 is for terminating zero
		if (count < min || count > max) {
			throw new WrongLengthOfStringException(min, max, count);
		}
	}

	/**
	 * Checks if the string contains valid date string as specified in SMPP spec.
	 *
	 * @param dateStr the date to check
	 * @throws WrongDateFormatException throwsn if the string doesn't
	 *         contain date with valid format
	 */
	protected static void checkDate(String dateStr) throws WrongDateFormatException {
		int count = dateStr == null ? 1 : (dateStr.length() + 1); // 1 is for terminating zero
		if ((count != 1) && (count != Data.SM_DATE_LEN)) {
			throw new WrongDateFormatException(dateStr);
		}
		if ((count == 1) || (!libraryCheckDateFormat)) {
			// i.e. no date provided or don't check the format
			return;
		}
		char locTime = dateStr.charAt(dateStr.length() - 1);
		if ("+-R".lastIndexOf(locTime) == -1) {
			// i.e. the locTime isn't one of the possible values
			throw new WrongDateFormatException(
				dateStr,
				"time difference relation indicator incorrect; " + "should be +, - or R and is " + locTime);
		}
		int formatLen = SMPP_TIME_DATE_FORMAT.length();
		String dateJavaStr = dateStr.substring(0, formatLen);
		synchronized (dateFormatter) {
			try {
				if (locTime == 'R') {
					// check relative date
					// won't check date validity just if it's all number it
					Long.parseLong(dateJavaStr);
				} else {
					// check absolute dates
					dateFormatter.parse(dateJavaStr);
				}
			} catch (ParseException e) {
				debug.write("Exception parsing absolute date " + dateStr + " " + e);
				throw new WrongDateFormatException(dateStr, "format of absolute date-time incorrect");
			} catch (NumberFormatException e) {
				debug.write("Exception parsing relative date " + dateStr + " " + e);
				throw new WrongDateFormatException(dateStr, "format of relative date-time incorrect");
			}
		}
		String tenthsOfSecStr = dateStr.substring(formatLen, formatLen + 1);
		try {
			Integer.parseInt(tenthsOfSecStr);
		} catch (NumberFormatException e) {
			throw new WrongDateFormatException(dateStr, "non-numeric tenths of seconds " + tenthsOfSecStr);
		}
		String timeDiffStr = dateStr.substring(formatLen + 1, formatLen + 3);
		int timeDiff = 0;
		try {
			timeDiff = Integer.parseInt(timeDiffStr);
		} catch (NumberFormatException e) {
			throw new WrongDateFormatException(dateStr, "non-numeric time difference " + timeDiffStr);
		}
		if ((timeDiff < 0) || (timeDiff > 48)) {
			// defined in SMPP v3.4 sect. 7.1.1
			throw new WrongDateFormatException(
				dateStr,
				"time difference is incorrect; " + "should be between 00-48 and is " + timeDiffStr);
		}
	}

	/**
	 * Checks if the integer value is within provided valid range of values.
	 *
	 * @param min minimal possible value
	 * @param val the value to check
	 * @param max maximal possible value
	 * @throws IntegerOutOfRangeException thrown if the value is out of bounds
	 */
	protected static void checkRange(int min, int val, int max) throws IntegerOutOfRangeException {
		if ((val < min) || (val > max)) {
			throw new IntegerOutOfRangeException(min, max, val);
		}
	}

	/**
	 * Allow a variable of type <code>byte</code> to carry lengths or sizes up to 255.
	 * The integral types are signed in Java, so if there is necessary to store
	 * an unsigned type into signed of the same size, the value can be stored
	 * as negative number even if it would be positive in unsigned case.
	 * For example message length can be 0 to 254 and is carried by 1 octet
	 * i.e. unsigned 8 bits. We use a byte variable to read the value from octet stream.
	 * If the length is >127, it is interpreted as negative byte using negative
	 * complement notation.
	 * So length 150 would be interpreted as Java byte -106. If we want to know
	 * the actual value (*length) we need to make a correction to a bigger integral type,
	 * in case of byte it's short. The correction from (negative) byte to short is<br>
	 * <code>(short)(256+(short)length)</code><br>
	 * This converts the negative byte value representing positive length into positive
	 * short value.
	 * @see #encodeUnsigned(short)
	 */
	protected static short decodeUnsigned(byte signed) {
		if (signed >= 0) {
			return signed;
		} else {
			return (short) (256 + (short) signed);
		}
	}

	/**
	 * Provides correction of positive unsigned 2 byte carried 
	 * by (signed) <code>short</code> into positive signed <code>int</code>.
	 * See explanation in <code>decodeUnsigned(byte)</code>.
	 * @see #decodeUnsigned(byte)
	 * @see #encodeUnsigned(int)
	 */
	protected static int decodeUnsigned(short signed) {
		if (signed >= 0) {
			return signed;
		} else {
			return (int) (65536 + (int) signed);
		}
	}

	/**
	 * Complementary operation to <code>decodeUnsigned</code>.
	 * @see #decodeUnsigned(byte)
	 */
	protected static byte encodeUnsigned(short positive) {
		if (positive < 128) {
			return (byte) positive;
		} else {
			return (byte) (- (256 - positive));
		}
	}

	/**
	 * Complementary operation to <code>decodeUnsigned</code>.
	 * @see #decodeUnsigned(byte)
	 * @see #decodeUnsigned(short)
	 */
	protected static short encodeUnsigned(int positive) {
		if (positive < 32768) {
            // paolo@bulksms.com 2005-09-22: no, this isn't right! Casting the
			// short to a byte here overflows the byte, converts it back to a
			// short, and produces a bogus result. This was the cause of a bug
			// whereby invalid TLVs were produced: try creating a TLV longer than
			// 127 octets and see what happens...
			//return (byte)positive;
			return (short)positive;
		} else {
			return (short) (- (65536 - positive));
		}
	}

	/**
	 * Returns human readable version of the data carried by the object.
	 * Derived classes should override this method with possible inclusion
	 * of result of <code>super.debugString()</code>.
	 *
	 * @return the textual form of the content of the object
	 */
	public String debugString() {
		return new String("");
	}

}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.2  2005/09/25 09:33:45  paoloc
 * Fixed bug in encodeUnsigned(int): if one created a TLV longer than 127 octets, the resul was invalid (invalid L)
 *
 * Revision 1.1  2003/07/23 00:28:39  sverkera
 * Imported
 *
 *
 * Old changelog:
 * 13-07-01 ticp@logica.com indentation fixed, tabs aren't used anymore
 * 03-10-01 ticp@logica.com string passed as a parameter to the string length
 *						    checking routines is now checked first if it's not null
 * 03-10-01 ticp@logica.com the datetime string format is fully checked
 *						    in the function checkDate according to the date format
 *						    spec in smpp
 * 31-10-01 ticp@logica.com added methods for correction from 'smaller' int negative
 *						    to 'bigger' int positive (for variables carrying lengths)
 *						    (several times reported this, thanks!)
 * 15-11-01 ticp@logica.com added support for checking length of data produced
 *						    from string with multibyte encoding
 * 16-11-01 ticp@logica.com added method for checking if length provided as int
 *						    is within provided bounds
 * 20-11-01 ticp@logica.com fixed date checking where relative dates were
 *						    rejected as incorrect; now relative dates are checked
 *						    only partially
 */
