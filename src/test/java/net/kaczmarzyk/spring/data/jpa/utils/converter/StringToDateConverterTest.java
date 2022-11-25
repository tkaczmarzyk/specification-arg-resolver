package net.kaczmarzyk.spring.data.jpa.utils.converter;

import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static net.kaczmarzyk.spring.data.jpa.utils.ThrowableAssertions.assertThrows;
import static net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch.EMPTY_RESULT;
import static net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch.EXCEPTION;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Hubert Gotfryd
 */
public class StringToDateConverterTest {

	Converter converterWithDefaultFormats = Converter.withTypeMismatchBehaviour(EMPTY_RESULT, null);

	@Test
	public void convertsToDateUsingDefaultFormat() {
		//when
		Date converted = converterWithDefaultFormats.convert("2022-11-24", Date.class);

		//then
		assertThat(converted)
				.isWithinMonth(11)
				.isWithinMonth(11)
				.isWithinDayOfMonth(24)
				.isWithinYear(2022);
	}

	@Test
	public void convertsToMultipleDatesUsingDefaultFormat() {
		//when
		List<Date> converted = converterWithDefaultFormats.convert(Arrays.asList("2022-11-24", "2022-04-02"), Date.class);

		//then
		assertThat(converted)
				.hasSize(2);

		assertThat(converted.get(0))
				.isWithinMonth(11)
				.isWithinDayOfMonth(24)
				.isWithinYear(2022);

		assertThat(converted.get(1))
				.isWithinMonth(4)
				.isWithinDayOfMonth(2)
				.isWithinYear(2022);
	}

	@Test
	public void convertsToDateUsingCustomFormat() {
		//given
		Converter converterWithCustomFormat = Converter.withDateFormat("MM-yyyy-dd", EMPTY_RESULT, null);

		//when
		Date converted = converterWithCustomFormat.convert("11-2022-24", Date.class);

		//then
		assertThat(converted)
				.isWithinMonth(11)
				.isWithinDayOfMonth(24)
				.isWithinYear(2022);
	}

	@Test
	public void convertsToMultipleDatesUsingCustomFormat() {
		//given
		Converter converterWithCustomFormat = Converter.withDateFormat("MM-yyyy-dd", EMPTY_RESULT, null);

		//when
		List<Date> converted = converterWithCustomFormat.convert(Arrays.asList("11-2022-24", "04-2022-15"), Date.class);

		//then
		assertThat(converted)
				.hasSize(2);

		assertThat(converted.get(0))
				.isWithinMonth(11)
				.isWithinDayOfMonth(24)
				.isWithinYear(2022);

		assertThat(converted.get(1))
				.isWithinMonth(4)
				.isWithinDayOfMonth(15)
				.isWithinYear(2022);
	}

	@Test
	public void convertsToMultipleDatesOmittingUnparsableDatesUsingConverterWithEmptyResultOnTypeMismatch() {
		//given
		Converter converterWithCustomFormat = Converter.withDateFormat("yyyy-MM-dd", EMPTY_RESULT, null);

		//when
		List<Date> converted = converterWithCustomFormat.convert(Arrays.asList("05-13-2022", "2022-11-24"), Date.class);

		//then
		assertThat(converted)
				.hasSize(1);

		assertThat(converted.get(0))
				.isWithinMonth(11)
				.isWithinDayOfMonth(24)
				.isWithinYear(2022);
	}

	@Test
	public void throwsValueRejectedExceptionForUnparseableDate() {
		//when + then
		assertThrows(
				Converter.ValueRejectedException.class,
				() -> converterWithDefaultFormats.convert("11-2022-24", Date.class),
				"Date format exception, expected format: yyyy-MM-dd"
		);
	}

	@Test
	public void throwsValueRejectedExceptionForUnparseableDateAndCustomFormat() {
		//given
		Converter converterWithCustomFormat = Converter.withDateFormat("MM-yyyy-dd", EMPTY_RESULT, null);

		//when + then
		assertThrows(
				Converter.ValueRejectedException.class,
				() -> converterWithCustomFormat.convert("2022-11-24", Date.class),
				"Date format exception, expected format: MM-yyyy-dd"
		);
	}

	@Test
	public void throwsValuesRejectedExceptionForConverterWithExceptionOnTypeMismatchIfOneOfMultipleDatesIsInUnparsableFormat() {
		//given
		Converter converterWithCustomFormat = Converter.withDateFormat("yyyy-MM-dd", EXCEPTION, null);

		//when + then
		assertThrows(
				Converter.ValuesRejectedException.class,
				() -> converterWithCustomFormat.convert(Arrays.asList("2022-11-24", "05-13-2022"), Date.class),
				"invalid values present in the HTTP param"
		);
	}
}
