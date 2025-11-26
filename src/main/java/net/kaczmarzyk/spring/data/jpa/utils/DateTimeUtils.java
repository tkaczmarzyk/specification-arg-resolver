/**
 * Copyright 2014-2025 the original author or authors.
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

import java.sql.Timestamp;
import java.time.*;
import java.util.Calendar;
import java.util.Date;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Calendar.DATE;

@SuppressWarnings("unchecked")
public abstract class DateTimeUtils {

	public static <T> T startOfDay(Object dateObject) {
		Class<?> expectedClass = dateObject.getClass();
		if (Calendar.class.isAssignableFrom(expectedClass)) {
			return (T) atStartOfDayFor((Calendar) dateObject);
		} else if (LocalDateTime.class.isAssignableFrom(expectedClass)) {
			return (T) atStartOfDayFor((LocalDateTime) dateObject);
		} else if (LocalDate.class.isAssignableFrom(expectedClass)) {
			LocalDate date = (LocalDate) dateObject;
			return (T) LocalDate.of(date.getYear(), date.getMonth(), date.getDayOfMonth());
		} else if (OffsetDateTime.class.isAssignableFrom(expectedClass)) {
			return (T) atStartOfDayFor((OffsetDateTime) dateObject);
		} else if (Instant.class.isAssignableFrom(expectedClass)) {
			return (T) ((Instant) dateObject).truncatedTo(DAYS);
		} else if (Timestamp.class.isAssignableFrom(expectedClass)) {
			Timestamp date = (Timestamp) dateObject;
			return (T) new Timestamp(date.getYear(), date.getMonth(), date.getDate(), 0, 0, 0, 0);
		} else if (Date.class.isAssignableFrom(expectedClass)) {
			Date date = (Date) dateObject;
			return (T) new Date(date.getYear(), date.getMonth(), date.getDate());
		} else {
			throw new IllegalArgumentException("Could not recognize date object!");
		}
	}

	public static <T> T startOfNextDay(Object dateObject) {
		Class<?> expectedClass = dateObject.getClass();
		if (Calendar.class.isAssignableFrom(expectedClass)) {
			Calendar startOfNextDayCalendar = atStartOfDayFor((Calendar) dateObject);
			startOfNextDayCalendar.add(DATE, 1);
			return (T) startOfNextDayCalendar;
		} else if (LocalDateTime.class.isAssignableFrom(expectedClass)) {
			return (T) atStartOfDayFor((LocalDateTime) dateObject).plusDays(1);
		} else if (LocalDate.class.isAssignableFrom(expectedClass)) {
			return (T) ((LocalDate) dateObject).plusDays(1);
		} else if (OffsetDateTime.class.isAssignableFrom(expectedClass)) {
			return (T) atStartOfDayFor((OffsetDateTime) dateObject).plusDays(1);
		} else if (Instant.class.isAssignableFrom(expectedClass)) {
			return (T) ((Instant) dateObject).truncatedTo(DAYS).plus(1, DAYS);
		} else if (Timestamp.class.isAssignableFrom(expectedClass)) {
			Timestamp date = (Timestamp) dateObject;
			LocalDateTime localDateTime = date.toLocalDateTime();
			return (T) Timestamp.valueOf(atStartOfDayFor(localDateTime).plusDays(1));
		} else if (Date.class.isAssignableFrom(expectedClass)) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime((Date) dateObject);
			Calendar startOfNextDayCalendar = atStartOfDayFor(calendar);
			startOfNextDayCalendar.add(DATE, 1);
			return (T) startOfNextDayCalendar.getTime();
		} else {
			throw new IllegalArgumentException("Could not recognize date object!");
		}
	}

	private static Calendar atStartOfDayFor(Calendar calendar) {
		Calendar startOfDayCalendar = Calendar.getInstance();
		startOfDayCalendar.setTime(calendar.getTime());
		startOfDayCalendar.set(Calendar.HOUR_OF_DAY, 0);
		startOfDayCalendar.set(Calendar.MINUTE, 0);
		startOfDayCalendar.set(Calendar.SECOND, 0);
		startOfDayCalendar.set(Calendar.MILLISECOND, 0);
		return startOfDayCalendar;
	}

	private static LocalDateTime atStartOfDayFor(LocalDateTime date) {
		return LocalDateTime.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), 0, 0, 0, 0);
	}

	private static OffsetDateTime atStartOfDayFor(OffsetDateTime date) {
		return OffsetDateTime.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), 0, 0, 0, 0, date.getOffset());
	}
}
