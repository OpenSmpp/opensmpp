package org.smpp.pdu;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class Matchers {
	public static Matcher<WrongLengthOfStringException> stringLengthException(int min, int max, int actual) {
		return new WrongLengthOfStringExceptionMatcher(min, max, actual);
	}

	public static Matcher<IntegerOutOfRangeException> integerOutOfRange(int min, int max, int actual) {
		return new IntegerOutOfRangeExceptionMatcher(min, max, actual);
	}

	private static class WrongLengthOfStringExceptionMatcher extends BaseMatcher<WrongLengthOfStringException> {
		private static final String FORMAT = "org.smpp.pdu.WrongLengthOfStringException: The string is shorter or longer than required:  min=%d max=%d actual=%d.";

		private final int min;
		private final int max;
		private final int actual;

		private WrongLengthOfStringExceptionMatcher(int min, int max, int actual) {
			this.min = min;
			this.max = max;
			this.actual = actual;
		}

		public boolean matches(Object item) {
			if (! (item instanceof WrongLengthOfStringException)) {
				return false;
			}

			return String.format(FORMAT, min, max, actual).equals(((WrongLengthOfStringException) item).toString());
		}

		public void describeTo(Description description) {
			description.appendText("Wrong length of string");
		}
	}

	public static class IntegerOutOfRangeExceptionMatcher extends BaseMatcher<IntegerOutOfRangeException> {
		private static final String FORMAT = "org.smpp.pdu.IntegerOutOfRangeException: The integer is lower or greater than required:  min=%d max=%d actual=%d.";

		private final int min;
		private final int max;
		private final int actual;

		private IntegerOutOfRangeExceptionMatcher(int min, int max, int actual) {
			this.min = min;
			this.max = max;
			this.actual = actual;
		}

		public boolean matches(Object item) {
			if (! (item instanceof IntegerOutOfRangeException)) {
				return false;
			}

			return String.format(FORMAT, min, max, actual).equals(((IntegerOutOfRangeException) item).toString());
		}

		public void describeTo(Description description) {
			description.appendText("Integer out of range");
		}
	}
}
