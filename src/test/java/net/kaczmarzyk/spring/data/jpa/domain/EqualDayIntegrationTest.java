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
package net.kaczmarzyk.spring.data.jpa.domain;

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.IntegrationTestBase;
import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import static java.time.ZoneOffset.ofHours;
import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

/**
 * @author Robert Dworak (Tratif sp. z o.o.)
 */
public class EqualDayIntegrationTest extends IntegrationTestBase {

	private static final String INVALID_PARAMETER_ARRAY_SIZE_EXCEPTION_MESSAGE = "Invalid size of 'httpParamValues' array, Expected 1 but was ";

	protected Customer homerSimpson;
	protected Customer margeSimpson;
	protected Customer moeSzyslak;
	protected Customer joeQuimby;

	@BeforeEach
	public void initData() {
		homerSimpson = customer("Homer", "Simpson")
				.registrationDate(2015, 03, 01)
				.birthDate(LocalDate.of(1970, 03, 21))
				.lastOrderTime(LocalDateTime.of(2016, 8, 21, 14, 51,0))
				.nextSpecialOffer(OffsetDateTime.of(2020, 6, 16, 16, 17, 0, 0, ofHours(9)))
				.build(em);
		margeSimpson = customer("Marge", "Simpson")
				.registrationDate(2015, 03, 01)
				.birthDate(LocalDate.of(1970, 03, 21))
				.lastOrderTime(LocalDateTime.of(2012, 8, 11, 12, 11,0))
				.nextSpecialOffer(OffsetDateTime.of(2019, 6, 5, 13, 19, 0, 0, ofHours(9)))
				.build(em);
		moeSzyslak = customer("Moe", "Szyslak")
				.registrationDate(2015, 03, 02)
				.birthDate(LocalDate.of(2002, 01, 21))
				.lastOrderTime(LocalDateTime.of(2016, 8, 21, 5, 51,0))
				.nextSpecialOffer(OffsetDateTime.of(2020, 6, 16, 16, 17, 0, 0, ofHours(9)))
				.build(em);

		joeQuimby = customer("Joe", "Quimby")
				.golden()
				.build(em);
	}

	@Test
	public void filtersByInstantWithDefaultDateFormat() {
		EqualDay<Customer> firstDateOfNextSpecialOffer = new EqualDay<>(queryCtx, "dateOfNextSpecialOfferInstant", new String[] { "2020-06-16T08:34:13.000+04:00" }, defaultConverter);
		List<Customer> found = customerRepo.findAll(firstDateOfNextSpecialOffer);

		assertThat(found).hasSize(2).containsOnly(homerSimpson, moeSzyslak);

		EqualDay<Customer> secondDateOfNextSpecialOffer = new EqualDay<>(queryCtx, "dateOfNextSpecialOfferInstant", new String[] { "2019-06-05T09:34:13.000+04:00" }, defaultConverter);
		found = customerRepo.findAll(secondDateOfNextSpecialOffer);

		assertThat(found).hasSize(1).containsOnly(margeSimpson);
	}

	@Test
	public void filtersByInstantWithCustomDateFormat() {
		EqualDay<Customer> firstDateOfNextSpecialOffer = new EqualDay<>(queryCtx, "dateOfNextSpecialOfferInstant", new String[] { "2020/06/16T02:34" },
				Converter.withDateFormat("yyyy/MM/dd'T'HH:mm", OnTypeMismatch.EMPTY_RESULT, null));
		List<Customer> found = customerRepo.findAll(firstDateOfNextSpecialOffer);

		assertThat(found).hasSize(2).containsOnly(homerSimpson, moeSzyslak);
	}

	@Test
	public void filtersByTimestampWithDefaultDateFormat() {
		EqualDay<Customer> firstDateOfNextSpecialOffer = new EqualDay<>(queryCtx, "dateOfNextSpecialOfferTimestamp", new String[] { "2020-06-16T08:34:13.000Z" }, defaultConverter);
		List<Customer> found = customerRepo.findAll(firstDateOfNextSpecialOffer);

		assertThat(found).hasSize(2).containsOnly(homerSimpson, moeSzyslak);

		EqualDay<Customer> secondDateOfNextSpecialOffer = new EqualDay<>(queryCtx, "dateOfNextSpecialOfferTimestamp", new String[] { "2019-06-05T09:34:13.000Z" }, defaultConverter);
		found = customerRepo.findAll(secondDateOfNextSpecialOffer);

		assertThat(found).hasSize(1).containsOnly(margeSimpson);
	}

