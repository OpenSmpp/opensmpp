/*
 * Copyright (c) 1996-2001
 * Logica Mobile Networks Limited
 * All rights reserved.
 *
 * This software is distributed under Logica Open Source License Version 1.0
 * ("Licence Agreement"). You shall use it and distribute only in accordance
 * with the terms of the License Agreement.
 *
 */
package org.smpp;

import org.smpp.util.*;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;

/**
 * Implementation of TCP/IP type of communication.
 * Covers both client (peer-to-peer) connection and server (new connections
 * from clients accepting and creating) type of connections.
 * 
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Id: TCPIPConnection.java 80 2009-10-28 02:13:18Z sverkera $
 * @see Connection
 * @see java.net.Socket
 * @see java.net.ServerSocket
 * @see java.io.BufferedInputStream
 * @see java.io.BufferedOutputStream
 */
public class TCPIPConnection extends Connection {
	/**
	 * The port number on the remote host to which the <code>socket</code>
	 * is connected or port number where <code>receiverSocket</code>
	 * is acception connections.
	 */
	private int port = 0;

	/**
	 * The TCP/IP (client) socket.
	 *
	 * @see java.net.Socket
	 */
	private Socket socket = null;

	/**
	 * An input stream for reading bytes from the <code>socket</code>.
	 *
	 * @see java.io.BufferedInputStream
	 */
	private BufferedInputStream inputStream = null;

	/**
	 * An output stream for writting bytes to the <code>socket</code>.
	 *
	 * @see java.io.BufferedOutputStream
	 */
	private BufferedOutputStream outputStream = null;

	/**
	 * Indication if the <code>socket</code> is opened.
	 */
	private boolean opened = false;

	/**
	 * The server socket used for accepting connection on <code>port</code>.
	 *
	 * @see #port
	 * @see java.net.ServerSocket
	 */
	private ServerSocket receiverSocket = null;

	/**
	 * Indicates if the connection represents client or server socket.
	 */
	private byte connType = CONN_NONE;

	/**
	 * Indicates that the connection type hasn't been set yet.
	 */
	private static final byte CONN_NONE = 0;

	/**
	 * Indicates that the connection is client type connection.
	 * @see #socket
	 */
	private static final byte CONN_CLIENT = 1;

	/**
	 * Indicates that the connection is server type connection.
	 * @see #receiverSocket
	 */
	private static final byte CONN_SERVER = 2;

	/**
	 * Default size for socket io streams buffers.
	 * @see #initialiseIOStreams(Socket)
	 */
	private static final int DFLT_IO_BUF_SIZE = 2 * 1024;

	/**
	 * Default size for the receiving buffer.
	 */
	private static final int DFLT_RECEIVE_BUFFER_SIZE = 4 * 1024;

	/**
	 * The default maximum of bytes received in one call to
	 * the <code>receive</code> function.
	 * @see #maxReceiveSize
	 * @see #receive()
	 */
	private static final int DFLT_MAX_RECEIVE_SIZE = 128 * 1024;

	/**
	 * The size for IO stream's buffers. Used by the instances of 
	 * <code>BufferedInputStream</code> which are used as sreams for accessing
	 * the socket.
	 * @see #setIOBufferSize(int)
	 */
	private int ioBufferSize = DFLT_IO_BUF_SIZE;

	/**
	 * The receiver buffer size. Can be changed by call to the function
	 * <code>setReceiveBufferSize</code>. This is the maximum count of bytes
	 * which can be read from the socket in one call to the socket's
	 * input stream's <code>read</code>.
	 * @see #setReceiveBufferSize(int)
	 * @see #receive()
	 */
	private int receiveBufferSize;

	/**
	 * The buffer for storing of received data in the <code>receive</code>
	 * function. 
	 * @see #setReceiveBufferSize(int)
	 * @see #receive()
	 */
	private byte[] receiveBuffer;

	/**
	 * Max count of bytes which can be returned from one call
	 * to the <code>receive</code> function. If the returned data seems
	 * to be incomplete, you might want to call the <code>receive</code> again.
	 * @see #setMaxReceiveSize(int)
	 * @see #receive()
	 */
	private int maxReceiveSize = DFLT_MAX_RECEIVE_SIZE;

	/**
	 * Instanciate a SocketFactory which will be used to create sockets later.
	 * Subclasses can override this field with e.g. a SSLSocketFactory
	 */
	protected SocketFactory socketFactory = SocketFactory.getDefault();

