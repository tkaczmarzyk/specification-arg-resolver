/**
 * Copyright 2014-2015 the original author or authors.
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

import java.util.Date;

import static org.assertj.core.api.Assertions.*;
import net.kaczmarzyk.spring.data.jpa.Gender;

import org.junit.Test;


public class ConverterTest {

	Converter converter = Converter.withDateFormat("yyyy-MM-dd");
	
	
	@Test
	public void convertsToDate() {
		Date converted = converter.convert("2015-03-01", Date.class);
		
		assertThat(converted)
			.isWithinMonth(3)
			.isWithinDayOfMonth(1)
			.isWithinYear(2015);
	}
	
	@Test
	public void convertsToLong() {
		assertThat(converter.convert("143", Long.class)).isEqualTo(143L);
	}
	
	@Test
	public void convertsToInt() {
		assertThat(converter.convert("143", Integer.class)).isEqualTo(143);
	}
	
	@Test
	public void stringIsPassedThrough() {
		assertThat(converter.convert("143", String.class)).isEqualTo("143");
	}
	
	@Test
	public void convertsToEnum() {
		assertThat(converter.convert("FEMALE", Gender.class)).isEqualTo(Gender.FEMALE);
	}
}
