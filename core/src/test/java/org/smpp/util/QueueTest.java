package org.smpp.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class QueueTest {

	private Queue queue;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setup() {
		queue = new Queue();
	}

	@Test
	public void testDefaultConstructorDoesNotLimitSize() {
		Object o = new Object();
		// FIXME: there seems to be a performance issue going over this...
		int limit = 1000000;
		for (int i = 0; i < limit; i++) {
			queue.enqueue(o);
		}
		assertEquals(limit, queue.size());
	}

	@Test
	public void testConstructorLimitsSize() throws IndexOutOfBoundsException {
		thrown.expect(IndexOutOfBoundsException.class);

		queue = new Queue(10);
		for (int i = 0; i < 11; i++) {
			queue.enqueue(new Object());
		}
	}

	@Test
	public void testIsEmpty() {
		assertTrue(queue.isEmpty());
		queue.enqueue(new Object());
		assertFalse(queue.isEmpty());
		queue.dequeue();
		assertTrue(queue.isEmpty());
	}

	@Test
	public void testSize() {
		int size = 0;
		assertEquals(size, queue.size());
		for (int i = 0; i < 10; i++) {
			queue.enqueue(size++);
			assertEquals(size, queue.size());
		}
	}

	@Test
	public void testFifo() {
		Object a = new Object();
		Object b = new Object();
		Object c = new Object();

		queue.enqueue(a);
		queue.enqueue(b);
		queue.enqueue(c);

		assertSame(a, queue.dequeue());
		assertSame(b, queue.dequeue());
		assertSame(c, queue.dequeue());
		assertTrue(queue.isEmpty());
	}

	@Test
	public void testFind() {
		Object a = new Object();
		Object b = new Object();
		Object c = new Object();

		queue.enqueue(a);
		queue.enqueue(b);
		queue.enqueue(c);

		assertEquals(3, queue.size());
		assertSame(a, queue.find(a));
		assertSame(b, queue.find(b));
		assertSame(c, queue.find(c));
		assertEquals(3, queue.size());
	}

	@Test
	public void testDequeueReturnsNullWhenEmpty() {
		assertNull(queue.dequeue());
	}

	@Test
	public void testDequeueReturnsNullWhenNotEnqueued() {
		queue.enqueue(new Object());
		assertNull(queue.dequeue(new Object()));
	}
}
