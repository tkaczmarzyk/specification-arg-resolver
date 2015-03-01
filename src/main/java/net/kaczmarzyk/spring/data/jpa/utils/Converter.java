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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * @author Tomasz Kaczmarzyk
 */
@SuppressWarnings("unchecked")
public class Converter {

	private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
	
	private String dateFormat;
	
	
	private Converter(String dateFormat) {
		this.dateFormat = dateFormat;
	}
	
	public <T> T convert(String value, Class<T> expectedClass) {
		if (expectedClass == Long.class) {
			return (T) Long.valueOf(value);
		}
		if (expectedClass == Integer.class) {
			return (T) Integer.valueOf(value);
		}
		else if (expectedClass.isEnum()) {
			return (T) convertToEnum(value, (Class<? extends Enum<?>>) expectedClass);
		}
		else if (expectedClass.isAssignableFrom(Date.class)) {
			return (T) convertToDate(value);
		}
		return (T) value;
	}

	public Date convertToDate(String value) {
		try {
			return new SimpleDateFormat(dateFormat).parse(value);
		} catch (ParseException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private <T> T convertToEnum(String value, Class<? extends Enum<?>> enumClass) {
		for (Enum<?> enumVal : enumClass.getEnumConstants()) {
			if (enumVal.name().equals(value)) {
				return (T) enumVal;
			}
		}
		throw new IllegalArgumentException("could not find value " + value + " for enum class " + enumClass.getSimpleName());
	}

	public static Converter withDateFormat(String dateFormat) {
		return new Converter(dateFormat != null ? dateFormat : DEFAULT_DATE_FORMAT);
	}
}
