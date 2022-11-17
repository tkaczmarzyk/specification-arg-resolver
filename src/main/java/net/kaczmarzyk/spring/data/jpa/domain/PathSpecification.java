/**
 * Copyright 2014-2022 the original author or authors.
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

import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;


/**
 * @author Tomasz Kaczmarzyk
 */
public abstract class PathSpecification<T> implements Specification<T> {
    
	private static final long serialVersionUID = 1L;
	
	protected String path;
    private QueryContext queryContext;
    

    public PathSpecification(QueryContext queryContext, String path) {
    	this.queryContext = queryContext;
        this.path = path;
    }

    @SuppressWarnings("unchecked")
    protected <F> Path<F> path(Root<T> root) {
        Path<?> expr = null;
        for (String field : path.split("\\.")) {
            if (expr == null) {
            	if (queryContext != null && getEvaluatedPath(field, root) != null) {
            		expr = (Path<T>) getEvaluatedPath(field, root);
            	} else {
            		expr = root.get(field);
            	}
            } else {
                expr = expr.get(field);
            }
        }
        return (Path<F>) expr;
    }

	private Path<T> getEvaluatedPath(String field, Root<T> root) {
		Path<T> evaluated = (Path<T>) queryContext.getEvaluated(field, root);

		/**
		 * When evaluated variable has null value, it means that 'field' variable contain value of some join alias.
		 * In such situation, the evaluated path should be fetched from "evaluated join fetch context".
		 *
		 * Example spec:
		 *     @JoinFetch(paths = "orders", alias = "o")
		 *     @Spec(path = "o.itemName", params = "itemName", spec = Equal.class)
		 *     interface CustomersWithOrderedItemSpecification { }
		 *
		 * When @Spec(path="o.itemName") will be processed, there will be no evaluated path under path "o" in evaluated context,
		 * it will be in the evaluated "join fetch context".
 		 */

		if(evaluated == null) {
			return (Path<T>) queryContext.getEvaluatedJoinFetch(field);
		}

		return evaluated;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + ((queryContext == null) ? 0 : queryContext.hashCode());
		return result;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PathSpecification other = (PathSpecification) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		if (queryContext == null) {
			if (other.queryContext != null)
				return false;
		} else if (!queryContext.equals(other.queryContext))
			return false;
		return true;
	}

}
