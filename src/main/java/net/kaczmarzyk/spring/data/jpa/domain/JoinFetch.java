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
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static net.kaczmarzyk.spring.data.jpa.utils.JoinPathUtils.pathToJoinContainsAlias;
import static net.kaczmarzyk.spring.data.jpa.utils.JoinPathUtils.pathToJoinSplittedByDot;


/**
 * <p>Extension to specification-arg-resolver to allow fetching collections in a specification query</p>
 *
 * @author Tomasz Kaczmarzyk
 * @author Gerald Humphries
 * @author Jakub Radlica
 */
public class JoinFetch<T> implements Specification<T> {

	private static final long serialVersionUID = 1L;

	private QueryContext context;

	private List<String> pathsToFetch;
	private String alias;
	private JoinType joinType;
	private boolean distinct;


	public JoinFetch(QueryContext queryContext, String[] pathsToFetch, JoinType joinType, boolean distinct) {
		this(queryContext, pathsToFetch, "", joinType, distinct);
	}

	public JoinFetch(QueryContext queryContext, String[] pathsToFetch, String alias, JoinType joinType, boolean distinct) {
		this.context = queryContext;
		this.pathsToFetch = Arrays.asList(pathsToFetch);
		this.alias = alias;
		this.joinType = joinType;
		this.distinct = distinct;

		if (!alias.isEmpty() && pathsToFetch.length != 1) {
			throw new IllegalArgumentException(
					"Join fetch alias can be defined only for join fetch with a single path! " +
					"Remove alias from the annotation or repeat @JoinFetch annotation for every path and use unique alias for each join."
			);
		}
	}

	@Override
	public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
		query.distinct(distinct);
		if (!Number.class.isAssignableFrom(query.getResultType())) { // do not join in count queries
			if (pathsToFetch.size() == 1) {
				String pathToFetch = pathsToFetch.get(0);
				if (pathToJoinContainsAlias(pathToFetch)) {
					String[] pathToJoinFetchOnSplittedByDot = pathToJoinSplittedByDot(pathToFetch);
					String alias = pathToJoinFetchOnSplittedByDot[0];
					String path = pathToJoinFetchOnSplittedByDot[1];

					Fetch<?, ?> evaluatedJoinFetchForGivenAlias = context.getEvaluatedJoinFetch(alias);

					if(evaluatedJoinFetchForGivenAlias == null) {
						throw new IllegalArgumentException(
								"Join fetch definition with alias: '" + alias + "' not found! " +
										"Make sure that join with the alias '" + alias +"' is defined before the join with path: '" + pathToFetch + "'"
						);
					}

					Fetch<?,?> joinFetch = evaluatedJoinFetchForGivenAlias.fetch(path, joinType);
					if (StringUtils.isNotBlank(this.alias)) {
						context.putEvaluatedJoinFetch(this.alias, joinFetch);
					}
				} else {
					Fetch<Object, Object> evaluated = root.fetch(pathToFetch, joinType);
					context.putEvaluatedJoinFetch(alias, evaluated);
				}
			} else {
				for (String path : pathsToFetch) {
					root.fetch(path, joinType);
				}
			}
		}
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		JoinFetch<?> joinFetch = (JoinFetch<?>) o;
		return distinct == joinFetch.distinct &&
				Objects.equals(context, joinFetch.context) &&
				Objects.equals(pathsToFetch, joinFetch.pathsToFetch) &&
				Objects.equals(alias, joinFetch.alias) &&
				joinType == joinFetch.joinType;
	}

	@Override
	public int hashCode() {
		return Objects.hash(context, pathsToFetch, alias, joinType, distinct);
	}

	@Override
	public String toString() {
		return "JoinFetch[" +
				"pathsToFetch=" + pathsToFetch +
				", joinType=" + joinType +
				']';
	}
}
