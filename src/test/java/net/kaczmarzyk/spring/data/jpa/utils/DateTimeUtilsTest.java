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
package net.kaczmarzyk.spring.data.jpa.utils;

import org.junit.Test;

import java.sql.Timestamp;
import java.time.*;
import java.util.Calendar;
import java.util.Date;

import static java.time.ZoneOffset.ofHours;
import static java.util.Calendar.DECEMBER;
import static org.assertj.core.api.Assertions.assertThat;

public class DateTimeUtilsTest {

	@Test
	public void shouldReturnStartOfDayForLocalDateTime() {
		//given
		LocalDateTime date = LocalDateTime.of(2022, 12, 28, 15, 22, 42, 11);

		//when
		LocalDateTime startOfDayLocalDateTime = DateTimeUtils.startOfDay(date);

		//then
		assertThat(startOfDayLocalDateTime)
				.isEqualTo(LocalDateTime.of(2022, 12, 28, 0,0, 0, 0));
	}

	@Test
	public void shouldReturnStartOfDayForCalendar() {
		//given
		Calendar calendar = Calendar.getInstance();
		calendar.set(2022, DECEMBER, 28, 15, 55, 49);

		//when
		Calendar startOfDayCalendar = DateTimeUtils.startOfDay(calendar);

		//then
		assertThat(startOfDayCalendar.getTime())
				.isWithinYear(2022)
				.isWithinMonth(12)
				.isWithinDayOfMonth(28)
				.hasHourOfDay(0)
				.hasMinute(0)
				.hasSecond(0)
				.hasMillisecond(0);
	}

	@Test
	public void shouldReturnTheSameLocalDateWhenDeterminingStartOfDay() {
		//given
		LocalDate date = LocalDate.of(2022, 12, 28);

		//when
		LocalDate startOfDayLocalDate = DateTimeUtils.startOfDay(date);

		//then
		assertThat(startOfDayLocalDate)
				.isEqualTo(date);
	}

	@Test
	public void shouldReturnStartOfDayForOffsetDateTime() {
		//given
		OffsetDateTime date = OffsetDateTime.of(2022, 12, 28, 15, 22, 42, 11233, ofHours(4));

		//when
		OffsetDateTime startOfDayOffsetDateTime = DateTimeUtils.startOfDay(date);

		//then
		assertThat(startOfDayOffsetDateTime)
				.isEqualTo(OffsetDateTime.of(2022, 12, 28, 0, 0, 0, 0, ofHours(4)));
	}

	@Test
	public void shouldReturnStartOfDayForInstant() {
		//given
		Instant date = OffsetDateTime.of(2022, 12, 28, 15, 22, 42, 11233, ofHours(0)).toInstant();

		//when
		Instant startOfDayInstant = DateTimeUtils.startOfDay(date);

		//then
		assertThat(startOfDayInstant)
				.isEqualTo(OffsetDateTime.of(2022, 12, 28, 0, 0, 0, 0, ofHours(0)).toInstant());
	}

	@Test
	public void shouldReturnStartOfDayForDate() {
		//given
		Calendar calendar = Calendar.getInstance();
		calendar.set(2022, DECEMBER, 28, 15, 55, 49);
		Date date = calendar.getTime();

		//when
		Date startOfDayDate = DateTimeUtils.startOfDay(date);

		//then
		assertThat(startOfDayDate)
				.isWithinYear(2022)
				.isWithinMonth(12)
				.isWithinDayOfMonth(28)
				.hasHourOfDay(0)
				.hasMinute(0)
				.hasSecond(0)
				.hasMillisecond(0);
	}

	@Test
	public void shouldReturnStartOfDayForTimestamp() {
		//given
		Timestamp date = Timestamp.valueOf(LocalDateTime.of(2022, 12, 28, 15, 22, 42, 11));

		//when
		Timestamp startOfDayTimestamp = DateTimeUtils.startOfDay(date);

		//then
		assertThat(startOfDayTimestamp)
				.hasYear(2022)
				.hasMonth(12)
				.hasDayOfMonth(28)
				.hasHourOfDay(0)
				.hasMinute(0)
				.hasSecond(0)
				.hasMillisecond(0);
	}

