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

public class JoinPathUtils {

	public static boolean pathToJoinContainsAlias(String pathToJoinOn) {
		return pathToJoinOn.contains(".");
	}

	public static String[] pathToJoinSplittedByDot(String pathToFetch) {
		String[] pathSplittedByDot = pathToFetch.split("\\.");

		if (pathSplittedByDot.length != 2) {
			throw new IllegalArgumentException(
					"Expected path to join with single alias in the pattern: 'alias.attribute' (without an apostrophe) where: " +
							"alias is a alias of another join (of which annotation should be before annotation of actual join)," +
							"attribute - name of the attribute for the target of the join."
			);
		}

		if(pathSplittedByDot[0].isEmpty()) {
			throw new IllegalArgumentException("Expected path to join with single alias in the pattern: 'alias.attribute'. An alias has to be not empty value!");
		}

		return pathSplittedByDot;
	}


}
