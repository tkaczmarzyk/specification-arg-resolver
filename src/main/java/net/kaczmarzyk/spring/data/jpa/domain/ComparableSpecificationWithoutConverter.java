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
package net.kaczmarzyk.spring.data.jpa.domain;

import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;

import javax.persistence.criteria.*;

public abstract class ComparableSpecificationWithoutConverter<T> extends PathSpecification<T> {

	private static final long serialVersionUID = 1L;

	public ComparableSpecificationWithoutConverter(QueryContext queryContext, String path) {
		super(queryContext, path);
	}

	@Override
	public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
		Expression<?> rootPath = path(root);

		return makePredicate(cb, (Expression<? extends Comparable>) rootPath);
	}

	protected abstract <Y extends Comparable<? super Y>>
	Predicate makePredicate(CriteriaBuilder cb, Expression<? extends Y> x);
}
