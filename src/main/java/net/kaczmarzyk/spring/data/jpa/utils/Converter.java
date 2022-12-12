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
package net.kaczmarzyk.spring.data.jpa.utils;

import net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch;
import org.springframework.core.convert.ConversionService;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.*;
import java.util.function.BiFunction;

import static java.util.Objects.nonNull;


/**
 * @author Tomasz Kaczmarzyk
 */
@SuppressWarnings("unchecked")
public class Converter {
	
	public static class ValuesRejectedException extends IllegalArgumentException {
		
		private static final long serialVersionUID = 1L;
		
		private Collection<String> rejectedValues;
		
		public ValuesRejectedException(Collection<String> rejectedValues, String message) {
			super(message);
			this.rejectedValues = rejectedValues;
		}
		
		public Collection<String> getRejectedValues() {
			return rejectedValues;
		}
		
		@Override
		public String toString() {
			return this.getClass() + ": " + getMessage();
		}
	}
	
	public static class ValueRejectedException extends IllegalArgumentException {
		
		private static final long serialVersionUID = 1L;
		
		private String rejectedValue;
		
		public ValueRejectedException(String rejectedValue, String message) {
			super(message);
			this.rejectedValue = rejectedValue;
		}
		
		public String getRejectedValue() {
			return rejectedValue;
		}
		
		@Override
		public String toString() {
			return this.getClass().getSimpleName() + ": " + getMessage();
		}
	}

	private static Map<Class<?>, String> DEFAULT_DATE_FORMATS = new HashMap<>();
	
	static {
		DEFAULT_DATE_FORMATS.put(Date.class, "yyyy-MM-dd");
		DEFAULT_DATE_FORMATS.put(LocalDate.class, "yyyy-MM-dd");
		DEFAULT_DATE_FORMATS.put(LocalDateTime.class, "yyyy-MM-dd\'T\'HH:mm:ss");
		DEFAULT_DATE_FORMATS.put(OffsetDateTime.class, "yyyy-MM-dd\'T\'HH:mm:ss.SSSXXX");
		DEFAULT_DATE_FORMATS.put(Instant.class, "yyyy-MM-dd\'T\'HH:mm:ss.SSSXXX");
		DEFAULT_DATE_FORMATS.put(Timestamp.class, "yyyy-MM-dd\'T\'HH:mm:ss.SSS\'Z\'");
	}

	private static BiFunction<Enum<?>, String, Boolean> enumMatcherCaseSensitive = (enumVal, rawValue) -> enumVal.name().equals(rawValue);

	private static BiFunction<Enum<?>, String, Boolean> enumMatcherCaseInsensitive =
			(enumVal, rawValue) -> enumVal.name().toUpperCase().equals(rawValue.toUpperCase());

	private String dateFormat;
	private OnTypeMismatch onTypeMismatch;
	
	private ConversionService conversionService;
	
	private Converter(String dateFormat, OnTypeMismatch onTypeMismatch, ConversionService conversionService) {
		this.dateFormat = dateFormat;
		this.onTypeMismatch = onTypeMismatch;
		this.conversionService = conversionService;
	}
	
	public <T> List<T> convert(List<String> values, Class<T> expectedClass) {
		if (expectedClass == String.class) {
			return (List<T>) values;
		}
		List<String> rejected = null;
		List<T> result = new ArrayList<>();
		for (String value : values) {
			try {
				result.add(convert(value, expectedClass));
			} catch (ValueRejectedException e) {
				if (rejected == null) {
					rejected = new ArrayList<>();
				}
				rejected.add(e.getRejectedValue());
			}
		}
		onTypeMismatch.handleRejectedValues(rejected);
		return result;
	}
	
	public <T> T convert(String value, Class<T> expectedClass) {
		return convert(value, expectedClass, false);
	}

