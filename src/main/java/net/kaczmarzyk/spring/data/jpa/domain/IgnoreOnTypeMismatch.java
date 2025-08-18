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
import net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch;
import org.springframework.data.jpa.domain.Specification;

/**
 * <p>Wrapper that turns a {@code Specification} into a one that ignores wrapped specification containing mismatched parameter
 * (except {@code spec = In.class} - in this specification only mismatched parameter values are ignored).
 *  Type mismatch - e.g. when type on path is {@code Long} and the value
 *  from the HTTP parameter is not a numeric.</p>
 *
 * <p>A spec will be wrapped with this decorator if {@code onTypeMismatch} property of {@code @Spec}
 * is explicitly set to {@code OnTypeMismatch.IGNORE}, i.e.: {@code @Spec(path="id", onTypeMismatch=IGNORE)}.
 * </p>
 *
 * @see OnTypeMismatch
 *
 */
public class IgnoreOnTypeMismatch<T> extends EmptyResultOnTypeMismatch<T> {

    private static final long serialVersionUID = 1L;

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

    @Override
    public String toString() {
        return "IgnoreOnTypeMismatch [wrappedSpec=" + getWrappedSpec() + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getWrappedSpec() == null) ? 0 : getWrappedSpec().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
        	return true;
        }
        if (obj == null) {
        	return false;
        }
        if (getClass() != obj.getClass()) {
        	return false;
        }
        IgnoreOnTypeMismatch other = (IgnoreOnTypeMismatch) obj;
        if (getWrappedSpec() == null) {
            if (other.getWrappedSpec() != null) {
            	return false;
            }
        } else if (!getWrappedSpec().equals(other.getWrappedSpec())) {
        	return false;
        }
        return true;
    }
}
