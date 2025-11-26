/**
 * Copyright 2014-2025 the original author or authors.
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
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.Gender;
import net.kaczmarzyk.spring.data.jpa.IntegrationTestBase;

/**
 * 
 * @author Tomasz Kaczmarzyk
 *
 */
public class LikeIgnoreCaseLocaleIntegrationTest extends IntegrationTestBase {

	private Customer homerWithLowercaseI;
	private Customer homerWithEnglishCapitalI;
	private Customer homerWithTurkishCapitalI;
	
	@BeforeEach
	public void initData() {
		homerWithLowercaseI = customer("Homer", "Simpson").gender(Gender.MALE).build(em);
		homerWithEnglishCapitalI = customer("Homer", "SIMPSON").gender(Gender.MALE).build(em);
		homerWithTurkishCapitalI = customer("Homer", "SİMPSON").build(em);
	}

	@Test
	public void filtersIgnoringCaseAccordingToDbCollation() {
		LikeIgnoreCase<Customer> simpsons = new LikeIgnoreCase<>(queryCtx, "lastName", new String[]{"i"});
		simpsons.setIgnoreCaseStrategy(IgnoreCaseStrategy.DATABASE_UPPER);
		List<Customer> simpsonsFound = customerRepo.findAll(simpsons);

		assertThat(simpsonsFound).hasSize(2).containsOnly(homerWithLowercaseI, homerWithEnglishCapitalI);
	}
	
	@Test
	public void filtersIgnoringCaseAccordingToDbCollation_turkishCapitalI() {
		LikeIgnoreCase<Customer> simpsons = new LikeIgnoreCase<>(queryCtx, "lastName", new String[]{"İ"});
		simpsons.setIgnoreCaseStrategy(IgnoreCaseStrategy.DATABASE_UPPER);
		List<Customer> simpsonsFound = customerRepo.findAll(simpsons);
		
		assertThat(simpsonsFound).hasSize(1).containsOnly(homerWithTurkishCapitalI);
	}
	
	@Test
	public void filtersIgnoringCaseWithDatabaseLowerStrategy_turkishCapitalI() {
		LikeIgnoreCase<Customer> simpsons = new LikeIgnoreCase<>(queryCtx, "lastName", new String[] { "İ" });
		simpsons.setIgnoreCaseStrategy(IgnoreCaseStrategy.DATABASE_LOWER);
		simpsons.setLocale(new Locale("tr", "TR"));
		List<Customer> simpsonsFound = customerRepo.findAll(simpsons);
		
		assertThat(simpsonsFound).hasSize(1).containsOnly(homerWithTurkishCapitalI);
	}
	
	@Test
	public void filtersIgnoringCaseWithApplicationStrategy_turkishCapitalI() {
		LikeIgnoreCase<Customer> simpsons = new LikeIgnoreCase<>(queryCtx, "lastName", new String[] { "İ" });
		simpsons.setIgnoreCaseStrategy(IgnoreCaseStrategy.APPLICATION);
		simpsons.setLocale(new Locale("tr", "TR"));
		List<Customer> simpsonsFound = customerRepo.findAll(simpsons);

		assertThat(simpsonsFound).hasSize(1).containsOnly(homerWithTurkishCapitalI);
	}
}
