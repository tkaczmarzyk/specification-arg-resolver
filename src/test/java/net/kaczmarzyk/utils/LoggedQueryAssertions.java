/**
 * Copyright 2014-2023 the original author or authors.
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

import jakarta.persistence.criteria.JoinType;
import org.assertj.core.api.Assertions;

/**
 * @see TestLogAppender
 * @see LoggedQuery
 * 
 * @author Tomasz Kaczmarzyk
 */
public class LoggedQueryAssertions<T> {

	private T parentAssertions;
	private LoggedQuery assertedQuery;

	private LoggedQueryAssertions(LoggedQuery assertedQuery) {
		this.assertedQuery = assertedQuery;
	}

	LoggedQueryAssertions(T parent, LoggedQuery assertedQuery) {
		this.parentAssertions = parent;
		this.assertedQuery = assertedQuery;
	}

	public static LoggedQueryAssertions assertThat(LoggedQuery loggedQuery) {
		return new LoggedQueryAssertions(loggedQuery);
	}

	public T and() {
		return parentAssertions;
	}

	public LoggedQueryAssertions hasNumberOfTableJoins(String table, int expectedCount) {
		assertQueryContextExists();
		Assertions.assertThat(assertedQuery.countTableJoins(table)).isEqualTo(expectedCount);
		return this;
	}

	public LoggedQueryAssertions hasNumberOfJoins(int expectedCount) {
		assertQueryContextExists();
		Assertions.assertThat(assertedQuery.countJoins()).isEqualTo(expectedCount);
		return this;
	}

	public LoggedQueryAssertions hasNumberOfJoins(int expectedCount, JoinType joinType) {
		assertQueryContextExists();
		Assertions.assertThat(assertedQuery.countJoins(joinType)).isEqualTo(expectedCount);
		return this;
	}
	private void assertQueryContextExists() throws AssertionError {
		if (assertedQuery == null) {
			throw new AssertionError("this method must be executed in context of some query, did you use `queryWithIndex` method?");
		}
	}
}
