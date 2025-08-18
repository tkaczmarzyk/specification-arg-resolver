/*
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
package net.kaczmarzyk.spring.data.jpa.domain;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Expression;
import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;

import java.util.Arrays;
import java.util.Objects;

import static net.kaczmarzyk.spring.data.jpa.utils.DateTimeUtils.startOfDay;
import static net.kaczmarzyk.spring.data.jpa.utils.DateTimeUtils.startOfNextDay;

/**
 * <p>Matches the day part of a date-time attribute, ignoring the time.
 * Internally uses `Between` specification with range within one day from `00:00:00` (inclusive) to `00:00:00` of the next day (exclusive).</p>
 *
 * <p>Supports date-type fields.</p>
 *
 * @author Hubert Gotfryd (Tratif sp. z o.o.)
 */
public class EqualDay<T> extends PathSpecification<T> {

	private static final long serialVersionUID = 1L;

	private final String expectedDay;
	private final Converter converter;

	public EqualDay(QueryContext queryContext, String path, String[] httpParamValues, Converter converter) {
		super(queryContext, path);
		if (httpParamValues == null || httpParamValues.length != 1) {
			throw new IllegalArgumentException("Invalid size of 'httpParamValues' array, Expected 1 but was " + Arrays.toString(httpParamValues));
		}
		this.converter = converter;
		this.expectedDay = httpParamValues[0];
	}

	@Override
	public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
		Expression<Comparable<Object>> targetExpression = path(root);
		Class<?> typeOnPath = targetExpression.getJavaType();

		Object targetDayDate = converter.convert(expectedDay, typeOnPath);
		Predicate lowerBoundaryPredicate = criteriaBuilder.greaterThanOrEqualTo(targetExpression, (Comparable<Object>) startOfDay(targetDayDate));
		Predicate upperBoundaryPredicate = criteriaBuilder.lessThan(targetExpression, (Comparable<Object>) startOfNextDay(targetDayDate));
		return criteriaBuilder.and(lowerBoundaryPredicate, upperBoundaryPredicate);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		EqualDay<?> equalDay = (EqualDay<?>) o;
		return Objects.equals(expectedDay, equalDay.expectedDay) &&
				Objects.equals(converter, equalDay.converter);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), expectedDay, converter);
	}

	@Override
	public String toString() {
		return "EqualDay[" +
				"expectedDay='" + expectedDay + '\'' +
				", converter=" + converter +
				", path='" + super.path + '\'' +
				']';
	}
}
