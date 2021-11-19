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

import org.junit.jupiter.api.Test;

import static net.kaczmarzyk.spring.data.jpa.utils.JoinPathUtils.pathToJoinContainsAlias;
import static net.kaczmarzyk.spring.data.jpa.utils.JoinPathUtils.pathToJoinSplittedByDot;
import static net.kaczmarzyk.spring.data.jpa.utils.ThrowableAssertions.assertThrows;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JoinPathUtilsTest {

	@Test
	public void returnsPathToJoinContainsAliasIfPathToJoinContainsDot() {
		assertTrue(pathToJoinContainsAlias("alias.attribute"));
		assertTrue(pathToJoinContainsAlias("invalidpath.alias.attribute.attribute2"));
		assertTrue(pathToJoinContainsAlias("."));
	}

	@Test
	public void throwsIllegalArgumentExceptionWhenPathContainsEmptyAlias() {
		assertThrows(
				IllegalArgumentException.class,
				() -> pathToJoinSplittedByDot(".attribute"),
				"Expected path to join with single alias in the pattern: 'alias.attribute'. An alias has to be not empty value!"
		);
	}

	@Test
	public void throwsIllegalArgumentExceptionWhenPathContainsEmptyAttribute() {
		assertThrows(
				IllegalArgumentException.class,
				() -> pathToJoinSplittedByDot("alias."),
				"Expected path to join with single alias in the pattern: 'alias.attribute' (without an apostrophe) where: " +
						"alias is a alias of another join (of which annotation should be before annotation of actual join)," +
						"attribute - name of the attribute for the target of the join."

		);
	}

	@Test
	public void throwsIllegalArgumentExceptionWhenPathContainsMultipleDots() {
		assertThrows(
				IllegalArgumentException.class,
				() -> pathToJoinSplittedByDot("alias.attribute.attribute2"),
				"Expected path to join with single alias in the pattern: 'alias.attribute' (without an apostrophe) where: " +
						"alias is a alias of another join (of which annotation should be before annotation of actual join)," +
						"attribute - name of the attribute for the target of the join."
		);
	}

	@Test
	public void returnsPathSplittedByDot() {
		String[] splittedPath = pathToJoinSplittedByDot("alias.attribute");

		assertThat(splittedPath)
				.containsExactly("alias", "attribute");
	}

}