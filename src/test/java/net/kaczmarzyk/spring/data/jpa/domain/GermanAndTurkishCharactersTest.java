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

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.IntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Jakub Radlica
 */
public class GermanAndTurkishCharactersTest extends IntegrationTestBase {
	
	// German test data
	private Customer juergenWeiss;      // Contains ß (eszett)
	private Customer melissaMueller;    // Contains ü (umlaut)
	private Customer hansSchmidt;       // Regular ASCII for comparison
	
	// Turkish test data
	private Customer istanbulUser;      // İstanbul with uppercase İ (with dot)
	private Customer ibrahimLowerCase; // ibrahim with lowercase i
	private Customer ibrahimUpperCase; // İBRAHİM with uppercase İ (with dot)
	private Customer englishIan;        // Ian with regular ASCII I
	
	@BeforeEach
	public void initData() {
		// German customers
		juergenWeiss = customer("Jürgen", "Weiß").build(em);
		melissaMueller = customer("Melissa", "Müller").build(em);
		hansSchmidt = customer("Hans", "Schmidt").build(em);
		
		// Turkish customers
		istanbulUser = customer("İstanbul", "User").build(em);
		ibrahimLowerCase = customer("ibrahim", "istanbul").build(em);
		ibrahimUpperCase = customer("İBRAHİM", "İSTANBUL").build(em);
		englishIan = customer("Ian", "Smith").build(em);
	}
	
	@Nested
	@DisplayName("German characters (ß, ü)")
	class GermanCharacter {
		
		@Nested
		class IgnoreCaseStrategyApplication {
			
			@Test
			@DisplayName("H2 still converts ß→SS even with COLLATION=EN")
			public void h2ConvertsEszettDespiteCollation() {
				//given
				LikeIgnoreCase<Customer> query = new LikeIgnoreCase<>(queryCtx, "lastName", "ß");
				query.setIgnoreCaseStrategy(IgnoreCaseStrategy.APPLICATION);
				query.setLocale(Locale.GERMAN);
				
				//when: Java converts ß to SS, H2 also converts despite COLLATION=EN
				List<Customer> result = customerRepo.findAll(query);
				
				//then: H2 finds the record (but MySQL/PostgreSQL wouldn't!)
				assertThat(result)
						.as("H2 ignores COLLATION=EN and still converts ß→SS. " +
						    "In MySQL/PostgreSQL this would return empty!")
						.containsExactly(juergenWeiss);
				// NOTE: This test documents H2-specific behavior.
				// With MySQL/PostgreSQL default collation, this would be .isEmpty()
			}
			
			@Test
			public void findsByUmlaut() {
				//given
				LikeIgnoreCase<Customer> query = new LikeIgnoreCase<>(queryCtx, "lastName", "müller");
				query.setIgnoreCaseStrategy(IgnoreCaseStrategy.APPLICATION);
				query.setLocale(Locale.GERMAN);
				
				//when:
				List<Customer> result = customerRepo.findAll(query);
				
				//then
				assertThat(result)
						.containsExactly(melissaMueller); // 'müller'.toUpperCase() = 'MÜLLER'
			}
		}
		
		@Nested
		class IgnoreCaseStrategyDatabaseUpper {
			
			@Test
			public void findsByEszett() {
				//given
				LikeIgnoreCase<Customer> query = new LikeIgnoreCase<>(queryCtx, "lastName", "ß");
				query.setIgnoreCaseStrategy(IgnoreCaseStrategy.DATABASE_UPPER);
				
				//when
				List<Customer> result = customerRepo.findAll(query);
				
				//then
				assertThat(result)
						.containsExactly(juergenWeiss);
			}
			
			@Test
			public void findsByUmlaut() {
				//given
				LikeIgnoreCase<Customer> query = new LikeIgnoreCase<>(queryCtx, "lastName", "MÜLLER");
				query.setIgnoreCaseStrategy(IgnoreCaseStrategy.DATABASE_UPPER);
				
				//when
				List<Customer> result = customerRepo.findAll(query);
				
				//then
				assertThat(result)
						.containsExactly(melissaMueller);
			}
		}
		
		@Nested
		class IgnoreCaseStrategyDatabaseLower {
			
			@Test
			public void findsByEszett() {
				//given
				LikeIgnoreCase<Customer> query = new LikeIgnoreCase<>(queryCtx, "lastName", "WEIß");
				query.setIgnoreCaseStrategy(IgnoreCaseStrategy.DATABASE_LOWER);
				
				//when
				List<Customer> result = customerRepo.findAll(query);
				
				//then
				assertThat(result)
						.containsExactly(juergenWeiss);
			}
			
			@Test
			public void findsByUmlaut() {
				//given
				LikeIgnoreCase<Customer> query = new LikeIgnoreCase<>(queryCtx, "lastName", "Üller");
				query.setIgnoreCaseStrategy(IgnoreCaseStrategy.DATABASE_LOWER);
				
				//when
				List<Customer> result = customerRepo.findAll(query);
				
				//then
				assertThat(result)
						.containsExactly(melissaMueller);
			}
		}
	}
	
