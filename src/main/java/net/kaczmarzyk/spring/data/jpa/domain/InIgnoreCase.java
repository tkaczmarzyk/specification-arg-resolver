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

import jakarta.persistence.criteria.*;
import net.kaczmarzyk.spring.data.jpa.utils.CaseConversionHelper;
import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;


/**
 * <p>Filters with "in" where-clause (e.g. {@code where firstName in ("homer", "marge")}).</p>
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
public class InIgnoreCase<T> extends PathSpecification<T> implements IgnoreCaseStrategyAware, LocaleAware {

    private static final long serialVersionUID = 1L;

    protected String[] allowedValues;
    protected Converter converter;
    protected IgnoreCaseStrategy ignoreCaseStrategy;
    protected Locale locale;

    public InIgnoreCase(QueryContext queryContext, String path, String[] httpParamValues, Converter converter) {
        super(queryContext, path);
        if (httpParamValues == null || httpParamValues.length < 1) {
            throw new IllegalArgumentException("Invalid size of 'httpParamValues' array, Expected at least 1 but was " + Arrays.toString(httpParamValues));
        }
        this.allowedValues = httpParamValues;
        this.converter = converter;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Path<?> path = path(root);
        Class<?> typeOnPath = path.getJavaType();

        if (typeOnPath.equals(String.class)) {
            var converted = CaseConversionHelper.applyCaseConversion(
                    cb,
                    this.<String>path(root),
                    allowedValues,
                    ignoreCaseStrategy,
                    locale
            );
            var inExpression = cb.in(converted.column());
            converted.values().forEach(inExpression::value);
            return inExpression;
        }
        return path.in(converter.convert(Arrays.asList(allowedValues), typeOnPath, true));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(Arrays.hashCode(allowedValues), converter, ignoreCaseStrategy, locale);
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
        InIgnoreCase<?> other = (InIgnoreCase<?>) o;
        return Arrays.equals(allowedValues, other.allowedValues) &&
                Objects.equals(converter, other.converter) &&
                Objects.equals(ignoreCaseStrategy, other.ignoreCaseStrategy) &&
                Objects.equals(locale, other.locale);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" +
                "allowedValues=" + Arrays.toString(allowedValues) +
                ", converter=" + converter +
                ", path='" + path + '\'' +
                ", ignoreCaseStrategy=" + ignoreCaseStrategy +
                ", locale=" + locale +
                ']';
    }

    @Override
    public void setIgnoreCaseStrategy(IgnoreCaseStrategy ignoreCaseStrategy) {
        this.ignoreCaseStrategy = ignoreCaseStrategy;
    }

    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
    }
}
