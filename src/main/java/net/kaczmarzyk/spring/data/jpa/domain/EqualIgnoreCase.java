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

import net.kaczmarzyk.spring.data.jpa.utils.CaseConversionHelper;
import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

/**
 * <p>Filters with equal where-clause (e.g. {@code where firstName = "Homer"}).</p>
 *
 * <p>Supports multiple field types: strings, numbers, booleans, enums, dates.</p>
 *
 * <p>If the field type is string or enum, the where-clause is case insensitive</p>
 *
 * @author Ricardo Pardinho
 * @author Tomasz Kaczmarzyk
 */
public class EqualIgnoreCase<T> extends PathSpecification<T> implements IgnoreCaseStrategyAware, LocaleAware {

    private static final long serialVersionUID = 2L;

    protected String expectedValue;
    private Converter converter;
    private IgnoreCaseStrategy ignoreCaseStrategy;
    private Locale locale;

    public EqualIgnoreCase(QueryContext queryContext, String path, String[] httpParamValues, Converter converter) {
        super(queryContext, path);
        if (httpParamValues == null || httpParamValues.length != 1) {
            throw new IllegalArgumentException("Invalid size of 'httpParamValues' array, Expected 1 but was " + Arrays.toString(httpParamValues));
        }
        this.expectedValue = httpParamValues[0];
        this.converter = converter;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

        if (path(root).getJavaType().equals(String.class)) {
            var converted = CaseConversionHelper.applyCaseConversion(
                cb,
                this.<String>path(root),
                expectedValue,
                ignoreCaseStrategy,
                locale
            );
            return cb.equal(converted.column(), converted.value());
        }

        Class<?> typeOnPath = path(root).getJavaType();
        return cb.equal(path(root), converter.convert(expectedValue, typeOnPath, true));
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
		result = prime * result + Objects.hash(converter, expectedValue, ignoreCaseStrategy, locale);
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
		EqualIgnoreCase other = (EqualIgnoreCase) obj;
		return Objects.equals(converter, other.converter) && Objects.equals(expectedValue, other.expectedValue) 
			&& Objects.equals(ignoreCaseStrategy, other.ignoreCaseStrategy) && Objects.equals(locale, other.locale);
	}

	@Override
    public String toString() {
        return "EqualIgnoreCase [expectedValue=" + expectedValue + ", converter=" + converter + ", path=" + super.path + ", ignoreCaseStrategy=" + ignoreCaseStrategy + ", locale=" + locale + "]";
    }
}