	@Test
	public void filtersByTimestampWithCustomDateFormat() {
		EqualDay<Customer> firstDateOfNextSpecialOffer = new EqualDay<>(queryCtx, "dateOfNextSpecialOfferTimestamp", new String[] { "2020/06/16T02:34" },
				Converter.withDateFormat("yyyy/MM/dd'T'HH:mm", OnTypeMismatch.EMPTY_RESULT, null));
		List<Customer> found = customerRepo.findAll(firstDateOfNextSpecialOffer);

		assertThat(found).hasSize(2).containsOnly(homerSimpson,  moeSzyslak);
	}

	@Test
	public void filtersByOffsetDateTimeWithDefaultDateFormat() {
		EqualDay<Customer> firstDateOfNextSpecialOffer = new EqualDay<>(queryCtx, "dateOfNextSpecialOffer", new String[] { "2020-06-16T08:34:13.000+04:00" }, defaultConverter);
		List<Customer> found = customerRepo.findAll(firstDateOfNextSpecialOffer);

		assertThat(found).hasSize(2).containsOnly(homerSimpson, moeSzyslak);

		EqualDay<Customer> secondDateOfNextSpecialOffer = new EqualDay<>(queryCtx, "dateOfNextSpecialOffer", new String[] { "2019-06-05T08:34:13.000+04:00" }, defaultConverter);
		found = customerRepo.findAll(secondDateOfNextSpecialOffer);

		assertThat(found).hasSize(1).containsOnly(margeSimpson);
	}

	@Test
	public void filtersByOffsetDateTimeWithCustomDateFormat() {
		EqualDay<Customer> firstDateOfNextSpecialOffer = new EqualDay<>(queryCtx, "dateOfNextSpecialOffer", new String[] { "2020/06/16T02:34" },
				Converter.withDateFormat("yyyy/MM/dd'T'HH:mm", OnTypeMismatch.EMPTY_RESULT, null));
		List<Customer> found = customerRepo.findAll(firstDateOfNextSpecialOffer);

		assertThat(found).hasSize(2).containsOnly(homerSimpson, moeSzyslak);
	}

	@Test
	public void filtersByLocalDateWithDefaultDateFormat() {
		EqualDay<Customer> firstBirthDate = new EqualDay<>(queryCtx, "birthDate", new String[] { "1970-03-21" }, defaultConverter);
		List<Customer> found = customerRepo.findAll(firstBirthDate);

		assertThat(found).hasSize(2).containsOnly(homerSimpson, margeSimpson);

		EqualDay<Customer> secondBirthDate = new EqualDay<>(queryCtx, "birthDate", new String[] { "2002-01-21" }, defaultConverter);
		found = customerRepo.findAll(secondBirthDate);

		assertThat(found).hasSize(1).containsOnly(moeSzyslak);
	}

	@Test
	public void filtersByLocalDateWithCustomDateFormat() {
		EqualDay<Customer> birthDate = new EqualDay<>(queryCtx, "birthDate", new String[] { "21-03-1970" },
				Converter.withDateFormat("dd-MM-yyyy", OnTypeMismatch.EMPTY_RESULT, null));
		List<Customer> found = customerRepo.findAll(birthDate);

		assertThat(found).hasSize(2).containsOnly(homerSimpson, margeSimpson);
	}

	@Test
	public void filtersByLocalDateTimeWithDefaultDateFormat() {
		EqualDay<Customer> lastOrderTime = new EqualDay<>(queryCtx, "lastOrderTime", new String[] { "2016-08-21T12:34:19" }, defaultConverter);
		List<Customer> found = customerRepo.findAll(lastOrderTime);

		assertThat(found).hasSize(2).containsOnly(homerSimpson, moeSzyslak);

		EqualDay<Customer> secondLastOrderTime = new EqualDay<>(queryCtx, "lastOrderTime", new String[] { "2012-08-11T02:34:19" }, defaultConverter);
		found = customerRepo.findAll(secondLastOrderTime);

		assertThat(found).hasSize(1).containsOnly(margeSimpson);
	}