	public <T> T convert(String value, Class<T> expectedClass, boolean ignoreCase) {
		if (expectedClass.isEnum()) {
			return (T) convertToEnum(value, (Class<? extends Enum<?>>) expectedClass, ignoreCase);
		} else if (expectedClass.isAssignableFrom(Date.class)) {
			return (T) convertToDate(value);
		} else if (expectedClass.isAssignableFrom(Calendar.class)) {
			return (T) convertToCalendar(value);
		} else if (isAssignableFromAnyOf(expectedClass, Boolean.class, boolean.class)) {
			return (T) convertToBoolean(value);
		} else if (isAssignableFromAnyOf(expectedClass, Integer.class, int.class, Long.class, long.class)) {
			return (T) convertToLong(value);
		} else if (isAssignableFromAnyOf(expectedClass, float.class, Float.class)) {
			return (T) convertToFloat(value);
		} else if (isAssignableFromAnyOf(expectedClass, double.class, Double.class)) {
			return (T) convertToDouble(value);
		} else if (expectedClass.isAssignableFrom(LocalDateTime.class)) {
			return (T) convertToLocalDateTime(value);
		} else if (expectedClass.isAssignableFrom(LocalDate.class)) {
			return (T) convertToLocalDate(value);
		} else if (expectedClass.isAssignableFrom(BigDecimal.class)) {
			return (T) convertToBigDecimal(value);
		} else if (expectedClass.isAssignableFrom(UUID.class)) {
			return (T) convertToUUID(value);
		} else if (expectedClass.isAssignableFrom(OffsetDateTime.class)) {
			return (T) convertToOffsetDateTime(value);
		} else if (expectedClass.isAssignableFrom(Instant.class)) {
			return (T) convertToInstant(value);
		} else if (expectedClass.isAssignableFrom(Timestamp.class)) {
			return (T) convertToTimestamp(value);
		} else if (nonNull(conversionService) && conversionService.canConvert(String.class, expectedClass)) {
			return conversionService.convert(value, expectedClass);
		}

		return (T) value;
	}
	
	private boolean isAssignableFromAnyOf(Class<?> expectedClass, Class<?>... candidates) {
		for (Class<?> candidate : candidates) {
			if (expectedClass.isAssignableFrom(candidate)) {
				return true;
			}
		}
		return false;
	}
	
