package org.smpp;

import static org.junit.Assert.assertTrue;

import org.smpp.pdu.PDU;
import org.smpp.pdu.Response;

/**
 * An embedded SMPP server for tests to run against.
 *
 */
class TestServer extends SmppObject implements Runnable {
	private PduResponder responder;
	private volatile boolean keepRunning = true;
	private volatile boolean shutDown = false;
	private volatile boolean running = false;
	private final int listenPort;
	private TCPIPConnection serverConn = null;

	// The defaults in Data are all around 60 seconds - set lower to speed up test completion:
	private static final int GLOBAL_DEFAULT_TIMEOUT = 4 * 1000;
	private long connectionReceiveTimeout = GLOBAL_DEFAULT_TIMEOUT;
	private int connectionConnectTimeout = GLOBAL_DEFAULT_TIMEOUT;
	private long connectionSocketCommsTimeout = GLOBAL_DEFAULT_TIMEOUT;
	private long receiverReceiveTimeout = GLOBAL_DEFAULT_TIMEOUT;
	private long acceptedSocketCommsTimeout = GLOBAL_DEFAULT_TIMEOUT;
	private long acceptedSocketReceiveTimeout = GLOBAL_DEFAULT_TIMEOUT;
	// This setting is especially detrimental to quick server shutdowns (but the global default is
	// low enough to limit the impact: https://github.com/OpenSmpp/opensmpp/issues/33)
	private long receiverQueueWaitTimeout = Data.QUEUE_TIMEOUT;
	
	public void stop() {
		this.keepRunning = false;
	}
	public boolean isShutDown() {
		return this.shutDown;
	}
	public boolean isRunning() {
		return running;
	}
	/**
	 * 
	 * @param listenPort a port to listen on, or 0 to use an ephemeral port
	 * @param responder
	 */
	public TestServer(int listenPort, PduResponder responder) {
		this.listenPort = listenPort;
		this.responder = responder;
	}
	
	public void run() {
		try {
			serverEvent("Trying to listen on port: " + ((listenPort > 0) ? String.valueOf(listenPort) : "<ephemeral>"));
			serverConn = new org.smpp.TCPIPConnection(listenPort);
			serverConn.setCommsTimeout(getConnectionSocketCommsTimeout());
			serverConn.setConnectionTimeout(getConnectionConnectTimeout());
			serverConn.setReceiveTimeout(getConnectionReceiveTimeout());
			serverConn.open();
			serverEvent("Listening on port: " + serverConn.getPort());
			running = true;

			Connection connection = serverConn.accept();
			connection.setCommsTimeout(acceptedSocketCommsTimeout);
			connection.setReceiveTimeout(acceptedSocketReceiveTimeout);
			serverEvent("Connection accepted");

			Transmitter transmitter = new Transmitter(connection);
			Receiver receiver = new ServerReceiver(transmitter, connection);

			serverEvent("Starting receiver");
			receiver.setReceiveTimeout(getReceiverReceiveTimeout());
			receiver.setQueueWaitTimeout(getReceiverQueueWaitTimeout());
			receiver.start();
			
			serverDebug("Trying to receive PDUs...");
			while (keepRunning) {
				// While this may look like it won't keep things hung up for long, it is in fact entirely
				// dependent on receiver.queueWaitTimeout in certain cases (i.e. if a PDU isn't immediately
				// available in the pduQueue - see Receiver.tryReceivePDU()):
				PDU pdu = receiver.receive(500);
				if (pdu == null) { continue; }
				serverDebug("PDU received: " + pdu.debugString());
				assertTrue(pdu.isOk());
				assertTrue(pdu.isRequest());

				PDU responsePdu = responder.getResponse(pdu);
				
				serverDebug("Sending response: " + responsePdu.debugString());
				transmitter.send(responsePdu);
				serverDebug("Response sent: " + responsePdu);
			}
			serverEvent("shutting down");
			receiver.stop();
			serverConn.close();
			serverEvent("shut down");
		} catch (Exception e) {
			serverEvent("Exception shutting down: " + e);
			e.printStackTrace();
		} finally {
			this.shutDown = true;
			running = false;
		}
	}
	
	private long getConnectionReceiveTimeout() {
		return connectionReceiveTimeout;
	}
	public void setConnectionReceiveTimeout(long connectionReceiveTimeout) {
		this.connectionReceiveTimeout = connectionReceiveTimeout;
	}

	private int getConnectionConnectTimeout() {
		return connectionConnectTimeout;
	}
	public void setConnectionConnectTimeout(int connectionConnectTimeout) {
		this.connectionConnectTimeout = connectionConnectTimeout;
	}

	private long getConnectionSocketCommsTimeout() {
		return connectionSocketCommsTimeout;
	}
	public void setConnectionSocketCommsTimeout(long connectionSocketCommsTimeout) {
		this.connectionSocketCommsTimeout = connectionSocketCommsTimeout;
	}

	private long getReceiverQueueWaitTimeout() {
		return receiverQueueWaitTimeout;
	}
	public void setReceiverQueueWaitTimeout(long receiverQueueWaitTimeout) {
		this.receiverQueueWaitTimeout = receiverQueueWaitTimeout;
	}

	private long getReceiverReceiveTimeout() {
		return receiverReceiveTimeout;
	}
	public void setReceiverReceiveTimeout(long receiverReceiveTimeout) {
		this.receiverReceiveTimeout = receiverReceiveTimeout;
	}
	
	public long getAcceptedSocketCommsTimeout() {
		return acceptedSocketCommsTimeout;
	}
	public void setAcceptedSocketCommsTimeout(long acceptedSocketCommsTimeout) {
		this.acceptedSocketCommsTimeout = acceptedSocketCommsTimeout;
	}
	
	public long getAcceptedSocketReceiveTimeout() {
		return acceptedSocketReceiveTimeout;
	}
	public void setAcceptedSocketReceiveTimeout(long acceptedSocketReceiveTimeout) {
		this.acceptedSocketReceiveTimeout = acceptedSocketReceiveTimeout;
	}

	public int getPort() {
		int iterations = 0;
		int sleepTime = 100;
		while (!running) {
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) { }
			if (sleepTime * iterations >  5 * 1000) {
				throw new RuntimeException("Could not get port - server not found running within 5 seconds");
			}
		}
		return serverConn.getPort();
	}

	class ServerReceiver extends Receiver {
		public ServerReceiver(Connection connection) {
			super(connection);
		}
		public ServerReceiver(Transmitter transmitter, Connection connection) {
			super(transmitter, connection);
		}
		@Override
		public String getThreadName() {
			return "ServerReceiver";
		}		
	}

	interface PduResponder {
		public Response getResponse(PDU pdu);
	}

	void serverDebug (String s) {
		debug.write("[Server]: " + s);
	}
	void serverEvent (String s) {
		event.write("[Server]: " + s);
	}	

}
