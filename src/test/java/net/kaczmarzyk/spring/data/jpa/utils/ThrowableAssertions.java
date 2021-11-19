/**
 * Copyright 2014-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.kaczmarzyk.spring.data.jpa.utils;

import org.assertj.core.api.Assertions;
import org.opentest4j.AssertionFailedError;

public class ThrowableAssertions {
	
	public static <T extends Throwable> Throwable assertThrows(Class<T> expectedType, ThrowableRunnable throwableRun) {
		try {
			throwableRun.run();
			throw new ThrowableNotFoundException();
		} catch (Throwable throwable) {
			if (throwable.getClass().equals(ThrowableNotFoundException.class)) {
				throw new AssertionFailedError("Expected " + expectedType + " to be thrown, but nothing was thrown.");
			}
			Assertions.assertThat(throwable).isInstanceOf(expectedType);
			return throwable;
		}
	}
	
	public static <T extends Throwable> void assertThrows(Class<T> expectedType, ThrowableRunnable throwableRun, String expectedMessage) {
		Throwable throwable = assertThrows(expectedType, throwableRun);
		
		Assertions.assertThat(throwable).hasMessage(expectedMessage);
	}
	
	public interface ThrowableRunnable {
		void run() throws Throwable;
	}
	
	private static class ThrowableNotFoundException extends RuntimeException {
	}
}
