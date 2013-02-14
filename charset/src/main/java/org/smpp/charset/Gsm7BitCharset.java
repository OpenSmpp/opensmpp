package org.smpp.charset;

import java.nio.CharBuffer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.util.HashMap;

import java.util.logging.Logger;

/**
 * A Charset implementation for Gsm 7-bit default and extended character set
 * See GSM 03.38
 * 
 * @author Sverker Abrahamsson
 * @version $Id: Gsm7BitCharset.java 90 2011-04-19 22:07:52Z sverkera $
 */
public class Gsm7BitCharset extends Charset {

	private boolean debug = false;
	
	// HashMap's used for encoding and decoding
	protected static HashMap<String, Byte> defaultEncodeMap = new HashMap<String, Byte>();
	protected static HashMap<Byte, String> defaultDecodeMap = new HashMap<Byte, String>();
	protected static HashMap<String, Byte> extEncodeMap = new HashMap<String, Byte>();
	protected static HashMap<Byte, String> extDecodeMap = new HashMap<Byte, String>();
	
	// Data to populate the hashmaps with
	private static final Object[][] gsmCharacters = { 
		{ "@",      new Byte((byte) 0x00) },
		{ "£",      new Byte((byte) 0x01) },
		{ "$",      new Byte((byte) 0x02) },
		{ "¥",      new Byte((byte) 0x03) },
		{ "è",      new Byte((byte) 0x04) },
		{ "é",      new Byte((byte) 0x05) },
		{ "ù",      new Byte((byte) 0x06) },
		{ "ì",      new Byte((byte) 0x07) },
		{ "ò",      new Byte((byte) 0x08) },
		{ "Ç",      new Byte((byte) 0x09) },
		{ "\n",     new Byte((byte) 0x0a) },
		{ "Ø",      new Byte((byte) 0x0b) },
		{ "ø",      new Byte((byte) 0x0c) },
		{ "\r",     new Byte((byte) 0x0d) },
		{ "Å",      new Byte((byte) 0x0e) },
		{ "å",      new Byte((byte) 0x0f) },
		{ "\u0394", new Byte((byte) 0x10) },
		{ "_",      new Byte((byte) 0x11) },
		{ "\u03A6", new Byte((byte) 0x12) },
		{ "\u0393", new Byte((byte) 0x13) },
		{ "\u039B", new Byte((byte) 0x14) },
		{ "\u03A9", new Byte((byte) 0x15) },
		{ "\u03A0", new Byte((byte) 0x16) },
		{ "\u03A8", new Byte((byte) 0x17) },
		{ "\u03A3", new Byte((byte) 0x18) },
		{ "\u0398", new Byte((byte) 0x19) },
		{ "\u039E", new Byte((byte) 0x1a) },
		{ "\u001B", new Byte((byte) 0x1b) }, // 27 is Escape character
		{ "Æ",      new Byte((byte) 0x1c) },
		{ "æ",      new Byte((byte) 0x1d) },
		{ "ß",      new Byte((byte) 0x1e) },
		{ "É",      new Byte((byte) 0x1f) },
		{ "\u0020", new Byte((byte) 0x20) },
		{ "!",      new Byte((byte) 0x21) },
		{ "\"",     new Byte((byte) 0x22) },
		{ "#",      new Byte((byte) 0x23) },
		{ "¤",      new Byte((byte) 0x24) },
		{ "%",      new Byte((byte) 0x25) },
		{ "&",      new Byte((byte) 0x26) },
		{ "'",      new Byte((byte) 0x27) },
		{ "(",      new Byte((byte) 0x28) },
		{ ")",      new Byte((byte) 0x29) },
		{ "*",      new Byte((byte) 0x2a) },
		{ "+",      new Byte((byte) 0x2b) },
		{ ",",      new Byte((byte) 0x2c) },
		{ "-",      new Byte((byte) 0x2d) },
		{ ".",      new Byte((byte) 0x2e) },
		{ "/",      new Byte((byte) 0x2f) },
		{ "0",      new Byte((byte) 0x30) },
		{ "1",      new Byte((byte) 0x31) },
		{ "2",      new Byte((byte) 0x32) },
		{ "3",      new Byte((byte) 0x33) },
		{ "4",      new Byte((byte) 0x34) },
		{ "5",      new Byte((byte) 0x35) },
		{ "6",      new Byte((byte) 0x36) },
		{ "7",      new Byte((byte) 0x37) },
		{ "8",      new Byte((byte) 0x38) },
		{ "9",      new Byte((byte) 0x39) },
		{ ":",      new Byte((byte) 0x3a) },
		{ ";",      new Byte((byte) 0x3b) },
		{ "<",      new Byte((byte) 0x3c) },
		{ "=",      new Byte((byte) 0x3d) },
		{ ">",      new Byte((byte) 0x3e) },
		{ "?",      new Byte((byte) 0x3f) },
		{ "¡",      new Byte((byte) 0x40) },
		{ "A",      new Byte((byte) 0x41) },
		{ "B",      new Byte((byte) 0x42) },
		{ "C",      new Byte((byte) 0x43) },
		{ "D",      new Byte((byte) 0x44) },
		{ "E",      new Byte((byte) 0x45) },
		{ "F",      new Byte((byte) 0x46) },
		{ "G",      new Byte((byte) 0x47) },
		{ "H",      new Byte((byte) 0x48) },
		{ "I",      new Byte((byte) 0x49) },
		{ "J",      new Byte((byte) 0x4a) },
		{ "K",      new Byte((byte) 0x4b) },
		{ "L",      new Byte((byte) 0x4c) },
		{ "M",      new Byte((byte) 0x4d) },
		{ "N",      new Byte((byte) 0x4e) },
		{ "O",      new Byte((byte) 0x4f) },
		{ "P",      new Byte((byte) 0x50) },
		{ "Q",      new Byte((byte) 0x51) },
		{ "R",      new Byte((byte) 0x52) },
		{ "S",      new Byte((byte) 0x53) },
		{ "T",      new Byte((byte) 0x54) },
		{ "U",      new Byte((byte) 0x55) },
		{ "V",      new Byte((byte) 0x56) },
		{ "W",      new Byte((byte) 0x57) },
		{ "X",      new Byte((byte) 0x58) },
		{ "Y",      new Byte((byte) 0x59) },
		{ "Z",      new Byte((byte) 0x5a) },
		{ "Ä",      new Byte((byte) 0x5b) },
		{ "Ö",      new Byte((byte) 0x5c) },
		{ "Ñ",      new Byte((byte) 0x5d) },
		{ "Ü",      new Byte((byte) 0x5e) },
		{ "§",      new Byte((byte) 0x5f) },
		{ "¿",      new Byte((byte) 0x60) },
		{ "a",      new Byte((byte) 0x61) },
		{ "b",      new Byte((byte) 0x62) },
		{ "c",      new Byte((byte) 0x63) },
		{ "d",      new Byte((byte) 0x64) },
		{ "e",      new Byte((byte) 0x65) },
		{ "f",      new Byte((byte) 0x66) },
		{ "g",      new Byte((byte) 0x67) },
		{ "h",      new Byte((byte) 0x68) },
		{ "i",      new Byte((byte) 0x69) },
		{ "j",      new Byte((byte) 0x6a) },
		{ "k",      new Byte((byte) 0x6b) },
		{ "l",      new Byte((byte) 0x6c) },
		{ "m",      new Byte((byte) 0x6d) },
		{ "n",      new Byte((byte) 0x6e) },
		{ "o",      new Byte((byte) 0x6f) },
		{ "p",      new Byte((byte) 0x70) },
		{ "q",      new Byte((byte) 0x71) },
		{ "r",      new Byte((byte) 0x72) },
		{ "s",      new Byte((byte) 0x73) },
		{ "t",      new Byte((byte) 0x74) },
		{ "u",      new Byte((byte) 0x75) },
		{ "v",      new Byte((byte) 0x76) },
		{ "w",      new Byte((byte) 0x77) },
		{ "x",      new Byte((byte) 0x78) },
		{ "y",      new Byte((byte) 0x79) },
		{ "z",      new Byte((byte) 0x7a) },
		{ "ä",      new Byte((byte) 0x7b) },
		{ "ö",      new Byte((byte) 0x7c) },
		{ "ñ",      new Byte((byte) 0x7d) },
		{ "ü",      new Byte((byte) 0x7e) },
		{ "à",      new Byte((byte) 0x7f) }
	};

