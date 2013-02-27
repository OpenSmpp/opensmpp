package org.smpp.debug;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DefaultDebugTest {
	private static final Object OBJECT = new Object() {
		@Override public String toString() { return "OBJECT"; }
	};
	private static final String NAME = "NAME";
	private static final String EMPTY = "";

	private ByteArrayOutputStream out;

	private DefaultDebug debug;

	@Before
	public void setup() {
		debug = new DefaultDebug();
		debug.activate();
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
	public void testEnterWithName() {
		debug.enter(0, OBJECT, NAME);
		assertEquals("-> OBJECT NAME\n", out.toString());
	}
	@Test
	public void testEnterWithoutName() {
		debug.enter(0, OBJECT, EMPTY);
		assertEquals("-> OBJECT\n", out.toString());
	}
	@Test
	public void testExit() {
		debug.exit(0, OBJECT);
		assertEquals("<- OBJECT\n", out.toString());
	}
	@Test
	public void testWrite() {
		debug.write(0, "HELLO");
		assertEquals(" HELLO\n", out.toString());
	}
	@Test
	public void testNestedEnter() {
		debug.enter(OBJECT, NAME);
		debug.enter(OBJECT, NAME);
		assertEquals("-> OBJECT NAME\n  -> OBJECT NAME\n", out.toString());
	}
	@Test
	public void testNestedWrite() {
		debug.enter(0, OBJECT, NAME);
		debug.write(0, "HELLO");
		assertEquals("-> OBJECT NAME\n   HELLO\n", out.toString());
	}
	@Test
	public void testDeactivate() {
		debug.deactivate();
		debug.write(0, "HELLO");
		assertEquals("", out.toString());
	}
	@Test
	public void testDeactiveGroupDoesNothing() {
		debug.deactivate(0);
		debug.write(0, "HELLO");
		assertEquals(" HELLO\n", out.toString());
	}
	@Test
	public void testActivateGroupDoesNothing() {
		debug.deactivate();
		debug.write("HELLO");
		assertEquals("", out.toString());
		debug.activate(0);
		debug.write(0, "HELLO");
		assertEquals("", out.toString());
	}
	@Test
	public void testActiveIsAlwaysTrue() {
		assertTrue(debug.active(0));
		debug.deactivate(0);
		assertTrue(debug.active(0));
		debug.deactivate();
		assertTrue(debug.active(0));
	}
}
