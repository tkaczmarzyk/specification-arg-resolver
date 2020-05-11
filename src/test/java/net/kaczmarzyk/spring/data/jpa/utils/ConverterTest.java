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
package net.kaczmarzyk.spring.data.jpa.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import net.kaczmarzyk.spring.data.jpa.Gender;
import net.kaczmarzyk.spring.data.jpa.utils.Converter.ValuesRejectedException;
import net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch;


public class ConverterTest {

	@Rule
	public ExpectedException expected = ExpectedException.none();
	
	Converter converter = Converter.withDateFormat("yyyy-MM-dd", OnTypeMismatch.EMPTY_RESULT);
	
	
	@Test
	public void convertsToDate() {
		Date converted = converter.convert("2015-03-01", Date.class);
		
		assertThat(converted)
			.isWithinMonth(3)
			.isWithinDayOfMonth(1)
			.isWithinYear(2015);
	}
	
	@Test
	public void convertsToMultipleDates() {
		List<Date> converted = converter.convert(Arrays.asList("2015-03-01", "2015-04-02"), Date.class);
		
		assertThat(converted)
			.hasSize(2);
		
		assertThat(converted.get(0))
			.isWithinMonth(3)
			.isWithinDayOfMonth(1)
			.isWithinYear(2015);
		
		assertThat(converted.get(1))
			.isWithinMonth(4)
			.isWithinDayOfMonth(2)
			.isWithinYear(2015);
	}
	
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
	public void convertsToBoolean() {
		assertThat(converter.convert("true", Boolean.class)).isEqualTo(true);
		assertThat(converter.convert("false", Boolean.class)).isEqualTo(false);
	}
	
	@Test
	public void convertsToBooleanByPrimitiveType() {
		assertThat(converter.convert("true", boolean.class)).isEqualTo(true);
		assertThat(converter.convert("false", boolean.class)).isEqualTo(false);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void throwsExceptionOnInvalidBooleanValue() {
		converter.convert("TRUE", Boolean.class);
	}
	
	@Test
	public void convertsToMultipleEnums() {
		assertThat(converter.convert(Arrays.asList("FEMALE", "MALE"), Gender.class))
			.isEqualTo(Arrays.asList(Gender.FEMALE, Gender.MALE));
	}
	
	@Test
	public void throwsExceptionWithRejectedEnumNames() {
		expected.expect(ValuesRejectedException.class);
		expected.expect(valuesRejected("ROBOT", "ALIEN"));
		
		converter = Converter.withTypeMismatchBehaviour(OnTypeMismatch.EXCEPTION);
		
		converter.convert(Arrays.asList("MALE", "ROBOT", "FEMALE", "ALIEN"), Gender.class);
	}
	
	@Test
	public void ignoresRejectedEnumValues() {
		converter = Converter.withTypeMismatchBehaviour(OnTypeMismatch.EMPTY_RESULT);
		
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
	public void convertsToBigDecimal() {
		assertThat(converter.convert("10.99", BigDecimal.class)).isEqualTo(new BigDecimal("10.99"));
	}
	
	@Test
	public void convertsValueWithoutDecimalPointToBigDecimal() {
		assertThat(converter.convert("10", BigDecimal.class)).isEqualTo(new BigDecimal("10"));
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
