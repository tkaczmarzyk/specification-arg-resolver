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
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;

import java.util.Objects;

/**
 * <p>Filters with {@code path between arg1 and arg2} where-clause.</p>
 * 
 * <p>Supports multiple field types: strings, numbers, booleans, enums, dates.</p>
 * 
 * <p>Field types must be Comparable (e.g, implement the Comparable interface); this is 
 * a JPA constraint.</p>
 * 
 * <p>NOTE: comparisons are dependent on the underlying database.</p>
 * <p>Comparisons of floats and doubles (especially floats) may be incorrect due to precision loss.</p>
 * <p>Comparisons of booleans may be dependent on the underlying database representation.</p>
 * <p>Comparisons of enums will be of their ordinal or string representations, depending on what you specified to JPA,
 * e.g., {@code @Enumerated(EnumType.STRING)}, {@code @Enumerated(EnumType.ORDINAL)} or the default ({@code @Enumerated(EnumType.ORDINAL)})</p>
 * 
 * @author Tomasz Kaczmarzyk
 * @author TP Diffenbach
 */
public class Between<T> extends PathSpecification<T> {

	private static final long serialVersionUID = 1L;

	private final String lowerBoundaryStr;
	private final String upperBoundaryStr;
	private final Converter converter;
	
	public Between(QueryContext queryContext, String path, String[] args, Converter converter) {
		super(queryContext, path);
        if (args == null || args.length != 2) {
            throw new IllegalArgumentException("expected 2 http params (lower and upper boundaries), but was: " + args);
        }
        this.converter = converter;
        this.lowerBoundaryStr = args[0];
        this.upperBoundaryStr = args[1];
	}

	@SuppressWarnings("unchecked")
	@Override
	public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
		Expression<Comparable<Object>> targetExpression = path(root);
		Class<?> typeOnPath = targetExpression.getJavaType();
		
		Comparable<Object> lowerBoundary = (Comparable<Object>) converter.convert(lowerBoundaryStr, typeOnPath);
		Comparable<Object> upperBoundary = (Comparable<Object>) converter.convert(upperBoundaryStr, typeOnPath);
		
		return criteriaBuilder.between(targetExpression, lowerBoundary, upperBoundary);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		Between<?> between = (Between<?>) o;
		return Objects.equals(lowerBoundaryStr, between.lowerBoundaryStr) &&
				Objects.equals(upperBoundaryStr, between.upperBoundaryStr) &&
				Objects.equals(converter, between.converter);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), lowerBoundaryStr, upperBoundaryStr, converter);
	}

	@Override
	public String toString() {
		return "Between[" +
				"lowerBoundaryStr='" + lowerBoundaryStr + '\'' +
				", upperBoundaryStr='" + upperBoundaryStr + '\'' +
				", converter=" + converter +
				']';
	}
}
