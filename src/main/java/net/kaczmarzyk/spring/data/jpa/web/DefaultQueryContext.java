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
import java.util.Optional;
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
		final int prime = 31;
		int result = 1;
		result = prime * result + ((contextMap == null) ? 0 : contextMap.hashCode());
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
		DefaultQueryContext other = (DefaultQueryContext) obj;
		if (contextMap == null) {
			if (other.contextMap != null)
				return false;
		} else if (!contextMap.equals(other.contextMap))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "WebRequestQueryContext [contextMap=" + contextMap + "]";
	}
}
