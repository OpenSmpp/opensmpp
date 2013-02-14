package org.smpp.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class UnprocessedTest {

	private Unprocessed unprocessed;

	@Before
	public void setup() {
		unprocessed = new Unprocessed();
	}

	@Test
	public void testInitialState() {
		assertEquals(0, unprocessed.getExpected());
		assertEquals(0, unprocessed.getLastTimeReceived());
		assertFalse(unprocessed.getHasUnprocessed());
	}

	@Test
	public void testReset() {
		unprocessed.getUnprocessed().setBuffer(new byte[] { 0x00 });
		unprocessed.setExpected(1);
		unprocessed.setHasUnprocessed(true);
		unprocessed.setLastTimeReceived(1);

		unprocessed.reset();

		assertNull(unprocessed.getUnprocessed().getBuffer());
		assertEquals(0, unprocessed.getExpected());
		assertFalse(unprocessed.getHasUnprocessed());
		// specifically, this is not reset, for reasons unknown
		assertEquals(1, unprocessed.getLastTimeReceived());
	}

	@Test
	public void testCheckWithNullBuffer() {
		unprocessed.getUnprocessed().setBuffer(null);
		unprocessed.check();
		assertFalse(unprocessed.getHasUnprocessed());
	}

	@Test
	public void testCheckWithEmptyBuffer() {
		unprocessed.getUnprocessed().setBuffer(new byte[] {});
		unprocessed.check();
		assertFalse(unprocessed.getHasUnprocessed());
	}

	@Test
	public void testCheckWithBuffer() {
		unprocessed.getUnprocessed().setBuffer(new byte[] { 0x00 });
		unprocessed.check();
		assertTrue(unprocessed.getHasUnprocessed());
	}

	@Test
	public void testSetUnprocessed() {
		unprocessed.setHasUnprocessed(true);
		assertTrue(unprocessed.getHasUnprocessed());
	}

	@Test
	public void testSetExpected() {
		unprocessed.setExpected(1234);
		assertEquals(1234, unprocessed.getExpected());
	}

	@Test
	public void testSetTimeLastReceivedWithValue() {
		unprocessed.setLastTimeReceived(1234L);
		assertEquals(1234L, unprocessed.getLastTimeReceived());
	}

	@Test
	public void testSetTimeLastReceived() {
		unprocessed.setLastTimeReceived();
		assertTrue(Math.abs(unprocessed.getLastTimeReceived() - System.currentTimeMillis()) < 5);
	}
}
