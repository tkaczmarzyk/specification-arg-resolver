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
import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;

import java.util.Arrays;
import java.util.Objects;


/**
 * <p>Negation of {@link net.kaczmarzyk.spring.data.jpa.domain.InIgnoreCase InIgnoreCase}.</p>
 *
 * <p>Filters with "not in" where-clause (e.g. {@code where firstName in ("homer", "marge")}).</p>
 *
 * <p>Values to match against should be provided as multiple values of a single HTTP parameter, e.g.:
 * {@code GET http://myhost/customers?firstName=homer&firstName=marge}.</p>
 *
 * <p>Supports multiple field types: strings, numbers, booleans, enums, dates.</p>
 *
 * <p>If the field type is string or enum, the where-clause is case-insensitive</p>
 *
 * @author cschierle
 */
public class NotInIgnoreCase<T> extends InIgnoreCase<T> implements IgnoreCaseStrategyAware, LocaleAware {

    private static final long serialVersionUID = 1L;

    public NotInIgnoreCase(QueryContext queryContext, String path, String[] httpParamValues, Converter converter) {
        super(queryContext, path, httpParamValues, converter);
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        var inIgnoreCasePredicate = super.toPredicate(root, query, cb);
        if (inIgnoreCasePredicate != null) {
            return inIgnoreCasePredicate.not();
        }
        return null;
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
        NotInIgnoreCase<?> other = (NotInIgnoreCase<?>) o;
        return Arrays.equals(allowedValues, other.allowedValues) &&
                Objects.equals(converter, other.converter) &&
                Objects.equals(ignoreCaseStrategy, other.ignoreCaseStrategy) &&
                Objects.equals(locale, other.locale);
    }
}