	@Nested
	@DisplayName("Turkish characters (İ/i distinction)")
	class TurkishCharacter {
		
		@Nested
		class IgnoreCaseStrategyApplication {
			
			@Test
			public void findsByITurkishCapital() {
				//given
				LikeIgnoreCase<Customer> query = new LikeIgnoreCase<>(queryCtx, "firstName", "i");
				query.setIgnoreCaseStrategy(IgnoreCaseStrategy.APPLICATION);
				query.setLocale(new Locale("tr", "TR"));
				
				//when
				List<Customer> result = customerRepo.findAll(query);
				
				//then
				assertThat(result)
						.contains(istanbulUser, ibrahimUpperCase)
						.doesNotContain(englishIan, ibrahimLowerCase);
			}
			
			@Test
			public void findsByTurkishMixedCase() {
				//given
				LikeIgnoreCase<Customer> query = new LikeIgnoreCase<>(queryCtx, "firstName", "İstanbul");
				query.setIgnoreCaseStrategy(IgnoreCaseStrategy.APPLICATION);
				query.setLocale(new Locale("tr", "TR"));
				
				//when
				List<Customer> result = customerRepo.findAll(query);
				
				//then
				assertThat(result)
						.as("Database lowercase conversion for Turkish characters")
						.containsExactly(istanbulUser);
			}
			
			@Test
			public void findsByNonTurkish() {
				//given
				LikeIgnoreCase<Customer> query = new LikeIgnoreCase<>(queryCtx, "firstName", "I");
				query.setIgnoreCaseStrategy(IgnoreCaseStrategy.APPLICATION);
				query.setLocale(new Locale("tr", "TR"));
				
				//when
				List<Customer> result = customerRepo.findAll(query);
				
				//then
				assertThat(result)
						.containsExactlyInAnyOrder(englishIan, melissaMueller, ibrahimLowerCase);
			}
		}
		
		@Nested
		class IgnoreCaseStrategyDatabaseUpper {
			
			@Test
			public void findsByTurkishUppercase() {
				//given
				LikeIgnoreCase<Customer> query = new LikeIgnoreCase<>(queryCtx, "firstName", "İBRAHİM");
				query.setIgnoreCaseStrategy(IgnoreCaseStrategy.DATABASE_UPPER);
				
				//when
				List<Customer> result = customerRepo.findAll(query);
				
				//then
				assertThat(result)
						.contains(ibrahimUpperCase);
			}
			
			@Test
			public void findsByTurkishMixedCase() {
				//given
				LikeIgnoreCase<Customer> query = new LikeIgnoreCase<>(queryCtx, "firstName", "İstanbul");
				query.setIgnoreCaseStrategy(IgnoreCaseStrategy.DATABASE_UPPER);
				
				//when
				List<Customer> result = customerRepo.findAll(query);
				
				//then
				assertThat(result)
						.as("Database lowercase conversion for Turkish characters")
						.containsExactly(istanbulUser);
			}
			
			@Test
			public void findsByNonTurkish() {
				//given
				LikeIgnoreCase<Customer> query = new LikeIgnoreCase<>(queryCtx, "firstName", "I");
				query.setIgnoreCaseStrategy(IgnoreCaseStrategy.DATABASE_UPPER);
				
				//when
				List<Customer> result = customerRepo.findAll(query);
				
				//then
				assertThat(result)
						.containsExactlyInAnyOrder(englishIan, melissaMueller, ibrahimLowerCase);
			}
		}
		
		@Nested
		class IgnoreCaseStrategyDatabaseLower {
			
			@Test
			public void findsByTurkishUppercase() {
				//given
				LikeIgnoreCase<Customer> query = new LikeIgnoreCase<>(queryCtx, "firstName", "İBRAHİM");
				query.setIgnoreCaseStrategy(IgnoreCaseStrategy.DATABASE_LOWER);
				
				//when
				List<Customer> result = customerRepo.findAll(query);
				
				//then
				assertThat(result)
						.contains(ibrahimUpperCase);
			}
			
			@Test
			public void findsByTurkishMixedCase() {
				//given
				LikeIgnoreCase<Customer> query = new LikeIgnoreCase<>(queryCtx, "firstName", "İstanbul");
				query.setIgnoreCaseStrategy(IgnoreCaseStrategy.DATABASE_LOWER);
				
				//when
				List<Customer> result = customerRepo.findAll(query);
				
				//then
				assertThat(result)
						.as("Database lowercase conversion for Turkish characters")
						.containsExactly(istanbulUser);
			}
			
			@Test
			public void findsByNonTurkish() {
				//given
				LikeIgnoreCase<Customer> query = new LikeIgnoreCase<>(queryCtx, "firstName", "I");
				query.setIgnoreCaseStrategy(IgnoreCaseStrategy.DATABASE_LOWER);
				
				//when
				List<Customer> result = customerRepo.findAll(query);
				
				//then
				assertThat(result)
						.containsExactlyInAnyOrder(englishIan, melissaMueller, istanbulUser, ibrahimLowerCase, ibrahimUpperCase);
			}
		}
	}
}
