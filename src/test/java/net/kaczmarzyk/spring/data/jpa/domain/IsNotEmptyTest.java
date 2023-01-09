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
package net.kaczmarzyk.spring.data.jpa.domain;

import com.jparams.verifier.tostring.ToStringVerifier;
import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.IntegrationTestBase;
import net.kaczmarzyk.spring.data.jpa.Order;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static net.kaczmarzyk.spring.data.jpa.OrderBuilder.order;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Konrad Hajduga (Tratif sp. z o.o.)
 */
public class IsNotEmptyTest extends IntegrationTestBase {

	Customer homerSimpson;
	Customer margeSimpson;
	Customer moeSzyslak;

	@Before
	public void initData() {
		homerSimpson = customer("Homer", "Simpson").phoneNumbers("123456789").build(em);
		margeSimpson = customer("Marge", "Simpson").orders(order("ring")).build(em);
		moeSzyslak = customer("Moe", "Szyslak").orders(order("snowboard")).build(em);
	}

	@Test
	public void filtersByOneToManyAssociation() {
		IsNotEmpty<Customer> streetWithEvergreen = new IsNotEmpty<>(queryCtx, "orders", new String[0]);
		List<Customer> result = customerRepo.findAll(streetWithEvergreen);
		assertThat(result).hasSize(2).containsOnly(margeSimpson, moeSzyslak);
	}

	@Test
	public void filtersByElementCollectionAssociation() {
		IsNotEmpty<Customer> streetWithEvergreen = new IsNotEmpty<>(queryCtx, "phoneNumbers", new String[0]);
		List<Customer> result = customerRepo.findAll(streetWithEvergreen);
		assertThat(result).hasSize(1).containsOnly(homerSimpson);
	}

	@Test
	public void equalsAndHashCodeContract() {
		EqualsVerifier.forClass(IsNotEmpty.class)
			.usingGetClass()
			.suppress(Warning.NONFINAL_FIELDS)
			.verify();
	}

	@Test
	public void toStringVerifier() {
		ToStringVerifier.forClass(IsNotEmpty.class)
			.withIgnoredFields("queryContext")
			.verify();
	}
}