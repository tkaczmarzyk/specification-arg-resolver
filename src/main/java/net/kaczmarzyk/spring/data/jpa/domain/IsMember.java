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

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Objects;

/**
 * <p>Checks if the value passed as HTTP parameter is a member of a collection attribute of an entity (defined under `path` in `@Spec` annotation).</p>
 *
 * @author Hubert Gotfryd (Tratif sp. z o.o.)
 */
public class IsMember<T> extends PathSpecification<T> {

    private String expectedMember;
    private Converter converter;

    public IsMember(QueryContext queryContext, String path, String[] httpParamValues, Converter converter) {
        super(queryContext, path);
        if (httpParamValues == null || httpParamValues.length != 1) {
            throw new IllegalArgumentException("Invalid size of 'httpParamValues' array, Expected 1 but was " + Arrays.toString(httpParamValues));
        }
        this.expectedMember = httpParamValues[0];
        this.converter = converter;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        Class<?> typeOnPath;
        try {
            String typeOnPathClassName = ((ParameterizedType) path(root).getParentPath().getJavaType().getDeclaredField(path).getGenericType()).getActualTypeArguments()[0].getTypeName();
            typeOnPath = Class.forName(typeOnPathClassName);
        } catch (NoSuchFieldException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        Object convertedExpectedMember = converter.convert(expectedMember, typeOnPath);
        return criteriaBuilder.isMember(convertedExpectedMember, path(root));
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
        IsMember<?> isMember = (IsMember<?>) o;
        return Objects.equals(expectedMember, isMember.expectedMember) &&
                Objects.equals(converter, isMember.converter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), expectedMember, converter);
    }

    @Override
    public String toString() {
        return "IsMember[" +
                "expectedMember='" + expectedMember + '\'' +
                ", converter=" + converter +
                ", path='" + path + '\'' +
                ']';
    }
}