	/**
	 * Instanciate a ServerSocketFactory which will be used to create server sockets later.
	 * Subclasses can override this field with e.g. a SSLServerSocketFactory
	 */
	protected ServerSocketFactory serverSocketFactory = ServerSocketFactory.getDefault();
	
	/**
	 * Initialises the connection with port only, which means that
	 * the connection will serve as connection receiving server.
	 * The accepting of the connection must be invoked explicitly by
	 * calling of <code>accept</code> method.
	 *
	 * @param port the port number to listen on
	 */
	public TCPIPConnection(int port) {
		if ((port >= Data.MIN_VALUE_PORT) && (port <= Data.MAX_VALUE_PORT)) {
			this.port = port;
		} else {
			debug.write("Invalid port.");
		}
		connType = CONN_SERVER;
	}

	/**
	 * Initialises the connection for client communication.
	 *
	 * @param address  the address of the remote end
	 *                 of the <code>socket</code>
	 * @param port     the port number on the remote host
	 */
	public TCPIPConnection(String address, int port) {
		if (address.length() >= Data.MIN_LENGTH_ADDRESS) {
			this.address = address;
		} else {
			debug.write("Invalid address.");
		}
		if ((port >= Data.MIN_VALUE_PORT) && (port <= Data.MAX_VALUE_PORT)) {
			this.port = port;
		} else {
			debug.write("Invalid port.");
		}
		connType = CONN_CLIENT;
		setReceiveBufferSize(DFLT_RECEIVE_BUFFER_SIZE);
	}

	/**
	 * Initialises the connection with existing socket.
	 * It's intended for use with one server connection which generates
	 * new sockets and creates connections with the sockets.
	 *
	 * @param socket the socket to use for communication
	 * @see #accept()
	 */
	public TCPIPConnection(Socket socket) throws IOException {
		connType = CONN_CLIENT;
		this.socket = socket;
		address = socket.getInetAddress().getHostAddress();
		port = socket.getPort();
		initialiseIOStreams(socket);
		opened = true;
		setReceiveBufferSize(DFLT_RECEIVE_BUFFER_SIZE);
	}

	/**
	 * Opens the connection by creating new <code>Socket</code> (for client
	 * type connection) or by creating <code>ServerSocket</code> (for
	 * server type connection). If the connection is already opened,
	 * the method is not doing anything.
	 *
	 * @see #connType
	 * @see java.net.ServerSocket
	 * @see java.net.Socket
	 */
	public void open() throws IOException {
		debug.enter(DCOM, this, "open");
		IOException exception = null;

		if (!opened) {
			if (connType == CONN_CLIENT) {
				try {
					socket = socketFactory.createSocket(address, port);
					initialiseIOStreams(socket);
					opened = true;
					debug.write(DCOM, "opened client tcp/ip connection to " + address + " on port " + port);
				} catch (IOException e) {
					debug.write("IOException opening TCPIPConnection " + e);
					event.write(e, "IOException opening TCPIPConnection");
					exception = e;
				}
			} else if (connType == CONN_SERVER) {
				try {
					receiverSocket = serverSocketFactory.createServerSocket(port);
					opened = true;
					debug.write(DCOM, "listening tcp/ip on port " + port);
				} catch (IOException e) {
					debug.write("IOException creating listener socket " + e);
					exception = e;
				}
			} else {
				debug.write("Unknown connection type = " + connType);
			}
		} else {
			debug.write("attempted to open already opened connection ");
		}

		debug.exit(DCOM, this);
		if (exception != null) {
			throw exception;
		}
	}

	/**
	 * Closes the client or server connection.
	 *
	 * @see #connType
	 * @see #open()
	 */
	public void close() throws IOException {
		debug.enter(DCOM, this, "close");
		IOException exception = null;

		if (connType == CONN_CLIENT) {
			try {
				if(inputStream != null)
					inputStream.close();
				if(outputStream != null)
					outputStream.close();
				if(socket != null)
					socket.close();
				inputStream = null;
				outputStream = null;
				socket = null;
				opened = false;
				debug.write(DCOM, "closed client tcp/ip connection to " + address + " on port " + port);
			} catch (IOException e) {
				debug.write("IOException closing socket " + e);
				event.write(e, "IOException closing socket");
				exception = e;
			}
		} else if (connType == CONN_SERVER) {
			try {
				if(receiverSocket != null)
					receiverSocket.close();
				receiverSocket = null;
				opened = false;
				debug.write(DCOM, "stopped listening tcp/ip on port " + port);
			} catch (IOException e) {
				debug.write("IOException closing listener socket " + e);
				event.write(e, "IOException closing listener socket");
				exception = e;
			}
		} else {
			debug.write("Unknown connection type = " + connType);
		}

		debug.exit(DCOM, this);
		if (exception != null) {
			throw exception;
		}
	}

