package org.smpp.util;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.smpp.util.NotEnoughDataInByteBufferException;

public class NotEnoughDataInByteBufferExceptionMatcher extends TypeSafeMatcher<NotEnoughDataInByteBufferException>{

	private final int expected;
	private final int available;

	public static NotEnoughDataInByteBufferExceptionMatcher notEnoughData(int expected, int available) {
		return new NotEnoughDataInByteBufferExceptionMatcher(expected, available);
	}

	public NotEnoughDataInByteBufferExceptionMatcher(int expected, int available) {
		this.expected = expected;
		this.available = available;
	}

	public void describeTo(Description description) {
		description
			.appendText("expected ")
			.appendValue(expected)
			.appendText("available ")
			.appendValue(available);
	}

	@Override
	protected boolean matchesSafely(NotEnoughDataInByteBufferException ex) {
		return ex.getExpected() == expected && ex.getAvailable() == available;
	}
}