	private static final Object[][] gsmExtensionCharacters = { 
		{ "\n", new Byte((byte) 0x0a) },
		{ "^",  new Byte((byte) 0x14) },
		{ " ",  new Byte((byte) 0x1b) }, // reserved for future extensions
		{ "{",  new Byte((byte) 0x28) },
		{ "}",  new Byte((byte) 0x29) },
		{ "\\", new Byte((byte) 0x2f) },
		{ "[",  new Byte((byte) 0x3c) },
		{ "~",  new Byte((byte) 0x3d) },
		{ "]",  new Byte((byte) 0x3e) },
		{ "|",  new Byte((byte) 0x40) },
		{ "",  new Byte((byte) 0x65) }
	};

	private static Logger logger = Logger.getLogger(Gsm7BitCharset.class.getName());
	
	// static section that populates the encode and decode HashMap objects
	static {
		// default alphabet
		int len = gsmCharacters.length;
		for (int i = 0; i < len; i++) {
			Object[] map = gsmCharacters[i];
			defaultEncodeMap.put((String) map[0], (Byte) map[1]);
			defaultDecodeMap.put((Byte) map[1], (String) map[0]);
		}

		// extended alphabet
		len = gsmExtensionCharacters.length;
		for (int i = 0; i < len; i++) {
			Object[] map = gsmExtensionCharacters[i];
			extEncodeMap.put((String) map[0], (Byte) map[1]);
			extDecodeMap.put((Byte) map[1], (String) map[0]);
		}
	}

