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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.IntegrationTestBase;

/**
 * @author Matt S.Y. Ho
 */
public class StartingWithTest extends IntegrationTestBase {

	Customer homerSimpson;
	Customer margeSimpson;
	Customer moeSzyslak;

	@BeforeEach
	public void initData() {
		homerSimpson = customer("Homer", "Simpson").street("Evergreen Terrace").build(em);
		margeSimpson = customer("Marge", "Simpson").street("Evergreen Terrace").build(em);
		moeSzyslak = customer("Moe", "Szyslak").street("Unknown").build(em);
	}

	@Test
	public void filtersByFirstLevelProperty() {
		StartingWith<Customer> lastNameSimpson = new StartingWith<>(queryCtx, "lastName", "Simpson");
		List<Customer> result = customerRepo.findAll(lastNameSimpson);
		assertThat(result).hasSize(2).containsOnly(homerSimpson, margeSimpson);

		StartingWith<Customer> firstNameWithO = new StartingWith<>(queryCtx, "firstName", "Ho");
		result = customerRepo.findAll(firstNameWithO);
		assertThat(result).hasSize(1).containsOnly(homerSimpson);
	}

	@Test
	public void filtersByNestedProperty() {
		StartingWith<Customer> streetWithEvergreen = new StartingWith<>(queryCtx, "address.street", "Evergreen");
		List<Customer> result = customerRepo.findAll(streetWithEvergreen);
		assertThat(result).hasSize(2).containsOnly(homerSimpson, margeSimpson);

		StartingWith<Customer> streetWithTerrace = new StartingWith<>(queryCtx, "address.street", "Terrace");
		result = customerRepo.findAll(streetWithTerrace);
		assertThat(result).hasSize(0);
	}

	@Test
	public void rejectsMissingArgument() {
		assertThrows(IllegalArgumentException.class, () -> new StartingWith<>(queryCtx, "path", new String[] {}));
	}

	@Test
	public void rejectsInvalidNumberOfArguments() {
		assertThrows(IllegalArgumentException.class, () -> new StartingWith<>(queryCtx, "path", new String[] { "a", "b" }));
	}
}
