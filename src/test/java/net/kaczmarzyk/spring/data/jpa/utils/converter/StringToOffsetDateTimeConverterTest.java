package net.kaczmarzyk.spring.data.jpa.utils.converter;

import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import net.kaczmarzyk.spring.data.jpa.utils.Converter.ValueRejectedException;
import org.junit.Test;

import java.time.OffsetDateTime;

import static net.kaczmarzyk.spring.data.jpa.utils.ThrowableAssertions.assertThrows;
import static net.kaczmarzyk.spring.data.jpa.IntegrationTestBase.DEFAULT_CONVERSION_SERVICE;
import static net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch.EMPTY_RESULT;
import static org.assertj.core.api.Assertions.assertThat;

public class StringToOffsetDateTimeConverterTest {
	
	Converter converterWithDefaultFormats = Converter.withTypeMismatchBehaviour(EMPTY_RESULT, DEFAULT_CONVERSION_SERVICE);
	
	@Test
	public void convertsToOffsetDateTimeUsingDefaultFormat() {
		OffsetDateTime offsetDateTime = converterWithDefaultFormats.convert("2020-06-16T15:08:53.282+02:00", OffsetDateTime.class);
		
		assertThat(offsetDateTime)
				.isEqualTo("2020-06-16T15:08:53.282+02:00");
	}
	
	@Test
	public void throwsValueRejectedExceptionForUnparseableOffsetDateTime() {
		assertThrows(
				ValueRejectedException.class,
				() -> converterWithDefaultFormats.convert("2020-15:08:53.282+02:0006-16T", OffsetDateTime.class),
				"invalid offset date time: 2020-15:08:53.282+02:0006-16T, expected format: yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
		);
	}
	
	@Test
	public void convertsToOffsetDateTimeUsingCustomFormat() {
		Converter converterWithCustomFormat = Converter.withDateFormat("yyyy-HH:mm:ss.SSSXXXMM-dd\'T\'", EMPTY_RESULT, DEFAULT_CONVERSION_SERVICE);
		OffsetDateTime offsetDateTime = converterWithCustomFormat.convert("2020-15:08:53.282+02:0006-16T", OffsetDateTime.class);
		
		assertThat(offsetDateTime)
				.isEqualTo("2020-06-16T15:08:53.282+02:00");
	}
	
	@Test
	public void throwsValueRejectedExceptionForUnparseableOffsetDateTimeAndCustomFormat() {
		Converter converterWithCustomFormat = Converter.withDateFormat("yyyy-invalid-format-HH:mm:ss", EMPTY_RESULT, DEFAULT_CONVERSION_SERVICE);
		
		assertThrows(
				ValueRejectedException.class,
				() -> converterWithCustomFormat.convert("2020-15:08:53.282+02:0006-16T", OffsetDateTime.class),
				"invalid offset date time: 2020-15:08:53.282+02:0006-16T, expected format: yyyy-invalid-format-HH:mm:ss"
		);
		
	}
}
