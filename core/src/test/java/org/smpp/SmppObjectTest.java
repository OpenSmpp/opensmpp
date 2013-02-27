package org.smpp;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.AfterClass;
import org.junit.Test;
import org.smpp.debug.Debug;
import org.smpp.debug.DefaultDebug;
import org.smpp.debug.DefaultEvent;
import org.smpp.debug.Event;

public class SmppObjectTest {

	@AfterClass
	public static void tearDownClass() {
		SmppObject.setDebug(new DefaultDebug());
		SmppObject.setEvent(new DefaultEvent());
	}

	@Test
	public void testDRXTXConstant() {
		assertEquals(1, SmppObject.DRXTX);
	}
	@Test
	public void testDRXTXDConstant() {
		assertEquals(2, SmppObject.DRXTXD);
	}
	@Test
	public void testDRXTXD2Constant() {
		assertEquals(3, SmppObject.DRXTXD2);
	}
	@Test
	public void testDSESSConstant() {
		assertEquals(4, SmppObject.DSESS);
	}
	@Test
	public void testDPDUConstant() {
		assertEquals(5, SmppObject.DPDU);
	}
	@Test
	public void testDPDUDConstant() {
		assertEquals(6, SmppObject.DPDUD);
	}
	@Test
	public void testDCOMConstant() {
		assertEquals(7, SmppObject.DCOM);
	}
	@Test
	public void testDCOMDConstant() {
		assertEquals(8, SmppObject.DCOMD);
	}
	@Test
	public void testDUTLConstant() {
		assertEquals(9, SmppObject.DUTL);
	}
	@Test
	public void testDUTLDConstant() {
		assertEquals(10, SmppObject.DUTLD);
	}

	@Test
	public void testDefaultDebug() {
		assertEquals(DefaultDebug.class, SmppObject.getDebug().getClass());
	}
	@Test
	public void testDefaultEvent() {
		assertEquals(DefaultEvent.class, SmppObject.getEvent().getClass());
	}

	@Test
	public void testSetDebug() {
		Debug debug = mock(Debug.class);
		SmppObject.setDebug(debug);
		assertEquals(debug, SmppObject.getDebug());
	}
	@Test
	public void testSetEvent() {
		Event event = mock(Event.class);
		SmppObject.setEvent(event);
		assertEquals(event, SmppObject.getEvent());
	}
}