	/**
	 * Constructor for the Gsm7Bit charset.  Call the superclass
	 * constructor to pass along the name(s) we'll be known by.
	 * Then save a reference to the delegate Charset.
	 */
	protected Gsm7BitCharset(String canonical, String[] aliases) {
		super(canonical, aliases);
	}

	// ----------------------------------------------------------

	/**
	 * Called by users of this Charset to obtain an encoder.
	 * This implementation instantiates an instance of a private class
	 * (defined below) and passes it an encoder from the base Charset.
	 */
	public CharsetEncoder newEncoder() {
		return new Gsm7BitEncoder(this);
	}

	/**
	 * Called by users of this Charset to obtain a decoder.
	 * This implementation instantiates an instance of a private class
	 * (defined below) and passes it a decoder from the base Charset.
	 */
	public CharsetDecoder newDecoder() {
		return new Gsm7BitDecoder(this);
	}

	/**
	 * This method must be implemented by concrete Charsets.  We always
	 * say no, which is safe.
	 */
	public boolean contains(Charset cs) {
		return (false);
	}

	/**
	 * The encoder implementation for the Gsm7Bit Charset.
	 * This class, and the matching decoder class below, should also
	 * override the "impl" methods, such as implOnMalformedInput() and
	 * make passthrough calls to the baseEncoder object.  That is left
	 * as an exercise for the hacker.
	 */
	private class Gsm7BitEncoder extends CharsetEncoder {

		/**
		 * Constructor, call the superclass constructor with the
		 * Charset object and the encodings sizes from the
		 * delegate encoder.
		 */
		Gsm7BitEncoder(Charset cs) {
			super(cs, 1, 2);
		}

		/**
		 * Implementation of the encoding loop.
		 */
		protected CoderResult encodeLoop(CharBuffer cb, ByteBuffer bb) {
			CoderResult cr = CoderResult.UNDERFLOW;

			while (cb.hasRemaining() && bb.hasRemaining()) {
				char ch = cb.get();

				// first check the default alphabet
				Byte b = (Byte) defaultEncodeMap.get("" + ch);
				if(debug)
					logger.finest("Encoding ch " + ch + " to byte " + b);
				if (b != null) {
					bb.put((byte) b.byteValue());
				} else {
					// check extended alphabet
					b = (Byte) extEncodeMap.get("" + ch);
					if(debug)
						logger.finest("Trying extended map to encode ch " + ch + " to byte " + b);
					if (b != null) {
						// since the extended character set takes two bytes 
						// we have to check that there is enough space left
						if (bb.remaining() < 2) {
							// go back one step
							cb.position(cb.position() - 1);
							cr = CoderResult.OVERFLOW;
							break;
						}
						// all ok, add it to the buffer
						bb.put((byte) 0x1b);
						bb.put((byte) b.byteValue());
					} else {
						// no match found, send a ?
						b = new Byte((byte) 0x3F);
						bb.put((byte) b.byteValue());
					}
				}
			}
			return cr;
		}
	}

	// --------------------------------------------------------

	/**
	 * The decoder implementation for the Gsm 7Bit Charset.
	 */
	private class Gsm7BitDecoder extends CharsetDecoder {

		/**
		 * Constructor, call the superclass constructor with the
		 * Charset object and pass alon the chars/byte values
		 * from the delegate decoder.
		 */
		Gsm7BitDecoder(Charset cs) {
			super(cs, 1, 1);
		}

		/**
		 * Implementation of the decoding loop.
		 */
		protected CoderResult decodeLoop(ByteBuffer bb, CharBuffer cb) {
			CoderResult cr = CoderResult.UNDERFLOW;

			while (bb.hasRemaining() && cb.hasRemaining()) {
				byte b = bb.get();

				// first check the default alphabet
				if(debug)
					logger.finest("Looking up byte " + b);
				String s = (String) defaultDecodeMap.get(new Byte(b));
				if (s != null) {
					char ch = s.charAt(0);
					if (ch != '\u001B') {
						if(debug)
							logger.finest("Found string " + s);
						cb.put(ch);
					} else {
						if(debug)
							logger.finest("Found escape character");
						// check the extended alphabet
						if (bb.hasRemaining()) {
							b = bb.get();
							s = (String) extDecodeMap.get(new Byte(b));
							if (s != null) {
								if(debug)
									logger.finest("Found extended string " + s);
								ch = s.charAt(0);
								cb.put(ch);
							} else {
								cb.put('?');
							}
						}
					}
				} else {
					cb.put('?');
				}
			}
			return cr;
		}
	}
}

/*
 * $Log: not supported by cvs2svn $
 * Revision 1.2  2006/03/09 16:24:14  sverkera
 * Removed compiler and javadoc warnings
 *
 * Revision 1.1  2003/09/30 09:02:09  sverkera
 * Added implementation for GSM 7Bit charset
 *
 */
