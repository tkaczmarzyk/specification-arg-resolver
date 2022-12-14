/**
 * Copyright 2014-2022 the original author or authors.
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
import org.junit.Test;

import java.time.LocalDateTime;

import static net.kaczmarzyk.spring.data.jpa.utils.ThrowableAssertions.assertThrows;
import static net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch.EMPTY_RESULT;
import static org.assertj.core.api.Assertions.assertThat;

public class StringToLocalDateTimeConverterTest {
	
	Converter converterWithDefaultFormats = Converter.withTypeMismatchBehaviour(EMPTY_RESULT, null);
	
	@Test
	public void convertsToLocalDateTimeUsingDefaultFormat() {
		LocalDateTime localDateTime = converterWithDefaultFormats.convert("2020-06-19T16:50:49", LocalDateTime.class);
		
		assertThat(localDateTime)
				.isEqualTo("2020-06-19T16:50:49");
	}
	
	@Test
	public void throwsValueRejectedExceptionForUnparseableLocalDateTime_differentThanExpectedDateFormat() {
		assertThrows(
				Converter.ValueRejectedException.class,
				() -> converterWithDefaultFormats.convert("06-2020-19T16:50:49", LocalDateTime.class),
				"LocalDateTime format exception, expected format:yyyy-MM-dd'T'HH:mm:ss"
		);
	}

	@Test
	public void throwsValueRejectedExceptionForUnparseableLocalDateTime_unnecessaryAdditionalCharacters() {
		assertThrows(
				Converter.ValueRejectedException.class,
				() -> converterWithDefaultFormats.convert("2020-06-19T16:50:49-invalid-format", LocalDateTime.class),
				"LocalDateTime format exception, expected format:yyyy-MM-dd'T'HH:mm:ss"
		);
	}
	
	@Test
	public void convertsToLocalDateTimeUsingCustomFormat() {
		Converter converterWithCustomFormat = Converter.withDateFormat("yyyy-MM-dd 'T' HH:mm:ss", EMPTY_RESULT, null);
		LocalDateTime localDateTime = converterWithCustomFormat.convert("2020-06-19 T 16:56:49", LocalDateTime.class);
		
		assertThat(localDateTime)
				.isEqualTo("2020-06-19T16:56:49");
	}
	
	@Test
	public void throwsValueRejectedExceptionForUnparseableLocalDateTimeAndCustomFormat_differentThanExpectedDateFormat() {
		Converter converterWithCustomFormat = Converter.withDateFormat("yyyy-MM-dd 'T' HH:mm:ss", EMPTY_RESULT, null);
		
		assertThrows(
				Converter.ValueRejectedException.class,
				() -> converterWithCustomFormat.convert("06-2020-19 T 16:56:49", LocalDateTime.class),
				"LocalDateTime format exception, expected format:yyyy-MM-dd 'T' HH:mm:ss"
		);
		
	}

	@Test
	public void throwsValueRejectedExceptionForUnparseableLocalDateTimeAndCustomFormat_unnecessaryAdditionalCharacters() {
		Converter converterWithCustomFormat = Converter.withDateFormat("yyyy-MM-dd 'T' HH:mm:ss", EMPTY_RESULT, null);

		assertThrows(
				Converter.ValueRejectedException.class,
				() -> converterWithCustomFormat.convert("2020-06-19 T 16:56:49-invalid-format", LocalDateTime.class),
				"LocalDateTime format exception, expected format:yyyy-MM-dd 'T' HH:mm:ss"
		);

	}

	@Test
	public void appendsDefaultTimeDuringConversionIfConverterHasOnlyDateFormatSpecified() {
		Converter converterWithCustomFormat = Converter.withDateFormat("yyyy-MM-dd", EMPTY_RESULT, null);
		LocalDateTime localDateTime = converterWithCustomFormat.convert("2022-12-13", LocalDateTime.class);

		assertThat(localDateTime)
				.isEqualTo("2022-12-13T00:00:00");
	}

}
