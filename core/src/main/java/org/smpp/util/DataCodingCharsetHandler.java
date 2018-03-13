package org.smpp.util;

import org.smpp.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles charset handling based on <code>data_coding</code> parameter, see
 * SMPP 3.4 specification, paragraph 5.2.19
 */
public class DataCodingCharsetHandler {
	private static final Map<Byte, String> DATA_CODING_CHARSET;

	static {
		DATA_CODING_CHARSET = new HashMap<Byte, String>();
		DATA_CODING_CHARSET.put((byte) 0, Data.ENC_GSM7BIT);
		DATA_CODING_CHARSET.put((byte) 8, Data.ENC_UTF16);
	}

	/**
	 * Return the correct charset given the <code>data_coding</code> parameter.
	 * If none is found it defaults to <code>X-Gsm7Bit</code>.
	 *
	 * @param dataCoding
	 * @return encoding name
	 */
	public static String getCharsetName(byte dataCoding) {
		return getCharsetName(dataCoding, Data.ENC_GSM7BIT);
	}

	/**
	 * Return the correct charset given the <code>data_coding</code> parameter.
	 *
	 * @param dataCoding
	 * @param defaultEncoding
	 * @return encoding name
	 */
	public static String getCharsetName(byte dataCoding, String defaultEncoding) {
		String encoding = DATA_CODING_CHARSET.get(dataCoding);
		return encoding != null ? encoding : defaultEncoding;
	}
}