	private LocalDate convertToLocalDate(String value) {
		String dateFormat = getDateFormat(LocalDate.class);
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
			return LocalDate.parse(value, formatter);
		} catch (DateTimeParseException | IllegalArgumentException e) {
			throw new ValueRejectedException(value, "LocalDate format exception, expected format: " + dateFormat);
		}
	}
	
	private LocalDateTime convertToLocalDateTime(String value) {
		String dateFormat = getDateFormat(LocalDateTime.class);
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
			return LocalDateTime.parse(value, formatter);
		} catch (DateTimeParseException | IllegalArgumentException e) {
			throw new ValueRejectedException(value, "LocalDateTime format exception, expected format:" + dateFormat);
		}
	}
	
	private Long convertToLong(String value) {
		try {
			return Long.valueOf(value);
		} catch (NumberFormatException e) {
			throw new ValueRejectedException(value, "number format exception");
		}
	}
	
	private Double convertToDouble(String value) {
		try {
			return Double.valueOf(value);
		} catch (NumberFormatException e) {
			throw new ValueRejectedException(value, "number format exception");
		}
	}
	
	private Float convertToFloat(String value) {
		try {
			return Float.valueOf(value);
		} catch (NumberFormatException e) {
			throw new ValueRejectedException(value, "number format exception");
		}
	}
	
	private BigDecimal convertToBigDecimal(String value) {
		try {
			return new BigDecimal(value);
		} catch (NumberFormatException e) {
			throw new ValueRejectedException(value, "number format exception");
		}
	}
	
	private Boolean convertToBoolean(String value) {
		if ("true".equals(value)) {
			return true;
		} else if ("false".equals(value)) {
			return false;
		} else {
			throw new ValueRejectedException(value, "unparseable boolean");
		}
	}
	
	public Date convertToDate(String value) {
		String dateFormat = getDateFormat(Date.class);
		try {
			validateDateFormat(dateFormat, value);
			return new SimpleDateFormat(dateFormat).parse(value);
		} catch (ParseException | DateTimeParseException e) {
			throw new ValueRejectedException(value, "Date format exception, expected format: " + dateFormat);
		}
	}

	public Calendar convertToCalendar(String value) {
		String dateFormat = getDateFormat(Date.class);
		try {
			validateDateFormat(dateFormat, value);
			Calendar cal = Calendar.getInstance();
			cal.setTime(new SimpleDateFormat(dateFormat).parse(value));
			return cal;
		} catch (ParseException | DateTimeParseException e) {
			throw new ValueRejectedException(value, "Date format exception, expected format: " + dateFormat);
		}
	}
	
	public String getDateFormat(Class<?> clazz) {
		if (dateFormat == null) {
			return DEFAULT_DATE_FORMATS.get(clazz);
		}
		
		return dateFormat;
	}
	
	public UUID convertToUUID(String value) {
		try {
			return UUID.fromString(value);
		} catch (IllegalArgumentException e) {
			throw new ValueRejectedException(value, "unparseable uuid");
		}
	}
	
	public OffsetDateTime convertToOffsetDateTime(String value) {
		String dateFormat = getDateFormat(OffsetDateTime.class);
		try {
			return OffsetDateTime.parse(value, DateTimeFormatter.ofPattern(dateFormat));
		} catch (DateTimeParseException | IllegalArgumentException e) {
			throw new ValueRejectedException(value, "OffsetDateTime format exception, expected format: " + dateFormat);
		}
	}
	
	public Instant convertToInstant(String value) {
		String dateFormat = getDateFormat(Instant.class);
		try {
			return Instant.from(DateTimeFormatter.ofPattern(dateFormat).parse(value));
		} catch (DateTimeParseException | IllegalArgumentException e) {
			throw new ValueRejectedException(value, "Instant format exception, expected format: " + dateFormat);
		}
	}

	private <T> T convertToEnum(String value, Class<? extends Enum<?>> enumClass, Boolean ignoreCase) {
		BiFunction<Enum<?>, String, Boolean> enumMatcher = ignoreCase ? enumMatcherCaseInsensitive : enumMatcherCaseSensitive;
		for (Enum<?> enumVal : enumClass.getEnumConstants()) {
			if (enumMatcher.apply(enumVal, value)) {
				return (T) enumVal;
			}
		}
		throw new ValueRejectedException(value, "could not find value " + value + " for enum class " + enumClass.getSimpleName());
	}

	private Timestamp convertToTimestamp(String value) {
		String dateFormat = getDateFormat(Timestamp.class);
		try {
			validateDateFormat(dateFormat, value);
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
			Date parsedDate = simpleDateFormat.parse(value);
			return new Timestamp(parsedDate.getTime());
		} catch (Exception e) {
			throw new ValueRejectedException(value, "Timestamp format exception, expected format: " + dateFormat);
		}
	}

	private void validateDateFormat(String expectedDateFormatPattern, String date) throws DateTimeParseException {
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(expectedDateFormatPattern)
				.withResolverStyle(ResolverStyle.STRICT);
		dateFormatter.parse(date);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dateFormat == null) ? 0 : dateFormat.hashCode());
		result = prime * result + ((onTypeMismatch == null) ? 0 : onTypeMismatch.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Converter converter = (Converter) o;
		return Objects.equals(dateFormat, converter.dateFormat) &&
				onTypeMismatch == converter.onTypeMismatch &&
				Objects.equals(conversionService, converter.conversionService);
	}

	@Override
	public String toString() {
		return "Converter [dateFormat=" + dateFormat + ", onTypeMismatch=" + onTypeMismatch + "]";
	}
	
	public static Converter withDateFormat(String dateFormat, OnTypeMismatch onTypeMismatch, ConversionService conversionService) {
		return new Converter(dateFormat, onTypeMismatch, conversionService);
	}
	
	public static Converter withTypeMismatchBehaviour(OnTypeMismatch onTypeMismatch, ConversionService conversionService) {
		return new Converter(null, onTypeMismatch, conversionService);
	}
	
}
