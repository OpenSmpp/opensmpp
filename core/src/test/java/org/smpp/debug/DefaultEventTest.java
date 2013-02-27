package org.smpp.debug;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DefaultEventTest {

	private ByteArrayOutputStream out;

	private DefaultEvent event;

	@Before
	public void setup() {
		event = new DefaultEvent();
		event.activate();
	}

	@Before
	public void setupStreams() {
		out = new ByteArrayOutputStream();

		System.setOut(new PrintStream(out));
	}

	@After
	public void cleanUpStreams() {
		System.setOut(null);
	}

	@Test
	public void testInactiveByDefault() {
		event = new DefaultEvent();
		event.write("HELLO");
		assertEquals("", out.toString());
	}
	@Test
	public void testWrite() {
		event.write("HELLO");
		assertEquals("HELLO\n", out.toString());
	}
	@Test
	public void testWriteException() {
		try { throw new RuntimeException(); }
		catch (Exception ex) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ex.printStackTrace(new PrintStream(baos));

			String expected = "Exception: " + baos.toString() + " END\n";
			event.write(ex, "END");
			assertEquals(expected, out.toString());
		}
	}
	@Test
	public void testDeactivate() {
		event.deactivate();
		event.write("HELLO");
		assertEquals("", out.toString());
	}
}
