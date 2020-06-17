package net.kaczmarzyk.spring.data.jpa.utils.converter;

import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import net.kaczmarzyk.spring.data.jpa.utils.Converter.ValueRejectedException;
import org.junit.Test;

import java.time.Instant;

import static net.kaczmarzyk.spring.data.jpa.IntegrationTestBase.DEFAULT_CONVERSION_SERVICE;
import static net.kaczmarzyk.spring.data.jpa.utils.ThrowableAssertions.assertThrows;
import static net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch.EMPTY_RESULT;
import static org.assertj.core.api.Assertions.assertThat;

public class StringToInstantConverterTest {
	
	Converter converterWithDefaultFormats = Converter.withTypeMismatchBehaviour(EMPTY_RESULT, DEFAULT_CONVERSION_SERVICE);
	
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
				"invalid instant value: 2020-15:08:53.282+02:0006-16T, expected format: yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
		);
	}
	
	@Test
	public void convertsToInstantUsingCustomFormat() {
		Converter converterWithCustomFormat = Converter.withDateFormat("yyyy-MM-dd\'T\' HH:mm:ss.SSS XXX", EMPTY_RESULT, DEFAULT_CONVERSION_SERVICE);
		Instant instant = converterWithCustomFormat.convert("2020-06-16T 15:08:53.282 +05:00", Instant.class);
		
		assertThat(instant)
				.isEqualTo("2020-06-16T10:08:53.282Z");
	}
	
	@Test
	public void throwsValueRejectedExceptionForUnparseableInstantAndCustomFormat() {
		Converter converterWithCustomFormat = Converter.withDateFormat("yyyy-invalid-format-HH:mm:ss", EMPTY_RESULT, DEFAULT_CONVERSION_SERVICE);
		
		assertThrows(
				ValueRejectedException.class,
				() -> converterWithCustomFormat.convert("2020-15:08:53.282+02:0006-16T", Instant.class),
				"invalid instant value: 2020-15:08:53.282+02:0006-16T, expected format: yyyy-invalid-format-HH:mm:ss"
		);
		
	}
	
}
