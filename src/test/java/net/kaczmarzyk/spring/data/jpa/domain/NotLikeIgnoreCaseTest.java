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

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.IntegrationTestBase;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.Test;

import java.util.List;
import java.util.Locale;

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static org.assertj.core.api.Assertions.assertThat;

public class NotLikeIgnoreCaseTest extends IntegrationTestBase {

    Customer homerSimpson;
    Customer margeSimpson;
    Customer moeSzyslak;

    @BeforeEach
    public void initData() {
        homerSimpson = customer("Homer", "Simpson").street("Evergreen Terrace").build(em);
        margeSimpson = customer("Marge", "Simpson").street("Evergreen Terrace").build(em);
        moeSzyslak = customer("MOE", "Szyslak").street("Unknown").build(em);
    }

    @Test
    public void filtersFirstLevelPropertyIgnoringCase() {
        NotLikeIgnoreCase<Customer> lastNameSimpson = new NotLikeIgnoreCase<>(queryCtx, "lastName", "sIMPSOn");
        lastNameSimpson.setLocale(Locale.getDefault());
        List<Customer> result = customerRepo.findAll(lastNameSimpson);
        assertThat(result)
                .hasSize(1)
                .containsOnly(moeSzyslak);

        NotLikeIgnoreCase<Customer> firstNameWithO = new NotLikeIgnoreCase<>(queryCtx, "firstName", "o");
        firstNameWithO.setLocale(Locale.getDefault());
        result = customerRepo.findAll(firstNameWithO);
        assertThat(result)
                .hasSize(1)
                .containsOnly(margeSimpson);
    }

    @Test
    public void filtersByNestedPropertyIgnoringCase() {
        NotLikeIgnoreCase<Customer> streetWithEvergreen = new NotLikeIgnoreCase<>(queryCtx, "address.street", "EvErGReeN");
        streetWithEvergreen.setLocale(Locale.getDefault());
        List<Customer> result = customerRepo.findAll(streetWithEvergreen);
        assertThat(result)
                .hasSize(1)
                .containsOnly(moeSzyslak);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsMissingArgument() {
        new NotLikeIgnoreCase<>(queryCtx, "path", new String[] {});
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsInvalidNumberOfArguments() {
        new NotLikeIgnoreCase<>(queryCtx, "path", new String[] { "a", "b" });
    }

    @Test
    public void equalsAndHashCodeContract() {
        EqualsVerifier.forClass(NotLikeIgnoreCase.class)
                .usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }

}
