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
package net.kaczmarzyk.spring.data.jpa.utils.converter;

import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static net.kaczmarzyk.spring.data.jpa.utils.ThrowableAssertions.assertThrows;
import static net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch.EMPTY_RESULT;
import static org.assertj.core.api.Assertions.assertThat;

public class StringToLocalDateConverterTest {
	
	Converter converterWithDefaultFormats = Converter.withTypeMismatchBehaviour(EMPTY_RESULT, null);
	
	@Test
	public void convertsToLocalDateTimeUsingDefaultFormat() {
		LocalDate localDate = converterWithDefaultFormats.convert("2020-06-19", LocalDate.class);
		
		assertThat(localDate)
				.isEqualTo("2020-06-19");
	}
	
	@Test
	public void throwsValueRejectedExceptionForUnparseableLocalDateTime() {
		assertThrows(
				Converter.ValueRejectedException.class,
				() -> converterWithDefaultFormats.convert("2020-06-19-2020", LocalDate.class),
				"LocalDate format exception, expected format: yyyy-MM-dd"
		);
	}
	
	@Test
	public void convertsToLocalDateTimeUsingCustomFormat() {
		Converter converterWithCustomFormat = Converter.withDateFormat("MM-yyyy-dd", EMPTY_RESULT, null);
		LocalDate localDate = converterWithCustomFormat.convert("06-2020-19", LocalDate.class);
		
		assertThat(localDate)
				.isEqualTo("2020-06-19");
	}
	
	@Test
	public void throwsValueRejectedExceptionForUnparseableLocalDateTimeAndCustomFormat() {
		Converter converterWithCustomFormat = Converter.withDateFormat("MM-yyyy-dd", EMPTY_RESULT, null);
		
		assertThrows(
				Converter.ValueRejectedException.class,
				() -> converterWithCustomFormat.convert("2020-06-19", LocalDate.class),
				"LocalDate format exception, expected format: MM-yyyy-dd"
		);
		
	}
	
	
}
