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

import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.Arrays;
import java.util.Objects;

/**
 * <p>Filters using `true` or `false` for a boolean type field, depending on the value of the parameter passed in.
 * This can be also achieved with `Equal` specification. `True` is a convenience-class to emphasize boolean attributes.</p>
 *
 * <p>Requires boolean parameter.</p>
 *
 * @author Hubert Gotfryd (Tratif sp. z o.o.)
 */
public class True<T> extends PathSpecification<T> {

    private static final long serialVersionUID = 1L;

    protected String expectedValue;
    private Converter converter;

    public True(QueryContext queryContext, String path, String[] httpParamValues, Converter converter) {
        super(queryContext, path);
        if (httpParamValues == null || httpParamValues.length != 1) {
            throw new IllegalArgumentException("Invalid size of 'httpParamValues' array, Expected 1 but was " + Arrays.toString(httpParamValues));
        }
        this.expectedValue = httpParamValues[0];
        this.converter = converter;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        if (converter.convert(expectedValue, Boolean.class)) {
            return criteriaBuilder.isTrue(path(root));
        } else {
            return criteriaBuilder.isFalse(path(root));
        }
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
        True<?> aTrue = (True<?>) o;
        return Objects.equals(expectedValue, aTrue.expectedValue) &&
                Objects.equals(converter, aTrue.converter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), expectedValue, converter);
    }

    @Override
    public String toString() {
        return "True[" +
                "expectedValue='" + expectedValue + '\'' +
                ", converter=" + converter +
                ", path='" + path + '\'' +
                ']';
    }
}
