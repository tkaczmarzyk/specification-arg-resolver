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

import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * <p>Filers with "is not null" where clause (e.g. {@code where nickName is not null}).</p>
 *
 * <p>Does not require any http-parameters to be present, i.e. represents constant part of the query.</p>
 *
 * @author Hubert Gotfryd (Tratif sp. z o.o.)
 */
public class IsNotNull<T> extends PathSpecification<T> implements ZeroArgSpecification {

    private static final long serialVersionUID = 1L;

    public IsNotNull(QueryContext queryContext, String path, String[] args) {
        super(queryContext, path);
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        return criteriaBuilder.isNotNull(path(root));
    }

    @Override
    public String toString() {
        return "IsNotNull[" +
                "path='" + path + '\'' +
                ']';
    }
}
