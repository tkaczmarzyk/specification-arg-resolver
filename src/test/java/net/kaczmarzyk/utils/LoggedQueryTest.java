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

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * @author Tomasz Kaczmarzyk
 */
public class LoggedQueryTest {

	@Test
	public void countsJoinsPerPath() {
		LoggedQuery query = new LoggedQuery(
				"HQL: select distinct generatedAlias0 from net.kaczmarzyk.spring.data.jpa.Movie as generatedAlias0"
				+ " inner join generatedAlias0.stars as generatedAlias1"
				+ " inner join generatedAlias0.directors as generatedAlias2"
				+ " inner join generatedAlias0.stars as generatedAlias3"
				+ " inner join generatedAlias0.directors as generatedAlias4"
				+ " inner join generatedAlias0.producers as generatedAlias5");

		assertThat(query.countJoinsForPath(".stars")).isEqualTo(2);
		assertThat(query.countJoinsForPath(".directors")).isEqualTo(2);
		assertThat(query.countJoinsForPath(".producers")).isEqualTo(1);
		assertThat(query.countJoinsForPath(".notExistingField")).isEqualTo(0);
	}
	
	@Test
	public void countsTotalNumberOfJoins() {
		LoggedQuery query = new LoggedQuery(
				"HQL: select distinct generatedAlias0 from net.kaczmarzyk.spring.data.jpa.Movie as generatedAlias0"
				+ " inner join generatedAlias0.stars as generatedAlias1"
				+ " inner join generatedAlias0.directors as generatedAlias2"
				+ " inner join generatedAlias0.stars as generatedAlias3"
				+ " inner join generatedAlias0.directors as generatedAlias4"
				+ " inner join generatedAlias0.producers as generatedAlias5");

		assertThat(query.countJoins()).isEqualTo(5);
	}
}
