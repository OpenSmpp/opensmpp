package org.smpp.test;

import org.smpp.pdu.BindReceiver;
import org.smpp.pdu.BindTransciever;

/**
 * Reproducible demonstration of bug #1029141. PDU was instantiating subclasses
 * of itself during (synchronized) class initialisation. A race condition
 * existed whereby, say, two threads would try to instantiate two different
 * subclasses of PDU. One thread (X) would get the lock on subclass A, and then
 * on PDU.class (following the constructor hierarchy). Another thread (Y) would
 * seemingly get the lock on its own target subclass (B). Thread X would then
 * try to construct B via static initialisation, causing a deadlock.
 * 
 * Being related to initialisation, this deadlock would likely be experienced by
 * users (if at all) only at JVM startup time, and then only under certain
 * conditions, and perhaps also intermittently.
 * 
 * Solved via:
 * 
 * @see org.smpp.pdu.PDUFactory
 * 
 * @author Paolo Campanella, BulkSMS.com.
 * 
 */
public class PDUInitDeadlockTest {
	static private int TYPE_RECEIVER = 1;
	static private int TYPE_TRANSCEIVER = 2;
	
	private PDUInitDeadlockTest() {
		// Create two or more test threads:
		int numThreads = 2;
		
		System.out.println("Creating PDU creator threads");
		PDUCreatorThread[] threads = new PDUCreatorThread[numThreads];
		boolean makeReceiver = false;

		for (int i = 0; i < numThreads; i++) {
			if (makeReceiver) {
				threads[i] = new PDUCreatorThread(TYPE_RECEIVER, "Receiver" + i);
			} else {
				threads[i] = new PDUCreatorThread(TYPE_TRANSCEIVER, "Transceiver" + i);
			}
			makeReceiver = !makeReceiver;
		}
		
		for (int i = 0; i < numThreads; i++) {
			threads[i].start();
			// On my workstation, anything over about 400ms gap between
			// starting threads masks the problem (gives the PDU
			// init code enough time to initialise):
			// Thread.sleep(400);
		}

		try {
			Thread.sleep(500);
			System.out.println("PDU creator threads started - please wait...");
			Thread.sleep(5000);
			boolean threadsAlive = false; 
			for (int i = 0; i < numThreads; i++) {
				if (threads[i].isAlive()) {
					threadsAlive = true;
					System.out.println("Thread " + threads[i].getName() + 
							" is alive. Here is its stack trace: ");
					System.out.println("(stacktrace - TODO)");
					//threads[i].getStackTrace()
				}
			}
			if (threadsAlive) {
				Thread.sleep(200);
				System.out.println("Some threads are still alive (after 5 seconds) - likely deadlock!");
				System.out.println("   - interrupting all living threads...");
				for (int i = 0; i < numThreads; i++) {
					if (threads[i].isAlive()) {
						threads[i].interrupt();
					}
				}
				Thread.sleep(1000);
			} else {
				System.out.println("All threads have exited correctly.");
			}
		} catch (InterruptedException e2) {
			e2.printStackTrace();
		}

		System.out.println("Exiting");
		System.exit(0);		
	}

	public static void main(String[] args) {
		new PDUInitDeadlockTest();
	}
	
	class PDUCreatorThread extends Thread {
		int type;
		public PDUCreatorThread(int type, String name) {
			this.type = type;
			this.setName(name);
		}
		public void run() {
			System.out.println(getName() + ": starting");
			// Doesn't need to be BindReceiver and BindTransciever - you
			// can use any PDU types to cause a deadlock. 
			if (type == TYPE_RECEIVER) {
				new BindReceiver();
			}
			else if (type == TYPE_TRANSCEIVER) {
				new BindTransciever();
			}
			System.out.println(getName() + ": done");
		}
	}

}

