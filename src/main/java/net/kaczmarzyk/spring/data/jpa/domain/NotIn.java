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

import java.util.Arrays;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;

/**
 * <p>Negation of {@link net.kaczmarzyk.spring.data.jpa.domain.In In}.</p>
 * 
 * <p>Filters with "not in" where-clause (e.g. {@code where firstName not in ("Homer", "Marge")}).</p>
 * 
 * <p>Values to match against should be provided as multiple values of a single HTTP parameter, eg.: 
 *  {@code GET http://myhost/customers?firstName=Homer&firstName=Marge}.</p>
 * 
 * <p>Supports multiple field types: strings, numbers, booleans, enums, dates.</p>
 * 
 * @author Tomasz Kaczmarzyk
 */
public class NotIn<T> extends PathSpecification<T> {

	private static final long serialVersionUID = 1L;
	
	private String[] allowedValues;
	private Converter converter;

	public NotIn(QueryContext queryContext, String path, String[] httpParamValues, Converter converter) {
		super(queryContext, path);
		if (httpParamValues == null || httpParamValues.length < 1) {
			throw new IllegalArgumentException();
		}
		this.allowedValues = httpParamValues;
		this.converter = converter;
	}
	
	@Override
	public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
		Path<?> path = path(root);
		Class<?> typeOnPath = path.getJavaType();
		return path.in(converter.convert(Arrays.asList(allowedValues), typeOnPath)).not();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(allowedValues);
		result = prime * result + ((converter == null) ? 0 : converter.hashCode());
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
		NotIn other = (NotIn) obj;
		if (!Arrays.equals(allowedValues, other.allowedValues))
			return false;
		if (converter == null) {
			if (other.converter != null)
				return false;
		} else if (!converter.equals(other.converter))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "NotIn [allowedValues=" + Arrays.toString(allowedValues) + "]";
	}
}
