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
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

/**
 * <p>Filters with equal where-clause (e.g. {@code where firstName = "Homer"}).</p>
 * 
 * <p>Supports multiple field types: strings, numbers, booleans, enums, dates.</p>
 * 
 * @author Tomasz Kaczmarzyk
 * @author TP Diffenbach
 */
public class ComparableEqual<T> extends ComparableSpecification<T> {

	public ComparableEqual(String path, String[] httpParamValues) {
		super(path, httpParamValues, null);
	}
	
	public ComparableEqual(String path, String[] httpParamValues, String[] config) {
		super(path, httpParamValues, config);
	}

	@Override
	protected <Y extends Comparable<? super Y>> Predicate makePredicate(CriteriaBuilder cb, Expression<? extends Y> x, Y y) {
		return cb.equal(x, y);
	}
	
}
