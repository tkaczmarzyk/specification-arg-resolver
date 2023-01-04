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

import java.sql.Timestamp;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

@SuppressWarnings("unchecked")
public abstract class DateTimeUtils {

	private static final String UTC_ZONE_ID = "UTC";

	public static <T> T startOfDay(Object dateObject) {
		if (dateObject instanceof Calendar) {
			Calendar calendar = (Calendar) dateObject;
			Calendar startOfDayCalendar = Calendar.getInstance();
			startOfDayCalendar.setTime(calendar.getTime());
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			return (T) calendar;
		} else if (dateObject instanceof LocalDateTime) {
			LocalDateTime date = (LocalDateTime) dateObject;
			return (T) LocalDateTime.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), 0, 0, 0, 0);
		} else if (dateObject instanceof LocalDate) {
			LocalDate date = (LocalDate) dateObject;
			return (T) LocalDate.of(date.getYear(), date.getMonth(), date.getDayOfMonth());
		} else if (dateObject instanceof OffsetDateTime) {
			OffsetDateTime date = (OffsetDateTime) dateObject;
			return (T) OffsetDateTime.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), 0, 0, 0, 0, date.getOffset());
		} else if (dateObject instanceof Instant) {
			return (T) ((Instant) dateObject).truncatedTo(ChronoUnit.DAYS);
		} else if (dateObject instanceof Timestamp) {
			Timestamp date = (Timestamp) dateObject;
			return (T) new Timestamp(date.getYear(), date.getMonth(), date.getDate(), 0, 0, 0, 0);
		} else if (dateObject instanceof Date) {
			Date date = (Date) dateObject;
			return (T) new Date(date.getYear(), date.getMonth(), date.getDate());
		} else {
			throw new IllegalArgumentException("Could not recognize date object!");
		}
	}

	public static <T> T endOfDay(Object dateObject) {
		if (dateObject instanceof Calendar) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(((Calendar) dateObject).getTime());
			return (T) atEndOfDayFor(calendar);
		} else if (dateObject instanceof LocalDateTime) {
			return (T) atEndOfDayFor((LocalDateTime) dateObject);
		} else if (dateObject instanceof LocalDate) {
			return (T) dateObject;
		} else if (dateObject instanceof OffsetDateTime) {
			return (T) atEndOfDayFor((OffsetDateTime) dateObject);
		} else if (dateObject instanceof Instant) {
			Instant date = (Instant) dateObject;
			LocalDateTime localInstant = LocalDateTime.ofInstant(date, ZoneId.of(UTC_ZONE_ID));
			return (T) atEndOfDayFor(localInstant).toInstant(ZoneOffset.UTC);
		} else if (dateObject instanceof Timestamp) {
			Timestamp date = (Timestamp) dateObject;
			LocalDateTime localDateTime = date.toLocalDateTime();
			return (T) Timestamp.valueOf(atEndOfDayFor(localDateTime));
		} else if (dateObject instanceof Date) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime((Date) dateObject);
			return (T) atEndOfDayFor(calendar).getTime();
		} else {
			throw new IllegalArgumentException("Could not recognize date object!");
		}
	}

	private static Calendar atEndOfDayFor(Calendar calendar) {
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 999);
		return calendar;
	}

	private static LocalDateTime atEndOfDayFor(LocalDateTime date) {
		return LocalDateTime.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), 23, 59, 59, 999999999);
	}

	private static OffsetDateTime atEndOfDayFor(OffsetDateTime date) {
		return OffsetDateTime.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), 23, 59, 59, 999999999, date.getOffset());
	}
}
