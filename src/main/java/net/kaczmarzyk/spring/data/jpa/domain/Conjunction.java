/**
 * Copyright 2014 the original author or authors.
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

import java.util.Arrays;
import java.util.Collection;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;

/**
 * Helper for easier joining lists of specs with {@code AND} operator
 * 
 * @author Tomasz Kaczmarzyk
 */
public class Conjunction<T> implements Specification<T> {

    private Collection<Specification<T>> innerSpecs;

    
    @SafeVarargs
    public Conjunction(Specification<T>... innerSpecs) {
        this(Arrays.asList(innerSpecs));
    }
    
    public Conjunction(Collection<Specification<T>> innerSpecs) {
        this.innerSpecs = innerSpecs;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Specifications<T> combinedSpecs = null;
        for (Specification<T> spec : innerSpecs) {
            if (combinedSpecs == null) {
                combinedSpecs = Specifications.where(spec);
            } else {
                combinedSpecs = combinedSpecs.and(spec);
            }
        }
        return combinedSpecs.toPredicate(root, query, cb);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((innerSpecs == null) ? 0 : innerSpecs.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Conjunction<?> other = (Conjunction<?>) obj;
        if (innerSpecs == null) {
            if (other.innerSpecs != null)
                return false;
        } else if (!innerSpecs.equals(other.innerSpecs))
            return false;
        return true;
    }

}