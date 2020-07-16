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

import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Objects;

/**
 * <p>Filters with equal where-clause (e.g. {@code where firstName = "Homer"}).</p>
 *
 * <p>Supports multiple field types: strings, numbers, booleans, enums, dates.</p>
 *
 * <p>If the field type is string or enum, the where-clause is case insensitive</p>
 *
 * @author Ricardo Pardinho
 */
public class EqualIgnoreCase<T> extends PathSpecification<T> {

    private static final long serialVersionUID = 2L;

    protected String expectedValue;
    private Converter converter;

    public EqualIgnoreCase(QueryContext queryContext, String path, String[] httpParamValues, Converter converter) {
        super(queryContext, path);
        if (httpParamValues == null || httpParamValues.length != 1) {
            throw new IllegalArgumentException();
        }
        this.expectedValue = httpParamValues[0];
        this.converter = converter;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

        if (path(root).getJavaType().equals(String.class)) {
            return cb.equal(cb.upper(this.<String>path(root)), expectedValue.toUpperCase());
        }

        Class<?> typeOnPath = path(root).getJavaType();
        return cb.equal(path(root), converter.convert(expectedValue, typeOnPath, true));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        EqualIgnoreCase<?> that = (EqualIgnoreCase<?>) o;
        return Objects.equals(expectedValue, that.expectedValue) &&
                Objects.equals(converter, that.converter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), expectedValue, converter);
    }

    @Override
    public String toString() {
        return "EqualIgnoreCase [expectedValue=" + expectedValue + ", converter=" + converter + ", path=" + super.path + "]";
    }
}
