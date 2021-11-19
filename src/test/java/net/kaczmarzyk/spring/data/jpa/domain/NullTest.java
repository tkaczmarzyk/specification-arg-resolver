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
import net.kaczmarzyk.spring.data.jpa.IntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Tomasz Kaczmarzyk
 * @author Gerald Humphries
 */
public class NullTest extends IntegrationTestBase {

	Customer bartSimpson;
	Customer lisaSimpson;

	@BeforeEach
	public void initData() {
		bartSimpson = customer("Bart", "Simpson").nickName("El Barto").street("Evergreen Terrace").build(em);
		lisaSimpson = customer("Lisa", "Simpson").registrationDate(2014, 03, 20).street("Evergreen Terrace").build(em);
	}

	@Test
	public void findsCustomersWithNullField() {
		Null<Customer> spec = new Null<>(queryCtx, "nickName", new String[]{"true"}, defaultConverter);

		List<Customer> found = customerRepo.findAll(spec);

		assertThat(found).hasSize(1).containsOnly(lisaSimpson);
	}

	@Test
	public void findsCustomersWithNotNullField() {
		Null<Customer> spec = new Null<>(queryCtx, "nickName", new String[]{"false"}, defaultConverter);

		List<Customer> found = customerRepo.findAll(spec);

		assertThat(found).hasSize(1).containsOnly(bartSimpson);
	}
}
