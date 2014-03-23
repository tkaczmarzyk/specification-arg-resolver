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

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;


/**
 * @author Tomasz Kaczmarzyk
 */
public abstract class PathSpecification<T> implements Specification<T> {
    private String path;

    public PathSpecification(String path) {
        this.path = path;
    }
    
    @SuppressWarnings("unchecked")
    protected <F> Path<F> path(Root<T> root) {
        Path<?> expr = null;
        for (String field : path.split("\\.")) {
            if (expr == null) {
                expr = root.get(field);
            } else {
                expr = expr.get(field);
            }
        }
        return (Path<F>) expr;
    }
}
