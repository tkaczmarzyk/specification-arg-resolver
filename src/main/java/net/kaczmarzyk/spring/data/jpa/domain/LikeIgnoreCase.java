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

import jakarta.persistence.criteria.*;
import net.kaczmarzyk.spring.data.jpa.utils.CaseConversionHelper;
import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;

import java.util.Locale;

/**
 * Filters with {@code path like %pattern%} where-clause and ignores pattern case
 * 
 * @author Michal Jankowski, Hazecod
 * @author Tomasz Kaczmarzyk
 *
 */
public class LikeIgnoreCase<T> extends Like<T> implements IgnoreCaseStrategyAware, LocaleAware {

	private static final long serialVersionUID = 1L;
	private IgnoreCaseStrategy ignoreCaseStrategy;
	private Locale locale;

	public LikeIgnoreCase(QueryContext queryCtx, String path, String... args) {
        super(queryCtx, path, args);
    }
	
    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        var converted = CaseConversionHelper.applyCaseConversion(
            builder, 
            this.<String>path(root), 
            pattern, 
            ignoreCaseStrategy,
            locale
        );
        return builder.like(converted.column(), converted.value());
    }
    
    @Override
    public void setIgnoreCaseStrategy(IgnoreCaseStrategy ignoreCaseStrategy) {
        this.ignoreCaseStrategy = ignoreCaseStrategy;
    }
    
    @Override
    @Deprecated
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((ignoreCaseStrategy == null) ? 0 : ignoreCaseStrategy.hashCode());
		result = prime * result + ((locale == null) ? 0 : locale.hashCode());
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
		LikeIgnoreCase<?> other = (LikeIgnoreCase<?>) obj;
		if (ignoreCaseStrategy != other.ignoreCaseStrategy) {
			return false;
		}
		if (locale == null) {
			if (other.locale != null) {
				return false;
			}
		} else if (!locale.equals(other.locale)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "LikeIgnoreCase [pattern=" + pattern + ", path=" + path + ", ignoreCaseStrategy=" + ignoreCaseStrategy + ", locale=" + locale + "]";
	}
}
