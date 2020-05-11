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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Tomasz Kaczmarzyk
 */
public class PathVariableResolver {

	private String pathPattern;
	private String actualPath;
	
	private PathVariableResolver(String pathPattern, String actualPath) {
		this.pathPattern = pathPattern;
		this.actualPath = actualPath;
	}

	public String resolveValue(String pathVariableName) {
		String variablePlaceholder = "{" + pathVariableName + "}";
		if (pathPattern.contains(variablePlaceholder)) {
			String regex = pathPattern.replace(variablePlaceholder, "(.+)");
			regex = regex.replaceAll("\\{.+\\}", ".+");
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(actualPath);
			if (matcher.matches()) {
				return matcher.group(1);
			}
		}
		return null;
	}

	public static PathVariableResolver forPathPatternAndActualPath(String pathPattern, String actualPath) {
		return new PathVariableResolver(pathPattern, actualPath);
	}
}
