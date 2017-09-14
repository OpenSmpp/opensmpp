package org.smpp.charset;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.spi.CharsetProvider;

import org.junit.Before;
import org.junit.Test;

public class Gsm7BitCharsetTest {

	private Charset charset; 

	@Before
	public void setup() {
		CharsetProvider provider = new Gsm7BitCharsetProvider();
		
		charset = provider.charsetForName("X-Gsm7Bit");
	}

	@Test
	public void testEuroCharacterEncoding() {
		assertEquals(ByteBuffer.wrap(new byte[] { (byte) 0x1b, (byte) 0x65 }), charset.encode("€"));
	}
	@Test
	public void testEuroDecoding() {
		assertEquals(CharBuffer.wrap("€"), charset.decode(ByteBuffer.wrap(new byte[] { (byte) 0x1b, (byte) 0x65 })));
	}
}
