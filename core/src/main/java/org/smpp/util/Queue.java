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
package org.smpp.util;

import java.util.LinkedList;
import java.util.ListIterator;

import org.smpp.SmppObject;

/**
 * Implements fifo style queue with removal of specified element from inside of
 * the queue.
 * 
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.1 $
 */
public class Queue extends SmppObject {
	private int maxQueueSize = 0;
	private LinkedList<Object> queueData = new LinkedList<Object>();
	private Object mutex;

	public Queue() {
		mutex = this;
	}

	public Queue(int maxSize) {
		maxQueueSize = maxSize;
		mutex = this;
	}

	/**
	 * Current count of the elements in the queue.
	 */
	public int size() {
		synchronized (mutex) {
			return queueData.size();
		}
	}

	/**
	 * If there is no element in the queue.
	 */
	public boolean isEmpty() {
		synchronized (mutex) {
			return queueData.isEmpty();
		}
	}

	/**
	 * Removes first element form the queue and returns it.
	 * If the queue is empty, returns null.
	 */
	public Object dequeue() {
		synchronized (mutex) {
			Object first = null;
			if (size() > 0) {
				first = queueData.removeFirst();
			}
			return first;
		}
	}

	/**
	 * Tries to find the provided element in the queue and if found,
	 * removes it from the queue and returns it.
	 * If the element is not found returns null.
	 */
	public Object dequeue(Object obj) {
		Object found = null;
		synchronized (mutex) {
			found = find(obj);
			if (found != null) {
				queueData.remove(found);
			}
		}
		return found;
	}

	/**
	 * Appends an element to the end of the queue. If the queue
	 * has set limit on maximum elements and there is already specified
	 * max count of elements in the queue throws IndexOutOfBoundsException.
	 */
	public void enqueue(Object obj) throws IndexOutOfBoundsException {
		synchronized (mutex) {
			if ((maxQueueSize > 0) && (size() >= maxQueueSize)) {
				throw new IndexOutOfBoundsException("Queue is full. Element not added.");
			}
			queueData.add(obj);
		}
	}

	/**
	 * Searches the queue to find the provided element.
	 * Uses <code>equals</code> method to compare elements.
	 */
	public Object find(Object obj) {
		synchronized (mutex) {
			Object current;
			ListIterator<Object> iter = queueData.listIterator(0);
			while (iter.hasNext()) {
				current = iter.next();
				if (current.equals(obj)) {
					return current;
				}
			}
		}
		return null;
	}
}
/*
 * $Log: not supported by cvs2svn $
 * 
 * Old changelog:
 * 13-07-01 ticp@logica.com added dequeue(Object) method which finds and removes
 *						    object which equals() to the provided one
 *						    (not too queue-ish)
 * 01-10-01 ticp@logica.com added some javadoc comments
 */
