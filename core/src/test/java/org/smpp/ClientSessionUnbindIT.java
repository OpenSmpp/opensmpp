package org.smpp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.smpp.TestServer.PduResponder;
import org.smpp.pdu.BindRequest;
import org.smpp.pdu.BindResponse;
import org.smpp.pdu.BindTransmitter;
import org.smpp.pdu.Response;
import org.smpp.pdu.Unbind;
import org.smpp.pdu.UnbindResp;

public class ClientSessionUnbindIT extends SmppObject {
	int listenPort = 0;
	PduResponder responder;
	TestServer testServer;
	Session clientSession;

    
	@Before
	public void setup() {
		// Uncomment to enable:
		// TestUtils.eventsToStdout();
		// TestUtils.debuggingToStdout();
		
		responder = Mockito.mock(PduResponder.class);
		
		testServer = new TestServer(0, responder); // 0: run server on ephemeral port

		Thread serverThread = new Thread(testServer, "server");
		serverThread.setDaemon(true);
		serverThread.start();
		listenPort = testServer.getPort(); // Find ephemeral port allocated
	}
	
	@After
	public void tearDown() throws InterruptedException {
		// Shut the server down if a test hasn't already chosen to do so itself:
		if (!testServer.isShutDown()) { testServer.stop(); }
		while (!testServer.isShutDown()) { Thread.sleep(100); }
	}
	
	/**
	 * See https://github.com/OpenSmpp/opensmpp/issues/33
	 */
	@Test(timeout=5000)
	public void unbindSucceedsWithinFiveSeconds() throws Exception {
		// Too high a commsTimeout on the clientConnection will cause this test to time out:
		bind();
		when(responder.getResponse(any(Unbind.class))).thenAnswer(mockLoggingUnbindResponse());
		unbind();
	}
	
	@Test(timeout=8000)
	public void serverShutsDownWithinEightSeconds() throws Exception {
		bind();
		when(responder.getResponse(any(Unbind.class))).thenAnswer(mockLoggingUnbindResponse());
		unbind();
		// This should make the test time out if e.g. the receiverQueueWaitTimeout is too long in TestServer
		// (and there has been certain async activity on the link, such at the unbind above):
		testServer.stop();
		while (!testServer.isShutDown()) { Thread.sleep(100); }
	}
	
	private Answer<Response> mockLoggingUnbindResponse( ) {
		return new Answer<Response>() {
			public Response answer(InvocationOnMock invocation) throws Throwable {
				return ((Unbind) invocation.getArguments()[0]).getResponse();
			}
		};
	}

	private void unbind() throws Exception {
		clientDebug("Sending unbind request");
		UnbindResp unbindResp = clientSession.unbind();
		assertFalse(clientSession.isBound());
		assertNotNull(unbindResp);
		assertTrue(unbindResp.isOk());
		clientDebug("Unbind: success");
	}
	
	private void bind() throws Exception {
		clientEvent("connecting");
		TCPIPConnection clientConnection = new TCPIPConnection("127.0.0.1", listenPort);
		clientConnection.setReceiveTimeout(2 * 1000);
		// The setting is especially detrimental to quick client shutdowns on unbind, but the global
		// setting is low enough to limit the impact: https://github.com/OpenSmpp/opensmpp/issues/33
		clientConnection.setCommsTimeout(Data.COMMS_TIMEOUT);

		clientSession = new Session(clientConnection);
		clientSession.open();
		
		BindRequest bindReq = new BindTransmitter();
		bindReq.assignSequenceNumber();
		bindReq.setSystemId("asd");
		bindReq.setPassword("asd");
		bindReq.setSystemType("asd");
		bindReq.setInterfaceVersion((byte) 0x34);
		bindReq.setAddressRange("1*");

		Response bindResp = ((BindRequest) bindReq).getResponse();
		bindResp.setCommandStatus(Data.ESME_ROK);
		when(responder.getResponse(any(BindRequest.class))).thenReturn(bindResp);

		clientEvent("Sending bind request");
		ServerPDUEventListener listener = new ServerPDUEventListener(){
			public void handleEvent(ServerPDUEvent event) {
				debug.write("Event: " + event.getPDU().debugString());
			}};
		BindResponse response = clientSession.bind(bindReq, listener);
		assertNotNull(response);
		clientEvent("Bind response " + response.debugString());
		assertEquals(Data.ESME_ROK, response.getCommandStatus());
		assertEquals(bindResp.getSequenceNumber(), response.getSequenceNumber());
	}

	
	private void clientEvent (String s) {
		event.write("[Client]: " + s);
	}
	private void clientDebug (String s) {
		debug.write("[Client]: " + s);
	}

}
