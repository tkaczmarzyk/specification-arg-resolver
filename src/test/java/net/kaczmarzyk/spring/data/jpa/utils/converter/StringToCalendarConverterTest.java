/**
 * Copyright 2014-2023 the original author or authors.
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

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static net.kaczmarzyk.spring.data.jpa.utils.ThrowableAssertions.assertThrows;
import static net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch.EMPTY_RESULT;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Hubert Gotfryd
 */
public class StringToCalendarConverterTest {

	Converter converterWithDefaultFormats = Converter.withTypeMismatchBehaviour(EMPTY_RESULT, null, Locale.getDefault());

	@Test
	public void convertsToCalendarUsingDefaultFormat() {
		//given
		Calendar referenceCalendar = convertLocalDateToCalendar(LocalDate.of(2022, 11, 24));

		//when
		Calendar converted = converterWithDefaultFormats.convert("2022-11-24", Calendar.class);

		//then
		assertThat(converted)
				.isEqualTo(referenceCalendar);
	}

	@Test
	public void throwsValueRejectedExceptionForUnparseableCalendar_differentThanExpectedDateFormat() {
		//when + then
		assertThrows(
				Converter.ValueRejectedException.class,
				() -> converterWithDefaultFormats.convert("24-11-2022", Calendar.class),
				"Date format exception, expected format: yyyy-MM-dd"
		);
	}

	@Test
	public void throwsValueRejectedExceptionForUnparseableCalendar_unnecessaryAdditionalCharacters() {
		//when + then
		assertThrows(
				Converter.ValueRejectedException.class,
				() -> converterWithDefaultFormats.convert("2022-11-24-invalid-format", Calendar.class),
				"Date format exception, expected format: yyyy-MM-dd"
		);
	}

	@Test
	public void convertsToCalendarUsingCustomFormat() {
		//given
		Converter converterWithCustomFormat = Converter.withDateFormat("MM-yyyy-dd", EMPTY_RESULT, null);
		Calendar referenceCalendar = convertLocalDateToCalendar(LocalDate.of(2022, 11, 24));

		//when
		Calendar converted = converterWithCustomFormat.convert("11-2022-24", Calendar.class);

		//then
		assertThat(converted)
				.isEqualTo(referenceCalendar);
	}

	@Test
	public void throwsValueRejectedExceptionForUnparseableCalendarAndCustomFormat_differentThanExpectedDateFormat() {
		//given
		Converter converterWithCustomFormat = Converter.withDateFormat("MM-yyyy-dd", EMPTY_RESULT, null);

		//when + then
		assertThrows(
				Converter.ValueRejectedException.class,
				() -> converterWithCustomFormat.convert("2022-11-24", Calendar.class),
				"Date format exception, expected format: MM-yyyy-dd"
		);
	}

	@Test
	public void throwsValueRejectedExceptionForUnparseableCalendarAndCustomFormat_unnecessaryAdditionalCharacters() {
		//given
		Converter converterWithCustomFormat = Converter.withDateFormat("MM-yyyy-dd", EMPTY_RESULT, null);

		//when + then
		assertThrows(
				Converter.ValueRejectedException.class,
				() -> converterWithCustomFormat.convert("11-2022-24-invalid-format", Calendar.class),
				"Date format exception, expected format: MM-yyyy-dd"
		);
	}

	@Test
	public void appendsDefaultTimeDuringConversionIfConverterHasOnlyDateFormatSpecified() {
		//given
		Converter converterWithCustomFormat = Converter.withDateFormat("yyyy-MM-dd", EMPTY_RESULT, null);
		Calendar referenceCalendar = convertLocalDateToCalendar(LocalDate.of(2022, 11, 24));

		//when
		Calendar converted = converterWithCustomFormat.convert("2022-11-24", Calendar.class);

		//then
		assertThat(converted)
				.isEqualTo(referenceCalendar);
		assertThat(converted.getTime())
				.hasHourOfDay(0)
				.hasMinute(0)
				.hasMillisecond(0);
	}

	private Calendar convertLocalDateToCalendar(LocalDate localDate) {
		Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar;
	}
}
