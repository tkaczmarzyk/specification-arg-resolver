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
import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;

/**
 * <p>A negation of {@link net.kaczmarzyk.spring.data.jpa.domain.Like Like}</p>
 * 
 * <p>Filters with {@code path not like %pattern%} where-clause.</p>
 * 
 * @author Tomasz Kaczmarzyk
 */
public class NotLike<T> extends Like<T> {
	
	private static final long serialVersionUID = 1L;

	public NotLike(QueryContext queryContext, String path, String... args) {
		super(queryContext, path, args);
	}

	@Override
	public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
		Predicate likePredicate = super.toPredicate(root, query, builder);
		return builder.not(likePredicate);
	}

	@Override
	public String toString() {
		return "NotLike[" +
				"argument='" + argument + '\'' +
				", escapeChar='" + escapeChar + '\'' +
				", pattern='" + pattern + '\'' +
				", path='" + path + '\'' +
				']';
	}
}
