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

import static net.kaczmarzyk.spring.data.jpa.utils.JoinPathUtils.pathToJoinContainsAlias;
import static net.kaczmarzyk.spring.data.jpa.utils.JoinPathUtils.pathToJoinSplittedByDot;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;


/**
 * <p>Extension to specification-arg-resolver to allow fetching collections in a specification query</p>
 *
 * @author Tomasz Kaczmarzyk
 * @author Gerald Humphries
 * @author Jakub Radlica
 */
public class JoinFetch<T> implements Specification<T>, Fake {

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
		if (!Number.class.isAssignableFrom(query.getResultType())) { // if it's not a count query, then just execute the fetch
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
		} else { // count query -- join fetch can be skipped unless it is used not only for fetching but for filtering as well
			if (!alias.isEmpty()) { // assumption: presence of a non-empty alias means that join fetch is used for filtering as well
									//  unfortunately, Hibernate disallows adding join fetches to count queries 
									//  (or more specifcally, does not allow fetching if fetch-root is not present in the query result)
									//  so we need to convert the join fetch into a regular join.
									//  In case that an alias exist, but is not used, then join won't be applied (as joins are lazily evaluated by the lib)
									//  so theoretically we could skip this if and let Join logic just work, but keeping it can hopefully reduce potential for hard-to-debug errors
				
				String pathToJoin = pathsToFetch.iterator().next(); // see the constructor, if alias is used, then pathsToFetch must have size 1
				
				Join<T> regularJoin = new Join<T>(context, pathToJoin, alias, joinType, distinct);
				
				return regularJoin.toPredicate(root, query, cb);				
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
