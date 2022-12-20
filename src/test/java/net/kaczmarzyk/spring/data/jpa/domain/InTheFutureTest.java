/**
 * Copyright 2014-2022 the original author or authors.
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
package net.kaczmarzyk.spring.data.jpa.domain;

import net.kaczmarzyk.spring.data.jpa.ComparableTestBase;
import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import org.junit.Test;
import org.springframework.data.jpa.domain.Specification;

import java.sql.Date;
import java.time.*;

public class InTheFutureTest extends ComparableTestBase {

	@Override
	protected Specification<Customer> makeUUT(String path, String[] value, Converter converter) {
		return new InTheFuture<>(queryCtx, path);
	}

	@Test
	public void filtersByInstant() {
		OffsetDateTime offsetDateTime = OffsetDateTime.of(LocalDate.of(2035, 01, 01), LocalTime.now(), ZoneOffset.ofHours(2));
		homerSimpson.setDateOfNextSpecialOffer(offsetDateTime);
		assertFilterContainsOnlyExpectedMembers("dateOfNextSpecialOfferInstant", homerSimpson);
	}

	@Test
	public void filtersByDate() {
		Date date = Date.valueOf(LocalDate.of(2035, 01, 01));
		homerSimpson.setRegistrationDate(date);
		assertFilterContainsOnlyExpectedMembers("registrationDate", homerSimpson);
	}

	@Test
	public void filtersByLocalDate() {
		LocalDate localDate = LocalDate.of(2035, 01, 01);
		homerSimpson.setBirthDate(localDate);
		assertFilterContainsOnlyExpectedMembers("birthDate", homerSimpson);
	}

	@Test
	public void filtersByLocalDateTime() {
		LocalDateTime localDateTime = LocalDateTime.of(2035, 01, 01, 12, 12);
		homerSimpson.setLastOrderTime(localDateTime);
		assertFilterContainsOnlyExpectedMembers("lastOrderTime", homerSimpson);
	}

	@Test
	public void filtersOffsetDateTime() {
		OffsetDateTime offsetDateTime = OffsetDateTime.of(2035, 01, 01, 12, 12, 12, 44, ZoneOffset.ofHours(2));
		homerSimpson.setDateOfNextSpecialOffer(offsetDateTime);
		assertFilterContainsOnlyExpectedMembers("dateOfNextSpecialOffer", homerSimpson);
	}

}