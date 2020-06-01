/**
 * Copyright 2014-2020 the original author or authors.
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
import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;

/**
 * <p>Filters with equal where-clause (e.g. {@code where firstName = "Homer"}).</p>
 * 
 * <p>Supports multiple field types: strings, numbers, booleans, enums, dates.</p>
 * 
 * @author Tomasz Kaczmarzyk
 */
public class Equal<T> extends PathSpecification<T> {

	private static final long serialVersionUID = 1L;
	
	protected String expectedValue;
	private Converter converter;	
	
	
	public Equal(QueryContext queryContext, String path, String[] httpParamValues, Converter converter) {
		super(queryContext, path);
		if (httpParamValues == null || httpParamValues.length != 1) {
			throw new IllegalArgumentException();
		}
		this.expectedValue = httpParamValues[0];
		this.converter = converter;
	}
	
	@Override
	public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
		Class<?> typeOnPath = path(root).getJavaType();
		return cb.equal(path(root), converter.convert(expectedValue, typeOnPath));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((converter == null) ? 0 : converter.hashCode());
		result = prime * result + ((expectedValue == null) ? 0 : expectedValue.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Equal other = (Equal) obj;
		if (converter == null) {
			if (other.converter != null)
				return false;
		} else if (!converter.equals(other.converter))
			return false;
		if (expectedValue == null) {
			if (other.expectedValue != null)
				return false;
		} else if (!expectedValue.equals(other.expectedValue))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Equal [expectedValue=" + expectedValue + ", converter=" + converter + ", path=" + super.path + "]";
	}
}
