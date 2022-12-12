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
package net.kaczmarzyk.spring.data.jpa.web;

import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;
import org.apache.commons.lang3.tuple.Pair;

import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author Tomasz Kaczmarzyk
 */
public class DefaultQueryContext implements QueryContext {

	private HashMap<String, Function<Root<?>, Join<?, ?>>> contextMap;
	private HashMap<String, Fetch<?, ?>> evaluatedJoinFetch;

	private Map<Pair<String, Root>, javax.persistence.criteria.Join<?, ?>> rootCache;

	public DefaultQueryContext() {
		this.contextMap = new HashMap<>();
		this.evaluatedJoinFetch = new HashMap<>();
		this.rootCache = new HashMap<>();
	}

	@Override
	public boolean existsJoin(String key, Root<?> root) {
		return contextMap.containsKey(key);
	}

	@Override
	public Join<?, ?> getEvaluated(String key, Root<?> root) {
		Function<Root<?>, Join<?, ?>> value = contextMap.get(key);

		if (value == null) {
			return null;
		}

		Pair<String, Root> rootKey = Pair.of(key, root);

		if (!rootCache.containsKey(rootKey)) {
			Join<?, ?> evaluated = value.apply(root);
			rootCache.put(rootKey, evaluated);
		}
		return rootCache.get(rootKey);
	}

	@Override
	public void putLazyVal(String key, Function<Root<?>, Join<?, ?>> value) {
		contextMap.put(key, value);
	}

	@Override
	public Fetch<?, ?> getEvaluatedJoinFetch(String key) {
		return this.evaluatedJoinFetch.get(key);
	}

	@Override
	public void putEvaluatedJoinFetch(String key, Fetch<?, ?> fetch) {
		this.evaluatedJoinFetch.put(key, fetch);
	}

	@Override
	public int hashCode() {
		int result = contextMap.hashCode();
		result = 31 * result + evaluatedJoinFetch.hashCode();
		result = 31 * result + rootCache.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DefaultQueryContext that = (DefaultQueryContext) o;
		return Objects.equals(contextMap, that.contextMap) &&
				Objects.equals(evaluatedJoinFetch, that.evaluatedJoinFetch) &&
				Objects.equals(rootCache, that.rootCache);
	}

	@Override
	public String toString() {
		return "DefaultQueryContext[" +
				"contextMap=" + contextMap +
				']';
	}
}
