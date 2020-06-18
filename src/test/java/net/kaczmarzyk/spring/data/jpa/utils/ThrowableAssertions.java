package net.kaczmarzyk.spring.data.jpa.utils;

import junit.framework.AssertionFailedError;
import org.assertj.core.api.Assertions;

public class ThrowableAssertions {
	
	public static <T extends Throwable> void assertThrows(Class<T> expectedType, ThrowableRunnable throwableRun, String expectedMessage) {
		try {
			throwableRun.run();
			throw new ThrowableNotFoundException();
		} catch (Throwable throwable) {
			if (throwable.getClass().equals(ThrowableNotFoundException.class)) {
				throw new AssertionFailedError("Expected " + expectedType + " to be thrown, but nothing was thrown.");
			}
			Assertions.assertThat(throwable).isInstanceOf(expectedType);
			Assertions.assertThat(throwable).hasMessage(expectedMessage);
		}
	}
	
	public interface ThrowableRunnable {
		void run() throws Throwable;
	}
	
	private static class ThrowableNotFoundException extends RuntimeException {
	}
}
