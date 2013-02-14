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
package org.smpp.smscsim;

import java.io.*;

import org.smpp.SmppObject;
import org.smpp.debug.*;
import org.smpp.pdu.DeliverSM;
import org.smpp.pdu.PDUException;
import org.smpp.pdu.WrongLengthOfStringException;
import org.smpp.smscsim.SimulatorPDUProcessor;
import org.smpp.smscsim.SimulatorPDUProcessorFactory;
import org.smpp.smscsim.util.Table;

/**
 * Class <code>Simulator</code> is an application class behaving as a real
 * SMSC with SMPP interface.
 * Clients (ESMEs) can bind to it, send requests to which this application
 * generates responses. It also allows to send message to the bound client.
 * It's primary use is for developers creating their SMPP applications to lessen
 * the use of real SMSC. Should any extra functionality is required,
 * the developers can add it to this application. Multiple clients are supported.
 * Transmitter/receiver/transciever bound modes are supported. The bounding clients
 * are authenticated using text file with user definitions.
 * <p>
 * This simulator application uses <code>SimulatorPDUProcessor</code> to process
 * the PDUs received from the clients.
 * <p>
 * To run this application using <b>smpp.jar</b> and <b>smscsim.jar</b> library files execute
 * the following command:
 * <p>
 * <code>java -cp smpp.jar:smscsim.jar org.smpp.smscsim.Simulator</code>
 * <p>
 * If your libraries are stored in other that default directory, use the
 * directory name in the <code>-cp</code> argument.
 * 
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Id: Simulator.java 72 2008-07-15 19:43:00Z sverkera $
 * @see SimulatorPDUProcessor
 * @see SimulatorPDUProcessorFactory
 * @see SMSCListener
 * @see SMSCSession
 * @see org.smpp.smscsim.util.BasicTableParser
 */
public class Simulator {
	static final String copyright =
		"Copyright (c) 1996-2001 Logica Mobile Networks Limited\n"
			+ "This product includes software developed by Logica by whom copyright\n"
			+ "and know-how are retained, all rights reserved.\n";

	static {
		System.out.println(copyright);
	}

	/**
	 * Name of file with user (client) authentication information.
	 */
	static String usersFileName = "etc/users.txt";

	/**
	 * Directory for creating of debug and event files.
	 */
	static final String dbgDir = "./";

	/**
	 * The debug object.
	 */
	static Debug debug = new FileDebug(dbgDir, "sim.dbg");

	/**
	 * The event object.
	 */
	static Event event = new FileEvent(dbgDir, "sim.evt");

	public static final int DSIM = 16;
	public static final int DSIMD = 17;
	public static final int DSIMD2 = 18;