	/**
	 * Sends data over the connection. Must be client type connection.
	 * The timeout for sending is set by <code>setCommsTimeout</code>.
	 *
	 * @see java.net.Socket
	 */
	public void send(ByteBuffer data) throws IOException {
		debug.enter(DCOM, this, "send");
		IOException exception = null;

		if (outputStream == null) {
			debug.exit(DCOM, this);
			throw new IOException("Not connected");
		}
		if (connType == CONN_CLIENT) {
			try {
				socket.setSoTimeout((int) getCommsTimeout());
				try {
					outputStream.write(data.getBuffer(), 0, data.length());
					debug.write(DCOM, "sent " + data.length() + " bytes to " + address + " on port " + port);
				} catch (IOException e) {
					debug.write("IOException sending data " + e);
					exception = e;
				}
				outputStream.flush();
			} catch (IOException e) {
				debug.write("IOException flushing data " + e);
				if (exception == null) {
					exception = e;
				}
			}
		} else if (connType == CONN_SERVER) {
			debug.write("Attempt to send data over server type connection.");
		} else {
			debug.write("Unknown connection type = " + connType);
		}

		debug.exit(DCOM, this);
		if (exception != null) {
			throw exception;
		}

	}

	/**
	 * Reads data from the connection. Must be client type connection.
	 * The timeout for receiving is set by <code>setReceiveTimeout</code>.
	 * The timeout for single attempt to read something from socket is 
	 * set by <code>setCommsTimeout</code>.
	 *
	 * @see #setReceiveBufferSize(int)
	 * @see #setMaxReceiveSize(int)
	 * @see Connection#getCommsTimeout()
	 * @see java.net.Socket
	 */
	public ByteBuffer receive() throws IOException {
		debug.enter(DCOMD, this, "receive");
		IOException exception = null;

		ByteBuffer data = null;
		if (connType == CONN_CLIENT) {
			data = new ByteBuffer();
			long endTime = Data.getCurrentTime() + getReceiveTimeout();
			//int bytesAvailable = 0;
			int bytesToRead = 0;
			int bytesRead = 0;
			int totalBytesRead = 0;

			try {
				socket.setSoTimeout((int) getCommsTimeout());
				bytesToRead = receiveBufferSize;
				debug.write(DCOMD, "going to read from socket");
				debug.write(
					DCOMD,
					"comms timeout="
						+ getCommsTimeout()
						+ " receive timeout="
						+ getReceiveTimeout()
						+ " receive buffer size="
						+ receiveBufferSize);
				do {
					bytesRead = 0;
					try {
						bytesRead = inputStream.read(receiveBuffer, 0, bytesToRead);
					} catch (InterruptedIOException e) {
						// comms read timeout expired, no problem
						debug.write(DCOMD, "timeout reading from socket");
					}
					if (bytesRead > 0) {
						debug.write(DCOMD, "read " + bytesRead + " bytes from socket");
						data.appendBytes(receiveBuffer, bytesRead);
						totalBytesRead += bytesRead;
					}
					if (bytesRead == -1) {
						debug.write(DCOMD, "reached end of stream");
						close();
						throw new EOFException("Reached end of stream");
					}

					bytesToRead = inputStream.available();
					if (bytesToRead > 0) {
						debug.write(DCOMD, "more data (" + bytesToRead + " bytes) remains in the socket");
					} else {
						debug.write(DCOMD, "no more data remains in the socket");
					}
					if (bytesToRead > receiveBufferSize) {
						bytesToRead = receiveBufferSize;
					}
					if (totalBytesRead + bytesToRead > maxReceiveSize) {
						// would be more than allowed
						bytesToRead = maxReceiveSize - totalBytesRead;
					}
				} while (
					((bytesToRead != 0) && (Data.getCurrentTime() <= endTime)) && (totalBytesRead < maxReceiveSize));

				debug.write(DCOM, "totally read " + data.length() + " bytes from socket");
			} catch (IOException e) {
				debug.write("IOException: " + e.getMessage());
				event.write(e, "IOException receive via TCPIPConnection");
				exception = e;
				close();
			}
		} else if (connType == CONN_SERVER) {
			debug.write("Attempt to receive data from server type connection.");
		} else {
			debug.write("Unknown connection type = " + connType);
		}

		debug.exit(DCOMD, this);
		if (exception != null) {
			throw exception;
		}
		return data;
	}

