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
package net.kaczmarzyk.utils.interceptor;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class InterceptedStatementsAssert {

	private final List<String> logs;

	private InterceptedStatementsAssert(List<String> logs) {
		this.logs = new ArrayList<>(logs);
	}

	public static InterceptedStatementsAssert assertThatInterceptedStatements() {
		return new InterceptedStatementsAssert(HibernateStatementInterceptor.getInterceptedStatements());
	}

	public InterceptedStatementsAssert hasSelects(int expectedAmountOfSelects) {
		long selectCount = logs.stream()
				.filter(statement -> statement.contains("SELECT") || statement.contains("select"))
				.count();

		assertThat(selectCount).isEqualTo(expectedAmountOfSelects);

		return this;
	}

	public InterceptedStatementsAssert hasJoins(int expectedNumberOfJoins) {
		long joinsCount = logs.stream()
				.map(statement -> countNumberOfSqlClauseInStatement(statement, "join"))
				.reduce((i, joinCounter) -> joinCounter += i)
				.orElse(0);

		if (joinsCount != expectedNumberOfJoins) {
			throw new AssertionError(
					"Expected clause number of joins: " + expectedNumberOfJoins + ", actual: " + joinsCount
			);
		}

		return this;
	}

	public InterceptedStatementsAssert hasOneClause(String clause) {
		return hasClause(clause, 1);
	}

	public InterceptedStatementsAssert hasClause(String clause, int expectedNumberOfClauseOccurrences) {
		long clauseOccurrenceCount = logs.stream()
				.map(statement -> countNumberOfSqlClauseInStatement(statement, clause))
				.reduce((i, joinCounter) -> joinCounter += i)
				.orElse(0);

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
}