	static BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));

	boolean keepRunning = true;
	private SMSCListener smscListener = null;
	private SimulatorPDUProcessorFactory factory = null;
	private PDUProcessorGroup processors = null;
	private ShortMessageStore messageStore = null;
	private DeliveryInfoSender deliveryInfoSender = null;
	private Table users = null;
	private boolean displayInfo = true;

	private Simulator() {
	}

	/**
	 * The main function of the application displays menu with available
	 * options.
	 */
	public static void main(String args[]) throws IOException {
		SmppObject.setDebug(debug);
		SmppObject.setEvent(event);
		debug.activate();
		event.activate();
		debug.deactivate(SmppObject.DRXTXD2);
		debug.deactivate(SmppObject.DPDUD);
		debug.deactivate(SmppObject.DCOMD);
		debug.deactivate(DSIMD2);
		Simulator menu = new Simulator();
		menu.menu();
	}

	/**
	 * Displays menu with available simulator options such as starting and
	 * stopping listener, listing all currently connected clients,
	 * sending of a message to a client, listing all received messages
	 * and reloading of user (client) definition file.
	 */
	protected void menu() throws IOException {
		debug.write("simulator started");

		keepRunning = true;
		String option = "1";
		int optionInt;

		while (keepRunning) {
			System.out.println();
			System.out.println("- 1 start simulation");
			System.out.println("- 2 stop simulation");
			System.out.println("- 3 list clients");
			System.out.println("- 4 send message");
			System.out.println("- 5 list messages");
			System.out.println("- 6 reload users file");
			System.out.println("- 7 log to screen " + (displayInfo ? "off" : "on"));
			System.out.println("- 0 exit");
			System.out.print("> ");
			optionInt = -1;
			try {
				option = keyboard.readLine();
				optionInt = Integer.parseInt(option);
			} catch (Exception e) {
				debug.write("exception reading keyboard " + e);
				optionInt = -1;
			}
			switch (optionInt) {
				case 1 :
					start();
					break;
				case 2 :
					stop();
					break;
				case 3 :
					listClients();
					break;
				case 4 :
					sendMessage();
					break;
				case 5 :
					messageList();
					break;
				case 6 :
					reloadUsers();
					break;
				case 7 :
					logToScreen();
					break;
				case 0 :
					exit();
					break;
				case -1 :
					// default option if entering an option went wrong
					break;
				default :
					System.out.println("Invalid option. Choose between 0 and 6.");
					break;
			}
		}

		System.out.println("Exiting simulator.");
		debug.write("simulator exited.");
	}

	/**
	 * Permits a user to choose the port where to listen on and then creates and
	 * starts new instance of <code>SMSCListener</code>.
	 * An instance of the <code>SimulatorPDUProcessor</code> is created 
	 * and this instance is passed to the <code>SMSCListener</code> which is started
	 * just after.
	 */
	protected void start() throws IOException {
		if (smscListener == null) {
			System.out.print("Enter port number> ");
			int port = Integer.parseInt(keyboard.readLine());
			System.out.print("Starting listener... ");
			smscListener = new SMSCListenerImpl(port, true);
			processors = new PDUProcessorGroup();
			messageStore = new ShortMessageStore();
			deliveryInfoSender = new DeliveryInfoSender();
			deliveryInfoSender.start();
			users = new Table(usersFileName);
			factory = new SimulatorPDUProcessorFactory(processors, messageStore, deliveryInfoSender, users);
			factory.setDisplayInfo(displayInfo);
			smscListener.setPDUProcessorFactory(factory);
			smscListener.start();
			System.out.println("started.");
		} else {
			System.out.println("Listener is already running.");
		}
	}

	/**
	 * Stops all the currently active sessions and then stops the listener.
	 */
	protected void stop() throws IOException {
		if (smscListener != null) {
			System.out.println("Stopping listener...");
			synchronized (processors) {
				int procCount = processors.count();
				SimulatorPDUProcessor proc;
				SMSCSession session;
				for (int i = 0; i < procCount; i++) {
					proc = (SimulatorPDUProcessor) processors.get(i);
					session = proc.getSession();
					System.out.print("Stopping session " + i + ": " + proc.getSystemId() + " ...");
					session.stop();
					System.out.println(" stopped.");
				}
			}
			smscListener.stop();
			smscListener = null;
			if (deliveryInfoSender != null) {
				deliveryInfoSender.stop();
			}
			System.out.println("Stopped.");
		}
	}

	/**
	 * Stops all the currently active sessions, stops the listener
	 * and the exits the application.
	 */
	protected void exit() throws IOException {
		stop();
		keepRunning = false;
	}

	/**
	 * Prints all messages currently present in the message store
	 * on the standard output.
	 */
	protected void messageList() {
		if (smscListener != null) {
			messageStore.print();
		} else {
			System.out.println("You must start listener first.");
		}
	}

	/**
	 * Reloads the user (client) definition file used for authentication of
	 * bounding ESMEs. Useful when the user setting is changed or added
	 * and restart of the simulator is not possible.
	 */
	protected void reloadUsers() {
		if (smscListener != null) {
			try {
				if (users != null) {
					users.reload();
				} else {
					users = new Table(usersFileName);
				}
				System.out.println("Users file reloaded.");
			} catch (FileNotFoundException e) {
				event.write(e, "reading users file " + usersFileName);
			} catch (IOException e) {
				event.write(e, "reading users file " + usersFileName);
			}
		} else {
			System.out.println("You must start listener first.");
		}
	}

	/**
	 * Changes the log to screen status. If logging to screen,
	 * an information about received and sent PDUs as well as about
	 * connection attempts is printed to standard output.
	 */
	protected void logToScreen() {
		if (smscListener != null) {
			synchronized (processors) {
				displayInfo = !displayInfo;
				int procCount = processors.count();
				SimulatorPDUProcessor proc;
				for (int i = 0; i < procCount; i++) {
					proc = (SimulatorPDUProcessor) processors.get(i);
					proc.setDisplayInfo(displayInfo);
				}
			}
			factory.setDisplayInfo(displayInfo);
		}
	}

	/**
	 * Prints all currently connected clients on the standard output.
	 */
	protected void listClients() {
		if (smscListener != null) {
			synchronized (processors) {
				int procCount = processors.count();
				if (procCount > 0) {
					SimulatorPDUProcessor proc;
					for (int i = 0; i < procCount; i++) {
						proc = (SimulatorPDUProcessor) processors.get(i);
						System.out.print(proc.getSystemId());
						if (!proc.isActive()) {
							System.out.println(" (inactive)");
						} else {
							System.out.println();
						}
					}
				} else {
					System.out.println("No client connected.");
				}
			}
		} else {
			System.out.println("You must start listener first.");
		}
	}

	/**
	 * Permits data to be sent to a specific client.
	 * With the id of the client set by the user, the method <code>sendMessage</code> 
	 * gets back the specific reference to the client's <code>PDUProcessor</code>.
	 * With this reference you are able to send data to the client.
	 */
	protected void sendMessage() throws IOException {
		if (smscListener != null) {
			int procCount = processors.count();
			if (procCount > 0) {
				String client;
				SimulatorPDUProcessor proc;
				listClients();
				if (procCount > 1) {
					System.out.print("Type name of the destination> ");
					client = keyboard.readLine();
				} else {
					proc = (SimulatorPDUProcessor) processors.get(0);
					client = proc.getSystemId();
				}
				for (int i = 0; i < procCount; i++) {
					proc = (SimulatorPDUProcessor) processors.get(i);
					if (proc.getSystemId().equals(client)) {
						if (proc.isActive()) {
							System.out.print("Type the message> ");
							String message = keyboard.readLine();
							DeliverSM request = new DeliverSM();
							try {
								request.setShortMessage(message);
								proc.serverRequest(request);
								System.out.println("Message sent.");
							} catch (WrongLengthOfStringException e) {
								System.out.println("Message sending failed");
								event.write(e, "");
							} catch (IOException ioe) {
							} catch (PDUException pe) {
							}
						} else {
							System.out.println("This session is inactive.");
						}
					}
				}
			} else {
				System.out.println("No client connected.");
			}
		} else {
			System.out.println("You must start listener first.");
		}
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.2  2003/09/30 09:17:49  sverkera
 * Created an interface for SMSCListener and SMSCSession and implementations of them  so that it is possible to provide other implementations of these classes.
 *
 * Revision 1.1  2003/07/23 00:28:39  sverkera
 * Imported
 *
 * 
 * Old changelog:
 * 20-09-01 ticp@logica.com added support for sending of delivery info
 * 26-09-01 ticp@logica.com debug now in a group
 */
