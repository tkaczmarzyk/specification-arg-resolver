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
				.isEqualTo(LocalDateTime.of(2022,12, 28, 0,0, 0, 0));
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
	public void shouldReturnEndOfDayForLocalDateTime() {
		//given
		LocalDateTime date = LocalDateTime.of(2022, 12, 28, 15, 22, 42, 11);

		//when
		LocalDateTime endOfDayLocalDateTime = DateTimeUtils.endOfDay(date);

		//then
		assertThat(endOfDayLocalDateTime)
				.isEqualTo(LocalDateTime.of(2022,12, 28, 23,59, 59, 999999999));
	}

	@Test
	public void shouldReturnEndOfDayForCalendar() {
		//given
		Calendar calendar = Calendar.getInstance();
		calendar.set(2022, DECEMBER, 28, 15, 55, 49);

		//when
		Calendar endOfDayCalendar = DateTimeUtils.endOfDay(calendar);

		//then
		assertThat(endOfDayCalendar.getTime())
				.isWithinYear(2022)
				.isWithinMonth(12)
				.isWithinDayOfMonth(28)
				.hasHourOfDay(23)
				.hasMinute(59)
				.hasSecond(59)
				.hasMillisecond(999);
	}

	@Test
	public void shouldReturnTheSameLocalDateWhenDeterminingEndOfDay() {
		//given
		LocalDate date = LocalDate.of(2022, 12, 28);

		//when
		LocalDate endOfDayLocalDate = DateTimeUtils.endOfDay(date);

		//then
		assertThat(endOfDayLocalDate)
				.isEqualTo(date);
	}

	@Test
	public void shouldReturnEndOfDayForOffsetDateTime() {
		//given
		OffsetDateTime date = OffsetDateTime.of(2022, 12, 28, 15, 22, 42, 11233, ofHours(4));

		//when
		OffsetDateTime endOfDayOffsetDateTime = DateTimeUtils.endOfDay(date);

		//then
		assertThat(endOfDayOffsetDateTime)
				.isEqualTo(OffsetDateTime.of(2022, 12, 28, 23, 59, 59, 999999999, ofHours(4)));
	}

	@Test
	public void shouldReturnEndOfDayForInstant() {
		//given
		Instant date = OffsetDateTime.of(2022, 12, 28, 15, 22, 42, 11233, ofHours(0)).toInstant();

		//when
		Instant endOfDayInstant = DateTimeUtils.endOfDay(date);

		//then
		assertThat(endOfDayInstant)
				.isEqualTo(OffsetDateTime.of(2022, 12, 28, 23, 59, 59, 999999999, ofHours(0)).toInstant());
	}

	@Test
	public void shouldReturnEndOfDayForDate() {
		//given
		Calendar calendar = Calendar.getInstance();
		calendar.set(2022, DECEMBER, 28, 15, 55, 49);
		Date date = calendar.getTime();

		//when
		Date endOfDayDate = DateTimeUtils.endOfDay(date);

		//then
		assertThat(endOfDayDate)
				.isWithinYear(2022)
				.isWithinMonth(12)
				.isWithinDayOfMonth(28)
				.hasHourOfDay(23)
				.hasMinute(59)
				.hasSecond(59)
				.hasMillisecond(999);
	}

	@Test
	public void shouldReturnEndOfDayForTimestamp() {
		//given
		Timestamp date = Timestamp.valueOf(LocalDateTime.of(2022, 12, 28, 15, 22, 42, 11));

		//when
		Timestamp endOfDayTimestamp = DateTimeUtils.endOfDay(date);

		//then
		assertThat(endOfDayTimestamp)
				.hasYear(2022)
				.hasMonth(12)
				.hasDayOfMonth(28)
				.hasHourOfDay(23)
				.hasMinute(59)
				.hasSecond(59)
				.hasMillisecond(999);
	}
}