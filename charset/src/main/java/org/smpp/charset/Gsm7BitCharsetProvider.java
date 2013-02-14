package org.smpp.charset;

import java.nio.charset.Charset;
import java.nio.charset.spi.CharsetProvider;
import java.util.HashSet;
import java.util.Iterator;

/**
 * This is a CharsetProvider for the GSM 7-Bit character set. It is named
 * X-Gsm7Bit since it's not registered in the IANA registry
 *
 * To activate this CharsetProvider, it's necessary to add a file to
 * the classpath of the JVM runtime at the following location:
 *   META-INF/services/java.nio.charsets.spi.CharsetProvider
 *
 * That file must contain a line with the fully qualified name of
 * this class on a line by itself:
 *   org.smpp.charset.Gsm7BitCharsetProvider
 *
 * See the javadoc page for java.nio.charsets.spi.CharsetProvider
 * for full details.
 *
 * @author Sverker Abrahamsson
 * @version $Revision: 1.1 $
 */
public class Gsm7BitCharsetProvider extends CharsetProvider {

	// The name of the charset we provide
	private static final String CHARSET_NAME = "X-Gsm7Bit";

	// A handle to the Charset object
	private Charset gsm7Bit = null;

	private boolean debug = false;
	
	/**
	 * Constructor, instantiate a Charset object and save the reference.
	 */
	public Gsm7BitCharsetProvider() {
		super();
		if(debug)
			System.out.println("Instansiating " + CHARSET_NAME);
		this.gsm7Bit = new Gsm7BitCharset(CHARSET_NAME, null);
	}

	/**
	 * Called by Charset static methods to find a particular named
	 * Charset.  If it's the name of this charset (we don't have
	 * any aliases) then return the Rot13 Charset, else return null.
	 */
	public Charset charsetForName (String charsetName) {
		if(charsetName.equalsIgnoreCase(CHARSET_NAME)) {
			return(gsm7Bit);
		}
		return(null);
	}

	/**
	 * Return an Iterator over the set of Charset objects we provide.
	 * @return An Iterator object containing references to all the
	 *  Charset objects provided by this class.
	 */
	public Iterator<Charset> charsets() {
		HashSet<Charset> set = new HashSet<Charset>(1);
		set.add(gsm7Bit);
		return(set.iterator());
	}
}
/*
 * $Log: not supported by cvs2svn $
 */