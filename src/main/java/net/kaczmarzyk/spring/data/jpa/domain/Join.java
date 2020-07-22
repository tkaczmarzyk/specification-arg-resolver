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

import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;

import static net.kaczmarzyk.spring.data.jpa.utils.JoinPathUtils.pathToJoinContainsAlias;
import static net.kaczmarzyk.spring.data.jpa.utils.JoinPathUtils.pathToJoinSplittedByDot;

/**
 * @author Tomasz Kaczmarzyk
 * @author Jakub Radlica
 */
public class Join<T> implements Specification<T>, Fake {

	private static final long serialVersionUID = 1L;

	private String pathToJoinOn;
	private String alias;
	private JoinType joinType;
	private QueryContext queryContext;
	private boolean distinctQuery;


	public Join(QueryContext queryContext, String pathToJoinOn, String alias, JoinType joinType, boolean distinctQuery) {
		this.pathToJoinOn = pathToJoinOn;
		this.alias = alias;
		this.joinType = joinType;
		this.queryContext = queryContext;
		this.distinctQuery = distinctQuery;
	}

	@Override
	public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
		query.distinct(distinctQuery);

		if (!pathToJoinContainsAlias(pathToJoinOn)) {
			queryContext.putLazyVal(alias, (r) -> r.join(pathToJoinOn, joinType));
		} else {
			String[] pathToJoinOnSplittedByDot = pathToJoinSplittedByDot(pathToJoinOn);

			String extractedAlias = pathToJoinOnSplittedByDot[0];
			javax.persistence.criteria.Join<?, ?> evaluated = queryContext.getEvaluated(extractedAlias, root);

			if (evaluated == null) {
				throw new IllegalArgumentException(
						"Join definition with alias: '" + extractedAlias + "' not found! " +
								"Make sure that join with the alias '" + extractedAlias +"' is defined before the join with path: '" + pathToJoinOn + "'"
				);
			}

			String extractedPathToJoin = pathToJoinOnSplittedByDot[1];

			queryContext.putLazyVal(
					alias,
					(r) -> evaluated.join(extractedPathToJoin, joinType)
			);
		}
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alias == null) ? 0 : alias.hashCode());
		result = prime * result + (distinctQuery ? 1231 : 1237);
		result = prime * result + ((joinType == null) ? 0 : joinType.hashCode());
		result = prime * result + ((pathToJoinOn == null) ? 0 : pathToJoinOn.hashCode());
		result = prime * result + ((queryContext == null) ? 0 : queryContext.hashCode());
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
		Join other = (Join) obj;
		if (alias == null) {
			if (other.alias != null)
				return false;
		} else if (!alias.equals(other.alias))
			return false;
		if (distinctQuery != other.distinctQuery)
			return false;
		if (joinType != other.joinType)
			return false;
		if (pathToJoinOn == null) {
			if (other.pathToJoinOn != null)
				return false;
		} else if (!pathToJoinOn.equals(other.pathToJoinOn))
			return false;
		if (queryContext == null) {
			if (other.queryContext != null)
				return false;
		} else if (!queryContext.equals(other.queryContext))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Join [pathToJoinOn=" + pathToJoinOn + ", alias=" + alias + ", joinType=" + joinType + ", queryContext=" + queryContext
				+ ", distinctQuery=" + distinctQuery + "]";
	}
}
