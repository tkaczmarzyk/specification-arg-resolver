package net.kaczmarzyk.spring.data.jpa.utils;

import net.kaczmarzyk.spring.data.jpa.Gender;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.convert.ConversionService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import static net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch.EMPTY_RESULT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ConverterFallbackMechanismTest {
	
	ConversionService conversionService = mock(ConversionService.class);
	Converter converter = Converter.withTypeMismatchBehaviour(EMPTY_RESULT, conversionService);
	
	@Before
	public void resetMocks() {
		Mockito.reset(conversionService);
	}
	
	@Test
	public void shouldNotUseFallbackMechanismForEnumType() {
		converter.convert("MALE", Gender.class);
		
		verifyZeroInteractions(conversionService);
	}
	
	@Test
	public void shouldNotUseFallbackMechanismForDateType() {
		converter.convert("2015-03-01", Date.class);
		
		verifyZeroInteractions(conversionService);
	}
	
	@Test
	public void shouldNotUseFallbackMechanismForBooleanType() {
		converter.convert("true", Boolean.class);
		converter.convert("false", Boolean.class);
		
		verifyZeroInteractions(conversionService);
	}
	
	@Test
	public void shouldNotUseFallbackMechanismForBooleanPrimitiveType() {
		converter.convert("true", boolean.class);
		converter.convert("false", boolean.class);
		
		verifyZeroInteractions(conversionService);
	}
	
	@Test
	public void shouldNotUseFallbackMechanismForFloatType() {
		converter.convert("10", Float.class);
		
		verifyZeroInteractions(conversionService);
	}
	
	@Test
	public void shouldNotUseFallbackMechanismForFloatPrimitiveType() {
		converter.convert("10.8", float.class);
		
		verifyZeroInteractions(conversionService);
	}
	
	@Test
	public void shouldNotUseFallbackMechanismForDoubleType() {
		converter.convert("13.52", Double.class);
		
		verifyZeroInteractions(conversionService);
	}
	
	@Test
	public void shouldNotUseFallbackMechanismForDoublePrimitiveType() {
		converter.convert("13.53", double.class);
		
		verifyZeroInteractions(conversionService);
	}
	
	@Test
	public void shouldNotUseFallbackMechanismForBigDecimalType() {
		converter.convert("13.54", BigDecimal.class);
		
		verifyZeroInteractions(conversionService);
	}
	
	@Test
	public void shouldNotUseFallbackMechanismForUUIDType() {
		converter.convert("2cdf7f82-2e32-4219-be0c-a5457e79c7b1", UUID.class);
		
		verifyZeroInteractions(conversionService);
	}
	
	@Test
	public void shouldNotUseFallbackMechanismForOffsetDateTimeType() {
		converter.convert("2020-06-16T15:08:53.282+02:00", OffsetDateTime.class);
		
		verifyZeroInteractions(conversionService);
	}
	
	@Test
	public void shouldNotUseFallbackMechanismForInstantType() {
		converter.convert("2020-06-16T15:08:53.282Z", Instant.class);
		
		verifyZeroInteractions(conversionService);
	}
	
	@Test
	public void shouldNotUseFallbackMechanismForUnsupportedTypeWhenConversionServiceDoesNotSupportRequiredConversion() {
		when(conversionService.canConvert(String.class, CustomType.class)).thenReturn(false);
		
		converter.convert("rawStringValue", CustomType.class);
		
		verify(conversionService).canConvert(String.class, CustomType.class);
		verify(conversionService, never()).convert("rawStringValue", CustomType.class);
	}
	
	@Test
	public void shouldUseFallbackMechanismForUnsupportedTypeWhenConversionServiceSupportsRequiredConversion() {
		when(conversionService.canConvert(String.class, CustomType.class)).thenReturn(true);
		when(conversionService.convert("rawValue", CustomType.class)).thenReturn(new CustomType("convertedValue"));
		
		CustomType convertedValue = converter.convert("rawValue", CustomType.class);
		assertThat(convertedValue).isEqualTo(new CustomType("convertedValue"));
		
		verify(conversionService).canConvert(String.class, CustomType.class);
		verify(conversionService, times(1)).convert("rawValue", CustomType.class);
	}
	
	private class CustomType {
		public final String value;
		
		public CustomType(String value) {
			this.value = value;
		}
		
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			CustomType that = (CustomType) o;
			return Objects.equals(value, that.value);
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(value);
		}
	}
	
}