	@Test
	public void shouldReturnStartOfNextDayForLocalDateTime() {
		//given
		LocalDateTime date = LocalDateTime.of(2022, 12, 28, 15, 22, 42, 11);

		//when
		LocalDateTime startOfNextDayLocalDateTime = DateTimeUtils.startOfNextDay(date);

		//then
		assertThat(startOfNextDayLocalDateTime)
				.isEqualTo(LocalDateTime.of(2022,12, 29, 0,0, 0, 0));
	}

	@Test
	public void shouldReturnStartOfNextDayForCalendar() {
		//given
		Calendar calendar = Calendar.getInstance();
		calendar.set(2022, DECEMBER, 28, 15, 55, 49);

		//when
		Calendar startOfNextDayCalendar = DateTimeUtils.startOfNextDay(calendar);

		//then
		assertThat(startOfNextDayCalendar.getTime())
				.isWithinYear(2022)
				.isWithinMonth(12)
				.isWithinDayOfMonth(29)
				.hasHourOfDay(0)
				.hasMinute(0)
				.hasSecond(0)
				.hasMillisecond(0);
	}

	@Test
	public void shouldReturnNextDayForLocalDate() {
		//given
		LocalDate date = LocalDate.of(2022, 12, 28);

		//when
		LocalDate nextDayLocalDate = DateTimeUtils.startOfNextDay(date);

		//then
		assertThat(nextDayLocalDate)
				.isEqualTo(LocalDate.of(2022, 12, 29));
	}

	@Test
	public void shouldReturnStartOfNextDayForOffsetDateTime() {
		//given
		OffsetDateTime date = OffsetDateTime.of(2022, 12, 28, 15, 22, 42, 11233, ofHours(4));

		//when
		OffsetDateTime startOfNextDayOffsetDateTime = DateTimeUtils.startOfNextDay(date);

		//then
		assertThat(startOfNextDayOffsetDateTime)
				.isEqualTo(OffsetDateTime.of(2022, 12, 29, 0, 0, 0, 0, ofHours(4)));
	}

	@Test
	public void shouldReturnStartOfNextDayForInstant() {
		//given
		Instant date = OffsetDateTime.of(2022, 12, 28, 15, 22, 42, 11233, ofHours(0)).toInstant();

		//when
		Instant startOfNextDayInstant = DateTimeUtils.startOfNextDay(date);

		//then
		assertThat(startOfNextDayInstant)
				.isEqualTo(OffsetDateTime.of(2022, 12, 29, 0, 0, 0, 0, ofHours(0)).toInstant());
	}

	@Test
	public void shouldReturnStartOfNextDayForDate() {
		//given
		Calendar calendar = Calendar.getInstance();
		calendar.set(2022, DECEMBER, 28, 15, 55, 49);
		Date date = calendar.getTime();

		//when
		Date startOfNextDayDate = DateTimeUtils.startOfNextDay(date);

		//then
		assertThat(startOfNextDayDate)
				.isWithinYear(2022)
				.isWithinMonth(12)
				.isWithinDayOfMonth(29)
				.hasHourOfDay(0)
				.hasMinute(0)
				.hasSecond(0)
				.hasMillisecond(0);
	}

	@Test
	public void shouldReturnStartOfNextDayForTimestamp() {
		//given
		Timestamp date = Timestamp.valueOf(LocalDateTime.of(2022, 12, 28, 15, 22, 42, 11));

		//when
		Timestamp startOfNextDayTimestamp = DateTimeUtils.startOfNextDay(date);

		//then
		assertThat(startOfNextDayTimestamp)
				.hasYear(2022)
				.hasMonth(12)
				.hasDayOfMonth(29)
				.hasHourOfDay(0)
				.hasMinute(0)
				.hasSecond(0)
				.hasMillisecond(0);
	}
}