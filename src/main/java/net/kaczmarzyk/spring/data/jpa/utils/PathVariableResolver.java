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
package net.kaczmarzyk.spring.data.jpa.utils;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.util.Map;

import static java.util.Collections.emptyMap;

/**
 * @author Tomasz Kaczmarzyk
 * @author Jakub Radlica
 */
public class PathVariableResolver {

	public static Map<String, String> resolvePathVariables(String pathPattern, String actualPath) {
		PathMatcher pathMatcher = new AntPathMatcher();
		
		if (pathMatcher.match(pathPattern, actualPath)) {
			return pathMatcher.extractUriTemplateVariables(pathPattern, actualPath);
		} else {
			return emptyMap();
		}
	}
}
