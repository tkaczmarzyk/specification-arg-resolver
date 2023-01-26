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

import net.kaczmarzyk.spring.data.jpa.Gender;
import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import net.kaczmarzyk.spring.data.jpa.utils.Converter.ValueRejectedException;
import net.kaczmarzyk.spring.data.jpa.utils.Converter.ValuesRejectedException;
import net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static net.kaczmarzyk.spring.data.jpa.utils.ThrowableAssertions.assertThrows;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


public class ConverterTest {

	private static enum TurkishCharEnum {
		I,
		İ
	}
	
	private Converter converter = Converter.withDateFormat("yyyy-MM-dd", OnTypeMismatch.EMPTY_RESULT, null);
	private Converter converterWithoutFormat = Converter.withTypeMismatchBehaviour(OnTypeMismatch.EMPTY_RESULT, null, Locale.ENGLISH);
	
	@Test
	public void stringIsPassedThrough() {
		assertThat(converter.convert("143", String.class)).isEqualTo("143");
	}
	
	@Test
	public void stringArePassedThrough() {
		List<String> values = Arrays.asList("1", "2", "3");
		assertThat(converter.convert(values, String.class)).isSameAs(values);
	}
	
	@Test
	public void convertsToEnum() {
		assertThat(converter.convert("FEMALE", Gender.class)).isEqualTo(Gender.FEMALE);
	}

	@Test
	public void convertsToEnumIgnoringCase() {
		assertThat(converter.convert("fEmAlE", Gender.class, true)).isEqualTo(Gender.FEMALE);
	}
	
	@Test
	public void convertsToEnumIgnoringCaseAndUsingCustomLocale_converterWithoutDateFormat() {
		// english locale
		assertThat(converterWithoutFormat.convert("i", TurkishCharEnum.class, true)).isEqualTo(TurkishCharEnum.I);
		
		// Turkish locale
		converterWithoutFormat = Converter.withTypeMismatchBehaviour(OnTypeMismatch.EMPTY_RESULT, null, new Locale("tr", "TR"));
		assertThat(converterWithoutFormat.convert("i", TurkishCharEnum.class, true)).isEqualTo(TurkishCharEnum.İ);
	}

	@Test
	public void convertsToBoolean() {
		assertThat(converter.convert("true", Boolean.class)).isEqualTo(true);
		assertThat(converter.convert("false", Boolean.class)).isEqualTo(false);
	}
	
	@Test
	public void convertsToBooleanByPrimitiveType() {
		assertThat(converter.convert("true", boolean.class)).isEqualTo(true);
		assertThat(converter.convert("false", boolean.class)).isEqualTo(false);
	}
	
	@Test
	public void throwsExceptionOnInvalidBooleanValue() {
		assertThatThrownBy(() -> converter.convert("TRUE", Boolean.class))
				.isInstanceOf(IllegalArgumentException.class);
	}
	
	@Test
	public void convertsToMultipleEnums() {
		assertThat(converter.convert(Arrays.asList("FEMALE", "MALE"), Gender.class))
			.isEqualTo(Arrays.asList(Gender.FEMALE, Gender.MALE));
	}
	
	@Test
	public void throwsExceptionWithRejectedEnumNames() {
		converter.convert(Arrays.asList("MALE", "ROBOT", "FEMALE", "ALIEN"), Gender.class);

		assertThatThrownBy(() -> converter.convert(Arrays.asList("MALE", "ROBOT", "FEMALE", "ALIEN"), Gender.class))
				.isInstanceOf(ValuesRejectedException.class)
				.satisfies(exception -> {
					assertThat(((ValuesRejectedException) exception).getRejectedValues())
							.containsExactly("ROBOT", "ALIEN");
				});
	}
	
	@Test
	public void ignoresRejectedEnumValues() {
		converter = Converter.withTypeMismatchBehaviour(OnTypeMismatch.EMPTY_RESULT, null, Locale.getDefault());
		
		List<Gender> result = converter.convert(Arrays.asList("MALE", "ROBOT", "FEMALE", "ALIEN"), Gender.class);
		
		assertThat(result).containsOnly(Gender.MALE, Gender.FEMALE);
	}
	
	@Test
	public void convertsValueWithoutDecimalPointToFloat() {
		assertThat(converter.convert("10", Float.class)).isEqualTo(Float.valueOf(10.0f));
	}
	
	@Test
	public void convertsToFloat() {
		assertThat(converter.convert("10.8", Float.class)).isEqualTo(Float.valueOf(10.8f));
	}
	
	@Test
	public void convertsToFloatPrimitive() {
		assertThat(converter.convert("10.8", float.class)).isEqualTo(10.8f);
	}
	
	@Test
	public void convertsValueWithoutDecimalPointToFloatPrimitive() {
		assertThat(converter.convert("10", float.class)).isEqualTo(10.0f);
	}
	
	@Test
	public void convertsToDouble() {
		assertThat(converter.convert("10.8", Double.class)).isEqualTo(Double.valueOf(10.8d));
	}
	
	@Test
	public void convertsToDoublePrimitive() {
		assertThat(converter.convert("10.8", double.class)).isEqualTo(10.8d);
	}
	
	@Test
	public void convertsValueWithoutDecimalPointToDouble() {
		assertThat(converter.convert("10", Double.class)).isEqualTo(Double.valueOf(10d));
	}
	
	@Test
	public void convertsValueWithoutDecimalPointToDoublePrimitive() {
		assertThat(converter.convert("10", double.class)).isEqualTo(10d);
	}

	@Test
	public void convertsValueToCharPrimitive() {
		assertThat(converter.convert("a", char.class)).isEqualTo('a');
	}

	@Test
	public void convertsValueToChar() {
		assertThat(converter.convert("A", Character.class)).isEqualTo('A');
	}

	@Test
	public void rejectsValueThatIsLongerThan1WhenConvertingToChar() {
		assertThrows(
				ValueRejectedException.class,
				() -> converter.convert("ABC", char.class));
	}
	
	@Test
	public void convertsToBigDecimal() {
		assertThat(converter.convert("10.99", BigDecimal.class)).isEqualTo(new BigDecimal("10.99"));
	}
	
	@Test
	public void convertsValueWithoutDecimalPointToBigDecimal() {
		assertThat(converter.convert("10", BigDecimal.class)).isEqualTo(new BigDecimal("10"));
	}
	
	@Test
	public void convertsToUUID() {
		UUID converted = converter.convert("2cdf7f82-2e32-4219-be0c-a5457e79c7b1", UUID.class);
		
		assertThat(converted)
				.isEqualTo(UUID.fromString("2cdf7f82-2e32-4219-be0c-a5457e79c7b1"));
	}
	
	@Test
	public void throwsValueRejectedExceptionForUnparsableUUID() {
		assertThrows(
				ValueRejectedException.class,
				() -> converter.convert("2cdf7f82-2e32-4219-be0c-a5457e79c7b@", UUID.class),
				"unparseable uuid"
				
		);
	}
	
	private Matcher<?> valuesRejected(final String... values) {
		return new BaseMatcher<ValuesRejectedException>() {

			@Override
			public boolean matches(Object item) {
				return Arrays.asList(values).equals(((ValuesRejectedException) item).getRejectedValues());
			}

			@Override
			public void describeTo(Description desc) {
				desc.appendText("ValuesRejectedException with items: " + Arrays.toString(values));
			}
		};
	}
}
