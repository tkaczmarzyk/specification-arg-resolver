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
package net.kaczmarzyk.spring.data.jpa.web.utils;

import net.kaczmarzyk.spring.data.jpa.web.MockWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Util for setting path variables in request's attributes
 * It's used in tests that should work with main request path variable resolving approach in {@link net.kaczmarzyk.spring.data.jpa.web.WebRequestProcessingContext WebRequestProcessingContext}
 * If path variable attributes aren't set then old path resolving approach is used
 */
public class RequestAttributesWithPathVariablesUtil {

	public static void setPathVariablesInRequestAttributes(MockWebRequest req, Map<String, String> pathVariables) {
		req.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, pathVariables, RequestAttributes.SCOPE_REQUEST);
	}

	public static Map.Entry<String, String> entry(String key, String value) {
		return new AbstractMap.SimpleEntry<>(key, value);
	}

	public static Map<String, String> pathVariables(Map.Entry<String, String> ... entries) {
		Map<String, String> pathVariables = new HashMap<>();

		for (Map.Entry<String, String> entry : entries) {
			pathVariables.put(entry.getKey(), entry.getValue());
		}
		return pathVariables;
	}
}
