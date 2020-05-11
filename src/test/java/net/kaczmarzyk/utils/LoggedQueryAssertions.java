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

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.assertj.core.api.Assertions;

/**
 * @see TestLogAppender
 * @see LoggedQuery
 * 
 * @author Tomasz Kaczmarzyk
 */
public class LoggedQueryAssertions {

	private LoggedQuery assertedQuery;

	public LoggedQueryAssertions hasNumberOfJoinsForPath(String joinPath, int expectedCount) {
		assertQueryContextExists();
		Assertions.assertThat(assertedQuery.countJoinsForPath(joinPath)).isEqualTo(expectedCount);
		return this;
	}
	
	public LoggedQueryAssertions hasNumberOfJoins(int expectedCount) {
		assertQueryContextExists();
		Assertions.assertThat(assertedQuery.countJoins()).isEqualTo(expectedCount);
		return this;
	}

	private void assertQueryContextExists() throws AssertionError {
		if (assertedQuery == null) {
			throw new AssertionError("this method must be executed in context of some query, did you use `queryWithIndex` method?");
		}
	}
	
	public LoggedQueryAssertions andQueryWithIndex(int index) {
		this.assertedQuery = loggedQueries().get(index);
		return this;
	}
	
	public LoggedQueryAssertions numberOfPerformedHqlQueriesIs(int expectedCount) {
		Assertions.assertThat(loggedQueries()).hasSize(expectedCount);
		return this;
	}
	
	private List<LoggedQuery> loggedQueries() {
		return TestLogAppender.getInterceptedLogs().stream()
			.filter(log -> log.contains("parse() - HQL: "))
			.map(LoggedQuery::new)
			.collect(toList());
	}
	
	public static LoggedQueryAssertions assertThat() {
		return new LoggedQueryAssertions();
	}
}
