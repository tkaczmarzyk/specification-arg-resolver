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
import net.kaczmarzyk.spring.data.jpa.utils.Converter.ValueRejectedException;
import org.junit.Test;

import java.time.OffsetDateTime;

import static net.kaczmarzyk.spring.data.jpa.utils.ThrowableAssertions.assertThrows;
import static net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch.EMPTY_RESULT;
import static org.assertj.core.api.Assertions.assertThat;

public class StringToOffsetDateTimeConverterTest {
	
	Converter converterWithDefaultFormats = Converter.withTypeMismatchBehaviour(EMPTY_RESULT, null);
	
	@Test
	public void convertsToOffsetDateTimeUsingDefaultFormat() {
		OffsetDateTime offsetDateTime = converterWithDefaultFormats.convert("2020-06-16T15:08:53.282+02:00", OffsetDateTime.class);
		
		assertThat(offsetDateTime)
				.isEqualTo("2020-06-16T15:08:53.282+02:00");
	}
	
	@Test
	public void throwsValueRejectedExceptionForUnparseableOffsetDateTime_differentThanExpectedDateFormat() {
		assertThrows(
				ValueRejectedException.class,
				() -> converterWithDefaultFormats.convert("06-2020-16T15:08:53.282+02:00", OffsetDateTime.class),
				"OffsetDateTime format exception, expected format: yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
		);
	}

	@Test
	public void throwsValueRejectedExceptionForUnparseableOffsetDateTime_unnecessaryAdditionalCharacters() {
		assertThrows(
				ValueRejectedException.class,
				() -> converterWithDefaultFormats.convert("2020-06-16T15:08:53.282+02:00-invalid-format", OffsetDateTime.class),
				"OffsetDateTime format exception, expected format: yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
		);
	}

	@Test
	public void convertsToOffsetDateTimeUsingCustomFormat() {
		Converter converterWithCustomFormat = Converter.withDateFormat("yyyy-HH:mm:ss.SSSXXXMM-dd\'T\'", EMPTY_RESULT, null);
		OffsetDateTime offsetDateTime = converterWithCustomFormat.convert("2020-15:08:53.282+02:0006-16T", OffsetDateTime.class);
		
		assertThat(offsetDateTime)
				.isEqualTo("2020-06-16T15:08:53.282+02:00");
	}
	
	@Test
	public void throwsValueRejectedExceptionForUnparseableOffsetDateTimeAndCustomFormat_differentThanExpectedDateFormat() {
		Converter converterWithCustomFormat = Converter.withDateFormat("yyyy-HH:mm:ss.SSSXXXMM-dd\'T\'", EMPTY_RESULT, null);
		
		assertThrows(
				ValueRejectedException.class,
				() -> converterWithCustomFormat.convert("15:08:53.282+02:0006-16T-2020", OffsetDateTime.class),
				"OffsetDateTime format exception, expected format: yyyy-HH:mm:ss.SSSXXXMM-dd\'T\'"
		);
		
	}

	@Test
	public void throwsValueRejectedExceptionForUnparseableOffsetDateTimeAndCustomFormat_unnecessaryAdditionalCharacters() {
		Converter converterWithCustomFormat = Converter.withDateFormat("yyyy-HH:mm:ss.SSSXXXMM-dd\'T\'", EMPTY_RESULT, null);

		assertThrows(
				ValueRejectedException.class,
				() -> converterWithCustomFormat.convert("2020-15:08:53.282+02:0006-16T-invalid-format", OffsetDateTime.class),
				"OffsetDateTime format exception, expected format: yyyy-HH:mm:ss.SSSXXXMM-dd\'T\'"
		);

	}

	@Test
	public void appendsDefaultTimeAndOffsetDuringConversionIfConverterHasOnlyDateFormatSpecified() {
		Converter converterWithCustomFormat = Converter.withDateFormat("yyyy-MM-dd", EMPTY_RESULT, null);
		OffsetDateTime offsetDateTime = converterWithCustomFormat.convert("2022-12-13", OffsetDateTime.class);

		assertThat(offsetDateTime)
				.isEqualTo("2022-12-13T00:00:00.000+00:00");
	}

}
