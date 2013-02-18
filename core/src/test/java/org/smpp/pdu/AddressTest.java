package org.smpp.pdu;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.smpp.Data;
import org.smpp.util.ByteBuffer;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Data.class)
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

	@Test
	public void testDefaultsFromData() throws Exception {
		PowerMockito.mockStatic(Data.class);
		Mockito.when(Data.getDefaultTon()).thenReturn((byte) 0x11);
		Mockito.when(Data.getDefaultNpi()).thenReturn((byte) 0x12);
		address = new Address();
		assertEquals(0x11, address.getTon());
		assertEquals(0x12, address.getNpi());
	}

	@Test
	public void testDefaultMaxAddressLengthAllpws20Digits() throws Exception {
		String a = address(20);
		address.setAddress(a);
		assertEquals(a, address.getAddress());
	}
	@Test(expected = WrongLengthOfStringException.class)
	public void testDefaultMaxAddressLengthDissalows21Digits() throws Exception {
		address.setAddress(address(21));
	}

	@Test(expected = WrongLengthOfStringException.class)
	public void testConstructorIntSpecifiesAddressMaxLength() throws Exception {
		address = new Address(5);
		address.setAddress(address(5));
	}

	@Test(expected = WrongLengthOfStringException.class)
	public void testConstructorByteByteIntSpecifiesTonNpiLength() throws Exception {
		address = new Address((byte) 0x01, (byte) 0x02, 5);
		assertEquals(0x01, address.getTon());
		assertEquals(0x02, address.getNpi());
		address.setAddress(address(5));
	}

	@Test
	public void testConstructorStringSpecifiesAddress() throws Exception {
		assertEquals("1234", new Address("1234").getAddress());
	}

	@Test(expected = WrongLengthOfStringException.class)
	public void testConstructorStringUsesDefaultMaxLength() throws Exception {
		new Address(address(21));
	}

	@Test(expected = WrongLengthOfStringException.class)
	public void testConstructorStringIntSpecifiesAddressLength() throws Exception {
		new Address(address(5), 5);
	}

	@Test
	public void testConstructorByteByteString() throws Exception {
		address = new Address((byte) 0x01, (byte) 0x02, "ABC");
		assertEquals(0x01, address.getTon());
		assertEquals(0x02, address.getNpi());
		assertEquals("ABC", address.getAddress());
	}

	@Test(expected = WrongLengthOfStringException.class)
	public void testConstructorByteByteStringInt() throws Exception {
		address = new Address((byte) 0x01, (byte) 0x02, "ABC", 4);
		assertEquals(0x01, address.getTon());
		assertEquals(0x02, address.getNpi());
		assertEquals("ABC", address.getAddress());
		address = new Address((byte) 0x01, (byte) 0x02, "ABCD", 4);
	}

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

	@Test
	public void testGetAddressWithEncoding() throws Exception {
		address.setAddress("ABCD");
		assertEquals("\u4142\u4344", address.getAddress("UTF-16BE"));
	}

	@Test
	public void testGetAddressWithInvalidEncoding() throws Exception {
		address.setAddress("ABCD");
		assertEquals("ABCD", address.getAddress("X-INVALID"));
	}

	@Test
	public void testSetAddressInt() throws Exception {
		String a = address(9);
		address.setAddress(a, 10);
		assertEquals(a, address.getAddress());
	}
	@Test(expected = WrongLengthOfStringException.class)
	public void testSetAddressIntLimitsLength() throws Exception {
		address.setAddress(address(10), 10);
	}

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
