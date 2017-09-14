package org.smpp.charset;

import static org.junit.Assert.assertNotNull;

import java.nio.charset.Charset;

import org.junit.Test;

public class Gsm7BitCharsetProviderTest {

	@Test
	public void testCharsetName() {
		Gsm7BitCharsetProvider provider = new Gsm7BitCharsetProvider();
		Charset charset = provider.charsetForName("X-Gsm7Bit");

		assertNotNull(charset);
	}
}
