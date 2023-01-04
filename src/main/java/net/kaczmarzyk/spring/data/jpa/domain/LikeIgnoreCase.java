/**
 * Copyright 2014-2023 the original author or authors.
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

import java.util.Locale;
import java.util.Objects;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;

/**
 * Filters with {@code path like %pattern%} where-clause and ignores pattern case
 * 
 * @author Michal Jankowski, Hazecod
 * @author Tomasz Kaczmarzyk
 *
 */
public class LikeIgnoreCase<T> extends Like<T> implements LocaleAware {

	private static final long serialVersionUID = 1L;

	private Locale locale;

	public LikeIgnoreCase(QueryContext queryCtx, String path, String... args) {
        super(queryCtx, path, args);
    }
	
    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        return builder.like(builder.upper(this.<String> path(root)), pattern.toUpperCase(locale));
    }

	@Override
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(locale);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		LikeIgnoreCase other = (LikeIgnoreCase) obj;
		return Objects.equals(locale, other.locale);
	}

	@Override
	public String toString() {
		return "LikeIgnoreCase [locale=" + locale + ", pattern=" + pattern + ", path=" + path + "]";
	}
}