	/**
	 * Accepts new connection on server type connection, i.e. on ServerSocket.
	 * If new socket is returned from ServerSocket.accept(), creates new
	 * instance of TCPIPConnection with the new socket and returns it,
	 * otherwise returns null. The timeout for new connection accept is
	 * set by <code>setReceiveTimeout</code>, i.e. waits for new connection
	 * for this time and then, if none is requested, returns with null.
	 *
	 * @see #TCPIPConnection(Socket)
	 * @see java.net.ServerSocket#accept()
	 * @see java.net.ServerSocket#setSoTimeout(int)
	 */
	public Connection accept() throws IOException {
		debug.enter(DCOMD, this, "receive");
		IOException exception = null;

		Connection newConn = null;
		if (connType == CONN_SERVER) {
			try {
				receiverSocket.setSoTimeout((int) getReceiveTimeout());
			} catch (SocketException e) {
				// don't care, we're just setting the timeout
			}
			Socket acceptedSocket = null;
			try {
				acceptedSocket = receiverSocket.accept();
			} catch (IOException e) {
				debug.write(DCOMD, "Exception accepting socket (timeout?)" + e);
			}
			if (acceptedSocket != null) {
				try {
					newConn = new TCPIPConnection(acceptedSocket);
				} catch (IOException e) {
					debug.write("IOException creating new client connection " + e);
					event.write(e, "IOException creating new client connection");
					exception = e;
				}
			}
		} else if (connType == CONN_CLIENT) {
			debug.write("Attempt to receive data from client type connection.");
		} else {
			debug.write("Unknown connection type = " + connType);
		}

		debug.exit(DCOMD, this);
		if (exception != null) {
			throw exception;
		}
		return newConn;
	}

	/**
	 * Initialises input and output streams to the streams from socket
	 * for client type connection.
	 * Streams are used for sending and receiving data from the socket.
	 *
	 * @see #inputStream
	 * @see #outputStream
	 * @see java.net.Socket#getInputStream()
	 * @see java.net.Socket#getOutputStream()
	 */
	private void initialiseIOStreams(Socket socket) throws IOException {
		if (connType == CONN_CLIENT) {
			inputStream = new BufferedInputStream(socket.getInputStream(), ioBufferSize);
			outputStream = new BufferedOutputStream(socket.getOutputStream(), ioBufferSize);
		} else if (connType == CONN_SERVER) {
			debug.write("Attempt to initialise i/o streams for server type connection.");
		} else {
			debug.write("Unknown connection type = " + connType);
		}
	}

	/**
	 * Sets the size for the io buffers of streams for accessing the socket.
	 * The size can only be changed before actual opening of the connection.
	 * @see #initialiseIOStreams(Socket)
	 */
	public void setIOBufferSize(int ioBufferSize) {
		if (!opened) {
			this.ioBufferSize = ioBufferSize;
		}
	}

	/**
	 * Sets the size of the receiving buffer, which is used for reading from
	 * the socket. A buffer of this size is allocated for the reading.
	 * @see #receive()
	 */
	public void setReceiveBufferSize(int receiveBufferSize) {
		this.receiveBufferSize = receiveBufferSize;
		receiveBuffer = new byte[receiveBufferSize];
	}

	/**
	 * Sets the maximum size of the data which can be read in one call to
	 * the <code>receive</code> function. After reading of this amount
	 * of bytes the receive returns the data read even if there are more data
	 * in the socket.
	 * @see #receive()
	 */
	public void setMaxReceiveSize(int maxReceiveSize) {
		this.maxReceiveSize = maxReceiveSize;
	}

	/**
	 * @see org.smpp.Connection#isOpened()
	 */
	public boolean isOpened() {
		return opened;
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.2  2003/12/16 14:47:20  sverkera
 * Added a close when reaching end-of-stream
 *
 * Revision 1.1  2003/07/23 00:28:39  sverkera
 * Imported
 *
 * 
 * Old changelog:
 * 26-09-01 ticp@logica.com debug code categorized to groups
 * 27-09-01 ticp@logica.com receive() rewritten not to consume cpu time while
 *						    waiting for data on socket
 * 27-09-01 ticp@logica.com added customizable limit on maximum received bytes
 *						    in one call to receive()
 * 27-09-01 ticp@logica.com added prealocated buffer for socket reads
 *						    with customizable size
 * 28-09-01 ticp@logica.com the io streams buffer size is customizable now
 * 01-10-01 ticp@logica.com traces added
 */
