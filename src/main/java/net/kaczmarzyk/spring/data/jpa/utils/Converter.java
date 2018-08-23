/**
 * Copyright 2014-2017 the original author or authors.
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch;


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
	
	private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

	public static final Converter DEFAULT = Converter.withDateFormat(DEFAULT_DATE_FORMAT, OnTypeMismatch.EMPTY_RESULT);
	
	private String dateFormat;
	private OnTypeMismatch onTypeMismatch;
	
	
	private Converter(String dateFormat, OnTypeMismatch onTypeMismatch) {
		this.dateFormat = dateFormat;
		this.onTypeMismatch = onTypeMismatch;
	}
	
	public <T> T convert(String value, Class<T> expectedClass) {
		if (expectedClass.isEnum()) {
			return (T) convertToEnum(value, (Class<? extends Enum<?>>) expectedClass);
		}
		else if (expectedClass.isAssignableFrom(Date.class)) {
			return (T) convertToDate(value);
		}
		else if (expectedClass.isAssignableFrom(Boolean.class) || expectedClass.isAssignableFrom(boolean.class)) {
			return (T) convertToBoolean(value);
		} else if (expectedClass.isAssignableFrom(Integer.class) || expectedClass.isAssignableFrom(Long.class)) {
			return (T) convertToLong(value);
		} else if (expectedClass.isAssignableFrom(UUID.class)) {
			return (T) convertToUUID(value);
		}
		return (T) value;
	}

	private UUID convertToUUID(String value) {
		return UUID.fromString(value);
	}

	private Long convertToLong(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            throw new ValueRejectedException(value, "number format exception");
        }
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
		try {
			return new SimpleDateFormat(dateFormat).parse(value);
		} catch (ParseException e) {
			throw new ValueRejectedException(value, "invalid date, expected format: " + dateFormat);
		}
	}

	private <T> T convertToEnum(String value, Class<? extends Enum<?>> enumClass) {
		for (Enum<?> enumVal : enumClass.getEnumConstants()) {
			if (enumVal.name().equals(value)) {
				return (T) enumVal;
			}
		}
		throw new ValueRejectedException(value, "could not find value " + value + " for enum class " + enumClass.getSimpleName());
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
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Converter other = (Converter) obj;
		if (dateFormat == null) {
			if (other.dateFormat != null)
				return false;
		} else if (!dateFormat.equals(other.dateFormat))
			return false;
		if (onTypeMismatch != other.onTypeMismatch)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "Converter [dateFormat=" + dateFormat + ", onTypeMismatch=" + onTypeMismatch + "]";
	}

	public static Converter withDateFormat(String dateFormat, OnTypeMismatch onTypeMismatch) {
		return new Converter(dateFormat != null ? dateFormat : DEFAULT_DATE_FORMAT, onTypeMismatch);
	}

	public static Converter withTypeMismatchBehaviour(OnTypeMismatch onTypeMismatch) {
		return new Converter(DEFAULT_DATE_FORMAT, onTypeMismatch);
	}
}
