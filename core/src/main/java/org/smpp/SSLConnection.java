/*
 * Created on 2004-jul-12
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.smpp;

import java.io.IOException;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * @author Sverker Abrahamsson
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class SSLConnection extends TCPIPConnection {

	/**
	 * Override the superclass SocketFactory with a SSLSocketFactory
	 */
	protected SocketFactory socketFactory = SSLSocketFactory.getDefault();

	/**
	 * Override the superclass ServerSocketFactory with a SSLServerSocketFactory
	 */
	protected ServerSocketFactory serverSocketFactory = SSLServerSocketFactory.getDefault();

	/**
	 * Initialises the connection with port only, which means that
	 * the connection will serve as connection receiving server.
	 * The accepting of the connection must be invoked explicitly by
	 * calling of <code>accept</code> method.
	 *
	 * @param port the port number to listen on
	 */
	public SSLConnection(int port) {
		super(port);
	}

	/**
	 * Initialises the connection for client communication.
	 *
	 * @param address  the address of the remote end
	 *                 of the <code>socket</code>
	 * @param port     the port number on the remote host
	 */
	public SSLConnection(String address, int port) {
		super(address, port);
	}

	/**
	 * Initialises the connection with existing socket.
	 * It's intended for use with one server connection which generates
	 * new sockets and creates connections with the sockets.
	 *
	 * @param sslsocket the socket to use for communication
	 * @see #accept()
	 * @throws IOException
	 */
	public SSLConnection(SSLSocket sslsocket) throws IOException {
		super(sslsocket);
	}
}