	@Test
	public void filtersByLocalDateTimeWithCustomDateFormat() {
		EqualDay<Customer> lastOrderTime = new EqualDay<>(queryCtx, "lastOrderTime", new String[] { "2016/08/21T02:34" },
				Converter.withDateFormat("yyyy/MM/dd'T'HH:mm", OnTypeMismatch.EMPTY_RESULT, null));
		List<Customer> found = customerRepo.findAll(lastOrderTime);

		assertThat(found).hasSize(2).containsOnly(homerSimpson, moeSzyslak);
	}

	@Test
	public void filtersByCalendarWithDefaultDateFormat() {
		EqualDay<Customer> firstRegistrationCalendar = new EqualDay<>(queryCtx, "registrationCalendar", new String[] { "2015-03-01" }, defaultConverter);
		List<Customer> found = customerRepo.findAll(firstRegistrationCalendar);

		assertThat(found).hasSize(2).containsOnly(homerSimpson, margeSimpson);

		EqualDay<Customer> secondRegistrationCalendar = new EqualDay<>(queryCtx, "registrationCalendar", new String[] { "2015-03-02" }, defaultConverter);
		found = customerRepo.findAll(secondRegistrationCalendar);

		assertThat(found).hasSize(1).containsOnly(moeSzyslak);
	}

	@Test
	public void filtersByCalendarWithCustomDateFormat() {
		EqualDay<Customer> registrationCalendar = new EqualDay<>(queryCtx, "registrationCalendar", new String[] { "01-03-2015" },
				Converter.withDateFormat("dd-MM-yyyy", OnTypeMismatch.EMPTY_RESULT, null));
		List<Customer> found = customerRepo.findAll(registrationCalendar);

		assertThat(found).hasSize(2).containsOnly(homerSimpson, margeSimpson);
	}

	@Test
	public void filtersByDateWithDefaultDateFormat() {
		EqualDay<Customer> firstRegistrationDate = new EqualDay<>(queryCtx, "registrationDate", new String[] { "2015-03-01" }, defaultConverter);
		List<Customer> found = customerRepo.findAll(firstRegistrationDate);

		assertThat(found).hasSize(2).containsOnly(homerSimpson, margeSimpson);

		EqualDay<Customer> secondRegistrationDate = new EqualDay<>(queryCtx, "registrationDate", new String[] { "2015-03-02" }, defaultConverter);
		found = customerRepo.findAll(secondRegistrationDate);

		assertThat(found).hasSize(1).containsOnly(moeSzyslak);
	}

	@Test
	public void filtersByDateWithCustomDateFormat() {
		EqualDay<Customer> registrationDate = new EqualDay<>(queryCtx, "registrationDate", new String[] { "01-03-2015" },
				Converter.withDateFormat("dd-MM-yyyy", OnTypeMismatch.EMPTY_RESULT, null));
		List<Customer> found = customerRepo.findAll(registrationDate);

		assertThat(found).hasSize(2).containsOnly(homerSimpson, margeSimpson);
	}

	@Test
	public void rejectsNullArgumentArray() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> new EqualDay<>(queryCtx, "path", null, defaultConverter));

		assertThat(exception.getMessage())
				.isEqualTo(INVALID_PARAMETER_ARRAY_SIZE_EXCEPTION_MESSAGE + "null");
	}

	@Test
	public void rejectsMissingArguments() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> new EqualDay<>(queryCtx, "path", new String[] {}, defaultConverter));

		assertThat(exception.getMessage())
				.isEqualTo(INVALID_PARAMETER_ARRAY_SIZE_EXCEPTION_MESSAGE + "[]");
	}

	@Test
	public void rejectsTooManyArguments() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> new EqualDay<>(queryCtx, "path", new String[] {"2014-03-10", "2014-03-11"}, defaultConverter));

		assertThat(exception.getMessage())
				.isEqualTo(INVALID_PARAMETER_ARRAY_SIZE_EXCEPTION_MESSAGE + "[2014-03-10, 2014-03-11]");
	}
}
