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
package net.kaczmarzyk.utils;

import jakarta.persistence.criteria.JoinType;
import net.kaczmarzyk.utils.interceptor.HibernateStatementInspector;

import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

import static org.assertj.core.api.Assertions.assertThat;

public class InterceptedStatementsAssert {

	private final List<String> logs;

	private InterceptedStatementsAssert(List<String> logs) {
		this.logs = new ArrayList<>(logs);
	}

	public static InterceptedStatementsAssert assertThatInterceptedStatements() {
		return new InterceptedStatementsAssert(HibernateStatementInspector.getInterceptedStatements());
	}

	public InterceptedStatementsAssert hasNumberOfTableJoins(String tableName, int expectedCount) {
		int numberOfJoins = logs.stream()
				.mapToInt(statement -> new LoggedQuery(statement).countTableJoins(tableName))
				.sum();

		assertThat(numberOfJoins)
				.withFailMessage("Expected: %s table %s joins, actual: %s", tableName, expectedCount, numberOfJoins)
				.isEqualTo(expectedCount);
		return this;
	}

	public InterceptedStatementsAssert hasNumberOfTableJoins(String tableName, JoinType joinType, int expectedCount) {
		int numberOfJoins = logs.stream()
				.mapToInt(statement -> new LoggedQuery(statement).countTableJoins(tableName, joinType))
				.sum();

		assertThat(numberOfJoins)
				.withFailMessage("Expected: %s table %s joins (%s), actual: %s", tableName, expectedCount, joinType, numberOfJoins)
				.isEqualTo(expectedCount);
		return this;
	}

	public LoggedQueryAssertions<InterceptedStatementsAssert> andQueryAtIndex(Integer index) {
		return new LoggedQueryAssertions<>(this, new LoggedQuery(logs.get(index)));
	}

	public InterceptedStatementsAssert hasOnlyOneQueryThatWasExecuted() {
		return hasSelects(1);
	}
	
	public InterceptedStatementsAssert hasSelects(int expectedAmountOfSelects) {
		long selectCount = logs.stream()
				.filter(statement -> statement.contains("SELECT") || statement.contains("select"))
				.count();

		assertThat(selectCount).isEqualTo(expectedAmountOfSelects);

		return this;
	}

	public InterceptedStatementsAssert hasNumberOfJoins(int expectedNumberOfJoins) {
		long joinsCount = logs.stream()
				.map(statement -> countNumberOfSqlClauseInStatement(statement, "join"))
				.reduce((i, joinCounter) -> joinCounter += i)
				.orElse(0);

		if (joinsCount != expectedNumberOfJoins) {
			throw new AssertionError(
					"Expected number of `join` clause: " + expectedNumberOfJoins + ", actual: " + joinsCount
			);
		}

		return this;
	}

	public InterceptedStatementsAssert hasOneClause(String clause) {
		return hasClause(clause, 1);
	}

	public InterceptedStatementsAssert hasClause(String clause, int expectedNumberOfClauseOccurrences) {
		long clauseOccurrenceCount = logs.stream()
				.mapToInt(statement -> countNumberOfSqlClauseInStatement(statement, clause))
				.sum();

		if (clauseOccurrenceCount != expectedNumberOfClauseOccurrences) {
			throw new AssertionError(
					"Expected clause '" + clause + "' occurrences: " + expectedNumberOfClauseOccurrences + ", actual: " + clauseOccurrenceCount
			);
		}

		return this;
	}

	public InterceptedStatementsAssert hasSelectsFromSingleTableWithWhereClause(int expectedAmountOfSelects) {
		long selectCount = logs.stream()
				.filter(statement -> {
					int selects = countNumberOfSqlClauseInStatement(statement, "select");
					if (selects != 1) {
						return false;
					}
					int from = countNumberOfSqlClauseInStatement(statement, "from");
					if (from != 1) {
						return false;
					}

					int where = countNumberOfSqlClauseInStatement(statement, "where");

					return where == 1;
				})
				.count();

		assertThat(selectCount).isEqualTo(expectedAmountOfSelects);

		return this;
	}

	public static int countNumberOfSqlClauseInStatement(String statement, String sqlClause) {
		int index = statement.indexOf(sqlClause);
		int count = 0;

		if (index != -1) {
			count++;
		}

		while (index >= 0) {
			index = statement.indexOf(sqlClause, index + 1);
			if (index != -1) {
				count++;
			}
		}

		return count;

	}

  public InterceptedStatementsAssert hasSingleSelectWithNumberOfJoins(int expectedNumberOfJoins) {
		assertThat(logs.size()).isEqualTo(1);

		Integer numberOfJoins = countNumberOfSqlClauseInStatement(logs.get(0), "join");

		assertThat(numberOfJoins)
				.isEqualTo(expectedNumberOfJoins);

		return this;
	}

	public InterceptedStatementsAssert doesNotHaveClause(String clause) {
		List<String> statementsWithGivenClause = logs.stream()
				.filter(string -> string.contains(clause))
				.toList();

		assertThat(statementsWithGivenClause)
				.isEmpty();

		return this;
	}
}
