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
 * Helper for easier joining lists of specs with {@code OR} operator
 * 
 * @author Tomasz Kaczmarzyk
 */
public class Disjunction<T> implements Specification<T> {

    public Specifications<T> combinedSpecs;
    
    
    @SafeVarargs
    public Disjunction(Specification<T>... specs) {
        this(Arrays.asList(specs));
    }
    
    public Disjunction(Collection<Specification<T>> innerSpecs) {
        for (Specification<T> spec : innerSpecs) {
            if (combinedSpecs == null) {
                combinedSpecs = Specifications.where(spec);
            } else {
                combinedSpecs = combinedSpecs.or(spec);
            }
        }
    }
    
    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return combinedSpecs.toPredicate(root, query, cb);
    }

}
