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

import java.util.Arrays;
import java.util.Objects;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;

/**
 * Filters with {@code path like %pattern%} where-clause.
 * 
 * @author Tomasz Kaczmarzyk
 */
public class Like<T> extends PathSpecification<T> implements WithoutTypeConversion {

	private static final long serialVersionUID = 1L;
	
	protected String pattern;

    public Like(QueryContext queryContext, String path, String... args) {
        super(queryContext, path);
        if (args == null || args.length != 1) {
            throw new IllegalArgumentException("Expected exactly one argument (the fragment to match against), but got: " + Arrays.toString(args));
        } else {
            this.pattern = "%" + args[0] + "%";
        }
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        return builder.like(this.<String>path(root), pattern);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((pattern == null) ? 0 : pattern.hashCode());
        return result;
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
        Like<?> like = (Like<?>) o;
        return Objects.equals(pattern, like.pattern);
    }

    @Override
    public String toString() {
        return "Like[" +
                "pattern='" + pattern + '\'' +
                ", path='" + path + '\'' +
                ']';
    }
}