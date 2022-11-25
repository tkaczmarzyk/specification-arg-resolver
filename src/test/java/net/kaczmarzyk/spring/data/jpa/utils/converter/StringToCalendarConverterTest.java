package net.kaczmarzyk.spring.data.jpa.utils.converter;

import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import org.junit.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

import static net.kaczmarzyk.spring.data.jpa.utils.ThrowableAssertions.assertThrows;
import static net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch.EMPTY_RESULT;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Hubert Gotfryd
 */
public class StringToCalendarConverterTest {

	Converter converterWithDefaultFormats = Converter.withTypeMismatchBehaviour(EMPTY_RESULT, null);

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
	public void throwsValueRejectedExceptionForUnparseableCalendar_invalidDateFormat() {
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
	public void throwsValueRejectedExceptionForUnparseableCalendarAndCustomFormat_invalidDateFormat() {
		//given
		Converter converterWithCustomFormat = Converter.withDateFormat("MM-yyyy-dd", EMPTY_RESULT, null);

		//when + then
		assertThrows(
				Converter.ValueRejectedException.class,
				() -> converterWithCustomFormat.convert("11-2022-24-invalid-format", Calendar.class),
				"Date format exception, expected format: MM-yyyy-dd"
		);
	}

	private Calendar convertLocalDateToCalendar(LocalDate localDate) {
		Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar;
	}
}
