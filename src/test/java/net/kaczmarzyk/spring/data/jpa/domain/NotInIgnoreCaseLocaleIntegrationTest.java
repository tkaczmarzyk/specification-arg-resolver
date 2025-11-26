/*
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
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Tomasz Kaczmarzyk
 *
 */
public class NotInIgnoreCaseLocaleIntegrationTest extends IntegrationTestBase {

    private Customer homerWithLowercaseI;
    private Customer homerWithEnglishCapitalI;
    private Customer homerWithTurkishCapitalI;

    @BeforeEach
    public void initData() {
        homerWithLowercaseI = customer("Homer", "Simpson").build(em);
        homerWithEnglishCapitalI = customer("Homer", "SIMPSON").build(em);
        homerWithTurkishCapitalI = customer("Homer", "SİMPSON").build(em);
    }

    @Test
    public void filtersIgnoringCaseAccordingToDbCollation() {
        NotInIgnoreCase<Customer> simpsons = new NotInIgnoreCase<>(queryCtx, "lastName", new String[]{"simpson"}, defaultConverter);
        simpsons.setIgnoreCaseStrategy(IgnoreCaseStrategy.DATABASE_UPPER);
        List<Customer> simpsonsFound = customerRepo.findAll(simpsons);

        assertThat(simpsonsFound).hasSize(1).containsOnly(homerWithTurkishCapitalI);
    }

    @Test
    public void filtersIgnoringCaseAccordingToDbCollation_turkishCapitalI() {
        NotInIgnoreCase<Customer> simpsons = new NotInIgnoreCase<>(queryCtx, "lastName", new String[]{"SİMPSON"}, defaultConverter);
        simpsons.setIgnoreCaseStrategy(IgnoreCaseStrategy.DATABASE_UPPER);
        List<Customer> simpsonsFound = customerRepo.findAll(simpsons);

        assertThat(simpsonsFound).hasSize(2).containsOnly(homerWithLowercaseI, homerWithEnglishCapitalI);
    }

    @Test
    public void filtersIgnoringCaseWithDatabaseLowerStrategy_turkishCapitalI() {
        NotInIgnoreCase<Customer> simpsons = new NotInIgnoreCase<>(queryCtx, "lastName", new String[]{"SİMPSON"}, defaultConverter);
        simpsons.setIgnoreCaseStrategy(IgnoreCaseStrategy.DATABASE_LOWER);
        simpsons.setLocale(new Locale("tr", "TR"));
        List<Customer> simpsonsFound = customerRepo.findAll(simpsons);

        assertThat(simpsonsFound).hasSize(2).containsOnly(homerWithLowercaseI, homerWithEnglishCapitalI);
    }

    @Test
    public void filtersIgnoringCaseWithApplicationStrategy_turkishCapitalI() {
        NotInIgnoreCase<Customer> simpsons = new NotInIgnoreCase<>(queryCtx, "lastName", new String[]{"SİMPSON"}, defaultConverter);
        simpsons.setIgnoreCaseStrategy(IgnoreCaseStrategy.APPLICATION);
        simpsons.setLocale(new Locale("tr", "TR"));
        List<Customer> simpsonsFound = customerRepo.findAll(simpsons);

        assertThat(simpsonsFound).hasSize(2).containsOnly(homerWithLowercaseI, homerWithEnglishCapitalI);
    }
}
