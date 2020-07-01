/**
 * Copyright 2014-2020 the original author or authors.
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

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.Arrays;
import java.util.List;


/**
 * <p>Extension to specification-arg-resolver to allow fetching collections in a specification query</p>
 *
 * @author Tomasz Kaczmarzyk
 * @author Gerald Humphries
 */
public class JoinFetch<T> implements Specification<T> {

	private static final long serialVersionUID = 1L;
	
	private List<String> pathsToFetch;
	private JoinType joinType;
	private boolean distinct;

    
    public JoinFetch(String[] pathsToFetch, JoinType joinType, boolean distinct) {
        this.pathsToFetch = Arrays.asList(pathsToFetch);
        this.joinType = joinType;
        this.distinct = distinct;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        query.distinct(distinct);
        if (!Number.class.isAssignableFrom(query.getResultType())) { // do not join in count queries
            for (String path : pathsToFetch){
                root.fetch(path, joinType);
            }
        }
        return null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((joinType == null) ? 0 : joinType.hashCode());
        result = prime * result + ((pathsToFetch == null) ? 0 : pathsToFetch.hashCode());
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
        @SuppressWarnings("rawtypes")
        JoinFetch other = (JoinFetch) obj;
        if (joinType != other.joinType) {
            return false;
        }
        if (pathsToFetch == null) {
            if (other.pathsToFetch != null) {
                return false;
            }
        } else if (!pathsToFetch.equals(other.pathsToFetch)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "JoinFetch[" +
                "pathsToFetch=" + pathsToFetch +
                ", joinType=" + joinType +
                ']';
    }
}
