/**
 * Copyright 2014-2016 the original author or authors.
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

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import net.kaczmarzyk.spring.data.jpa.utils.Converter;

/**
 * <p>Filters with equal where-clause (e.g. {@code where firstName = "Homer"}).</p>
 * 
 * <p>Supports multiple field types: strings, numbers, booleans, enums, dates.</p>
 * 
 * @author Tomasz Kaczmarzyk
 */
public class Equal<T> extends PathSpecification<T> {

	private String expectedValue;
	private Converter converter;	
	
	public Equal(String path, String[] httpParamValues) {
		this(path, httpParamValues, null);
	}
	
	public Equal(String path, String[] httpParamValues, String[] config) {
		super(path);
		if (httpParamValues == null || httpParamValues.length != 1) {
			throw new IllegalArgumentException();
		}
		if (config != null && config.length != 1) {
			throw new IllegalArgumentException("config may contain only one value (date format), but was: " + config);
		}
		String dateFormat = config != null ? config[0] : null;
		expectedValue = httpParamValues[0];
		converter = Converter.withDateFormat(dateFormat);
	}
	
	@Override
	public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
		Class<?> typeOnPath = path(root).getJavaType();
		return cb.equal(path(root), converter.convert(expectedValue, typeOnPath));
	}

}
