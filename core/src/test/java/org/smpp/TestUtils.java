package org.smpp;

import org.smpp.debug.DefaultDebug;
import org.smpp.debug.DefaultEvent;

public class TestUtils {

	/**
	 * Switch on debugging to stdout.
	 */
	public static void debuggingToStdout() {
		DebugWithThreadName debugWithThreadName = new TestUtils.DebugWithThreadName();
		debugWithThreadName.activate();
		SmppObject.setDebug(debugWithThreadName);
	}

	/**
	 * Switch on events to stdout.
	 */
	public static void eventsToStdout() {
		EventWithThreadName eventWithThreadName = new TestUtils.EventWithThreadName();
		eventWithThreadName.activate();
		SmppObject.setEvent(eventWithThreadName);

	}
	
	public static class DebugWithThreadName extends DefaultDebug {
		@Override
		public void write(String msg) {
			super.write("[" + Thread.currentThread().getName() + "] " + msg);
		}
	}
	public static class EventWithThreadName extends DefaultEvent {
		@Override
		public void write(String msg) {
			super.write("[" + Thread.currentThread().getName() + "] " + msg);
		}
	}

}
