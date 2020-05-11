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

import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;

/**
 * Filters with {@code path like %pattern%} where-clause and ignores pattern case
 * 
 * @author Michal Jankowski, Hazecod
 *
 */
public class LikeIgnoreCase<T> extends Like<T> {

	private static final long serialVersionUID = 1L;

	public LikeIgnoreCase(QueryContext queryCtx, String path, String... args) {
        super(queryCtx, path, args);
    }
	
    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        return builder.like(builder.upper(this.<String> path(root)), pattern.toUpperCase());
    }

}
