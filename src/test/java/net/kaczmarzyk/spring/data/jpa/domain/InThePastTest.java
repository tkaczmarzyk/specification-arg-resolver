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
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;

public class InThePastTest extends ComparableTestBase {

	private static final OffsetDateTime FUTURE_DATE_TIME = OffsetDateTime.now().plusDays(10);
	private static final OffsetDateTime PAST_DATE_TIME = OffsetDateTime.now().minusMonths(10);

	@Before
	public void initData() {
		homerSimpson = customer("Homer", "Simpson")
				.registrationDate(PAST_DATE_TIME.getYear(), PAST_DATE_TIME.getMonth().getValue(), PAST_DATE_TIME.getDayOfMonth() - 1)
				.nextSpecialOffer(PAST_DATE_TIME)
				.birthDate(FUTURE_DATE_TIME.toLocalDate())
				.lastOrderTime(PAST_DATE_TIME.toLocalDateTime())
				.build(em);
		margeSimpson = customer("Marge", "Simpson")
				.registrationDate(PAST_DATE_TIME.getYear(), PAST_DATE_TIME.getMonth().getValue() - 2, PAST_DATE_TIME.getDayOfMonth())
				.nextSpecialOffer(FUTURE_DATE_TIME)
				.birthDate(PAST_DATE_TIME.toLocalDate())
				.lastOrderTime(PAST_DATE_TIME.toLocalDateTime().minusMonths(2))
				.build(em);
		moeSzyslak = customer("Moe", "Szyslak")
				.registrationDate(FUTURE_DATE_TIME.getYear(), FUTURE_DATE_TIME.getMonth().getValue(), FUTURE_DATE_TIME.getDayOfMonth())
				.nextSpecialOffer(PAST_DATE_TIME.minusDays(30))
				.birthDate(PAST_DATE_TIME.toLocalDate().minusDays(10))
				.lastOrderTime(FUTURE_DATE_TIME.toLocalDateTime())
				.build(em);

		/*
		 *         | registration | next special offer | birth date | last order time
		 * ------  | ------------ | ------------------ | ---------- | ----------------
		 * HOMER   |     PAST     |        PAST        |   FUTURE   |      PAST
		 * MARGE   |     PAST     |       FUTURE       |    PAST    |      PAST
		 * MOE     |    FUTURE    |        PAST        |    PAST    |     FUTURE
		 */
	}

	@Override
	protected Specification<Customer> makeUUT(String path, String[] value, Converter converter) {
		return new InThePast<>(queryCtx, path);
	}

	@Test
	public void filtersByInstant() {
		assertFilterContainsOnlyExpectedMembers("dateOfNextSpecialOfferInstant", homerSimpson, moeSzyslak);
	}

	@Test
	public void filtersByDate() {
		assertFilterContainsOnlyExpectedMembers("registrationDate", homerSimpson, margeSimpson);
	}

	@Test
	public void filtersByLocalDate() {
		assertFilterContainsOnlyExpectedMembers("birthDate", margeSimpson, moeSzyslak);
	}

	@Test
	public void filtersByLocalDateTime() {
		assertFilterContainsOnlyExpectedMembers("lastOrderTime", homerSimpson, margeSimpson);
	}

	@Test
	public void filtersOffsetDateTime() {
		assertFilterContainsOnlyExpectedMembers("dateOfNextSpecialOffer", homerSimpson, moeSzyslak);
	}

	@Test
	public void filtersByTimestamp() {
		assertFilterContainsOnlyExpectedMembers("dateOfNextSpecialOfferTimestamp", homerSimpson, moeSzyslak);
	}

	@Test
	public void filtersByZonedDateTime() {
		assertFilterContainsOnlyExpectedMembers("dateOfNextSpecialOfferZoned", homerSimpson, moeSzyslak);
	}

	@Test
	public void filtersByCalendar() {
		assertFilterContainsOnlyExpectedMembers("registrationCalendar", homerSimpson, margeSimpson);
	}
}