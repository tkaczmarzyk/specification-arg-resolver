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
package net.kaczmarzyk.spring.data.jpa.utils.converter;

import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import net.kaczmarzyk.spring.data.jpa.utils.Converter.ValueRejectedException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static net.kaczmarzyk.spring.data.jpa.utils.ThrowableAssertions.assertThrows;
import static net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch.EMPTY_RESULT;
import static org.assertj.core.api.Assertions.assertThat;

public class StringToInstantConverterTest {
	
	Converter converterWithDefaultFormats = Converter.withTypeMismatchBehaviour(EMPTY_RESULT, null);
	
	@Test
	public void convertsToInstantUsingDefaultFormat() {
		Instant instant = converterWithDefaultFormats.convert("2020-06-16T15:08:53.282Z", Instant.class);
		
		assertThat(instant)
				.isEqualTo("2020-06-16T15:08:53.282Z");
	}
	
	@Test
	public void throwsValueRejectedExceptionForUnparseableInstant() {
		assertThrows(
				ValueRejectedException.class,
				() -> converterWithDefaultFormats.convert("2020-15:08:53.282+02:0006-16T", Instant.class),
				"Instant format exception, expected format: yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
		);
	}
	
	@Test
	public void convertsToInstantUsingCustomFormat() {
		Converter converterWithCustomFormat = Converter.withDateFormat("yyyy-MM-dd\'T\' HH:mm:ss.SSS XXX", EMPTY_RESULT, null);
		Instant instant = converterWithCustomFormat.convert("2020-06-16T 15:08:53.282 +05:00", Instant.class);
		
		assertThat(instant)
				.isEqualTo("2020-06-16T10:08:53.282Z");
	}
	
	@Test
	public void throwsValueRejectedExceptionForUnparseableInstantAndCustomFormat() {
		Converter converterWithCustomFormat = Converter.withDateFormat("yyyy-invalid-format-HH:mm:ss", EMPTY_RESULT, null);
		
		assertThrows(
				ValueRejectedException.class,
				() -> converterWithCustomFormat.convert("2020-15:08:53.282+02:0006-16T", Instant.class),
				"Instant format exception, expected format: yyyy-invalid-format-HH:mm:ss"
		);
	}
	
}
