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
package net.kaczmarzyk.spring.data.jpa.web;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.context.request.NativeWebRequest;

import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;

import javax.persistence.criteria.Root;

/**
 * @author Tomasz Kaczmarzyk
 */
public class WebRequestQueryContext implements QueryContext {

	private static final String ATTRIBUTE_KEY = WebRequestQueryContext.class.getName() + ".ATTRIBUTE_KEY";
	
	private HashMap<String, Object> contextMap;
	private Map<Pair<String, Root>, Object> rootCache = new HashMap<>();

	public WebRequestQueryContext(NativeWebRequest request) {
		this.contextMap = (HashMap<String, Object>) request.getAttribute(ATTRIBUTE_KEY, NativeWebRequest.SCOPE_REQUEST);
		if (this.contextMap == null) {
			this.contextMap = new HashMap<>();
			request.setAttribute(ATTRIBUTE_KEY, contextMap, NativeWebRequest.SCOPE_REQUEST);
		}
	}
	
	@Override
	public Object getEvaluated(String key, Root<?> root) {
		Object value = contextMap.get(key);
		if (value instanceof Function) {
			Pair<String, Root> rootKey = Pair.of(key, root);

			if (!rootCache.containsKey(rootKey)) {
				Object evaluated = ((Function) value).apply(root);
				rootCache.put(rootKey, evaluated);
			}
			return rootCache.get(rootKey);
		} else {
			return value;
		}
	}

	@Override
	public void putLazyVal(String key, Function<Root<?>, Object> value) {
		contextMap.put(key, value);
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
		WebRequestQueryContext other = (WebRequestQueryContext) obj;
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
