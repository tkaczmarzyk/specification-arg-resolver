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

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;

import java.util.Arrays;
import java.util.Objects;

/**
 * <p>Checks if the value passed as HTTP parameter is not a member of a collection attribute of an entity (defined under `path` in `@Spec` annotation).</p>
 *
 * @author Hubert Gotfryd (Tratif sp. z o.o.)
 */
public class IsNotMember<T> extends PathSpecification<T> {

    private String unwantedMember;
    private Converter converter;

    public IsNotMember(QueryContext queryContext, String path, String[] httpParamValues, Converter converter) {
        super(queryContext, path);
        if (httpParamValues == null || httpParamValues.length != 1) {
            throw new IllegalArgumentException("Invalid size of 'httpParamValues' array, Expected 1 but was " + Arrays.toString(httpParamValues));
        }
        this.unwantedMember = httpParamValues[0];
        this.converter = converter;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        Class<?> typeOnPath = path(root).getJavaType();
        Object convertedUnwantedMember = converter.convert(unwantedMember, typeOnPath);
        return criteriaBuilder.isNotMember(convertedUnwantedMember, path(root));
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
        IsNotMember<?> isNotMember = (IsNotMember<?>) o;
        return Objects.equals(unwantedMember, isNotMember.unwantedMember) &&
                Objects.equals(converter, isNotMember.converter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), unwantedMember, converter);
    }

    @Override
    public String toString() {
        return "IsNotMember[" +
                "unwantedMember='" + unwantedMember + '\'' +
                ", converter=" + converter +
                ", path='" + path + '\'' +
                ']';
    }
}
