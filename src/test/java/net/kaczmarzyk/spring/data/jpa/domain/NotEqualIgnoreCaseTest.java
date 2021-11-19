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
package net.kaczmarzyk.spring.data.jpa.domain;

import net.kaczmarzyk.spring.data.jpa.Customer;
import org.junit.jupiter.api.Test;

/**
 * @author Mateusz Fedkowicz
 **/
public class NotEqualIgnoreCaseTest extends NotEqualTest {

	@Test
	public void filtersByStringCaseInsensitive() {
		NotEqualIgnoreCase<Customer> notSimpson = notEqualIgnoreCaseSpec("lastName", "SIMpsOn");
		assertFilterMembers(notSimpson, joeQuimby);

		NotEqualIgnoreCase<Customer> notHomer = notEqualIgnoreCaseSpec("firstName", "HoMeR");
		assertFilterMembers(notHomer, margeSimpson, joeQuimby);
	}

	@Test
	public void filtersByEnumCaseInsensitive() {
		NotEqualIgnoreCase<Customer> notMale = notEqualIgnoreCaseSpec("gender", "maLe");
		assertFilterMembers(notMale, margeSimpson);

		NotEqualIgnoreCase<Customer> notFemale = notEqualIgnoreCaseSpec("gender", "fEmALE");
		assertFilterMembers(notFemale, homerSimpson);
	}

	private <T> NotEqualIgnoreCase<T> notEqualIgnoreCaseSpec(String path, Object expectedValue) {
		return new NotEqualIgnoreCase<>(queryCtx, path, new String[]{expectedValue.toString()}, defaultConverter);
	}

}
