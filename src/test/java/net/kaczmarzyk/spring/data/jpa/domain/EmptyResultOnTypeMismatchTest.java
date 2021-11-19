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

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.Gender;
import net.kaczmarzyk.spring.data.jpa.IntegrationTestBase;

/**
 * @author Tomasz Kaczmarzyk
 */
public class EmptyResultOnTypeMismatchTest extends IntegrationTestBase {

	private Customer homerSimpson;
	private Customer margeSimpson;

	@BeforeEach
	public void initData() {
		homerSimpson = customer("Homer", "Simpson").gender(Gender.MALE).registrationDate(2015, 03, 01).weight(121).build(em);
		margeSimpson = customer("Marge", "Simpson").gender(Gender.FEMALE).registrationDate(2015, 03, 01).weight(55).build(em);
	}

	@Test
	public void producesEmptyResultOnTypeMismatch_long() {
		EmptyResultOnTypeMismatch<Customer> idQuery = new EmptyResultOnTypeMismatch<>(new Equal<Customer>(queryCtx, "id", new String[] { "not a long" }, defaultConverter));
		List<Customer> result = customerRepo.findAll(idQuery);
		assertThat(result).isEmpty();
	}
	
	@Test
	public void producesEmptyResultOnTypeMismatch_longPrimitive() {
		EmptyResultOnTypeMismatch<Customer> weightQuery = new EmptyResultOnTypeMismatch<>(new Equal<Customer>(queryCtx, "weightLong", new String[] { "not a long" }, defaultConverter));
		List<Customer> result = customerRepo.findAll(weightQuery);
		assertThat(result).isEmpty();
	}
	
	@Test
	public void producesEmptyResultOnTypeMismatch_intPrimitive() {
		EmptyResultOnTypeMismatch<Customer> weightQuery = new EmptyResultOnTypeMismatch<>(new Equal<Customer>(queryCtx, "weightInt", new String[] { "not an int" }, defaultConverter));
		List<Customer> result = customerRepo.findAll(weightQuery);
		assertThat(result).isEmpty();
	}
	
	@Test
	public void producesEmptyResultOnTypeMismatch_enum() {
		EmptyResultOnTypeMismatch<Customer> genderQuery = new EmptyResultOnTypeMismatch<>(new Equal<Customer>(queryCtx, "gender", new String[] { "not an enum" }, defaultConverter));
		List<Customer> result = customerRepo.findAll(genderQuery);
		assertThat(result).isEmpty();
	}
	
	@Test
	public void producesEmptyResultOnTypeMismatch_date() {
		EmptyResultOnTypeMismatch<Customer> registrationDateQuery = new EmptyResultOnTypeMismatch<>(new Equal<Customer>(queryCtx, "registrationDate", new String[] { "not a date" }, defaultConverter));
		List<Customer> result = customerRepo.findAll(registrationDateQuery);
		assertThat(result).isEmpty();
	}
	
	@Test
	public void producesEmptyResultOnTypeMismatch_boolean() {
		EmptyResultOnTypeMismatch<Customer> goldStatusQuery = new EmptyResultOnTypeMismatch<>(new Equal<Customer>(queryCtx, "gold", new String[] { "not a boolean" }, defaultConverter));
		List<Customer> result = customerRepo.findAll(goldStatusQuery);
		assertThat(result).isEmpty();
	}
	
	@Test
	public void usesWrappedSpecWhenTypeMatches() {
		EmptyResultOnTypeMismatch<Customer> homerId = new EmptyResultOnTypeMismatch<>(new Equal<Customer>(queryCtx, "id", new String[] { homerSimpson.getId().toString() }, defaultConverter));
		List<Customer> result = customerRepo.findAll(homerId);
		assertThat(result).hasSize(1).contains(homerSimpson);
	}
}
