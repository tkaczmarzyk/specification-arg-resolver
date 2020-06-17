package net.kaczmarzyk.spring.data.jpa.utils;

import org.assertj.core.api.Assertions;

public class ThrowableAssertions {
	
	public static <T extends Throwable> void assertThrows(Class<T> expectedType, ThrowableRun throwableRun, String expectedMessage) {
		try {
			throwableRun.run();
		} catch (Throwable throwable) {
			Assertions.assertThat(throwable).isInstanceOf(expectedType);
			Assertions.assertThat(throwable).hasMessage(expectedMessage);
		}
	}
	
	public interface ThrowableRun {
		void run() throws Throwable;
	}
}
