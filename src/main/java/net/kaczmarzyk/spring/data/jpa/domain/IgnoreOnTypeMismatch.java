/**
 * Copyright 2014-2017 the original author or authors.
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

import net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * <p>Wrapper that turns a {@code Specification} into a one that always produces in an empty result
 * (i.e. {@code where 0 = 1}) in case of a type mismatch (e.g. when type on path is {@code Long}
 *  and the value from the HTTP parameter is not a numeric.</p>
 *
 * <p> It's useful for polymorphic "OR" queries such as {@code where id = ? or name = ?}.
 * A spec will be wrapped with this decorator if {@code onTypeMismatch} property of {@code @Spec}
 * is explicitly set to {@code OnTypeMismatch.EMPTY_RESULT}, i.e.: {@code @Spec(path="id", onTypeMismatch=EMPTY_RESULT)}.
 * </p>
 *
 * @see OnTypeMismatch
 *
 * @author Tomasz Kaczmarzyk
 */
public class IgnoreOnTypeMismatch<T> extends EmptyResultOnTypeMismatch<T> {

    public IgnoreOnTypeMismatch(Specification<T> wrappedSpec) {
        super(wrappedSpec);
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        try {
            return getWrappedSpec().toPredicate(root, query, cb);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
