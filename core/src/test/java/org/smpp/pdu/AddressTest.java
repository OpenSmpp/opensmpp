package org.smpp.pdu;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.smpp.util.ByteBuffer;

public class AddressTest {

	// TODO: check the actual values allowed in smpp, but
	// note the API doesn't limit this AFAIK, so maybe need
	// to test the whole range the byte type offers
	private static final byte[] TONS = new byte[] {
		0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07
	};

	// TODO: check the actual values allowed in smpp, but
	// note the API doesn't limit this AFAIK, so maybe need
	// to test the whole range the byte type offers
	private static final byte[] NPIS = new byte[] {
		0x00, 0x01, 0x03, 0x04, 0x06, 0x08, 0x09, 0x0a, 0x0e, 0x0f, 0x12
	};
	private static final int MAX_ADDRESS_LENGTH = 0x21;

	private Address address;

	@Before
	public void setup() {
		address = new Address();
	}

	@Test
	public void testDefaultConstructorInitialValues() {
		assertEquals(0x00, address.getTon());
		assertEquals(0x00, address.getNpi());
		assertEquals("", address.getAddress());
	}

	// TODO: Address(int)
	// TODO: Address(byte, byte, int)
	// TODO: Address(String)
	// TODO: Address(String, int)
	// TODO: Address(byte, byte, String)
	// TODO: Address(byte, byte, String, int)

	@Test
	public void testSetData() throws Exception {
		ByteBuffer buffer = mock(ByteBuffer.class);
		when(buffer.removeByte()).thenReturn((byte) 0x01, (byte) 0x02);
		when(buffer.removeCString()).thenReturn("1234567890");

		address.setData(buffer);
		assertEquals(0x01, address.getTon());
		assertEquals(0x02, address.getNpi());
		assertEquals("1234567890", address.getAddress());
	}

	@Test
	public void testGetData() throws Exception {
		for (byte ton : TONS) {
			for (byte npi : NPIS) {
				for (int len = 1; len <= MAX_ADDRESS_LENGTH; len++) {
					String a = address(len);
					address = new Address(ton, npi, a, len + 1);
					ByteBuffer buffer = address.getData();
					assertEquals(ton, buffer.removeByte());
					assertEquals(npi, buffer.removeByte());
					assertEquals(a, buffer.removeCString());
				}
			}
		}
	}

	@Test
	public void testSetTon() {
		for (byte ton : TONS) {
			address.setTon(ton);
			assertEquals(ton, address.getTon());
		}
	}

	@Test
	public void testSetNpi() {
		for (byte npi : NPIS) {
			address.setNpi(npi);
			assertEquals(npi, address.getNpi());
		}
	}
	// TODO: Address#setAddress(String)
	// - with min length
	// - with max length
	// TODO: Address#setAddress(String, int)
	// - with min length
	// - with max length

	@Test
	public void testDebugString() throws Exception {
		for (byte ton : TONS) {
			for (byte npi : NPIS) {
				for (int len = 1; len <= MAX_ADDRESS_LENGTH; len++) {
					String a = address(len);
					address = new Address(ton, npi, a, len + 1);
					String s = "(addr: " + Integer.toString(ton) + " " + Integer.toString(npi) + " " + a + ") ";
					assertEquals(s, address.debugString());
				}
			}
		}
	}

	private static String address(int length) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			sb.append(Math.round(Math.random() * 9));
		}
		return sb.toString();
	}
}
