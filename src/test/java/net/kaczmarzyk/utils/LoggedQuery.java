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
package net.kaczmarzyk.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a logged HQL query
 * 
 * @author Tomasz Kaczmarzyk
 */
public class LoggedQuery {

	private String logStatementWithQuery;
	
	public LoggedQuery(String logStatementWithQuery) {
		this.logStatementWithQuery = logStatementWithQuery;
	}

	public int countJoinsForPath(String joinPath) {
		return countOccurences("join \\w+" + joinPath);
	}

	public int countJoins() {
		return countOccurences("join");
	}
	
	private int countOccurences(String pattern) {
		Pattern joinPattern = Pattern.compile(pattern);
		Matcher matcher = joinPattern.matcher(logStatementWithQuery);
		int count = 0;
		while (matcher.find()) {
		    count++;
		}
		return count;
	}
}
