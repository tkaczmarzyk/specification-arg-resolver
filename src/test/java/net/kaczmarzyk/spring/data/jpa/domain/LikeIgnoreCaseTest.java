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
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Locale;

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 
 * @author Michal Jankowski, Hazecod
 *
 */
public class LikeIgnoreCaseTest extends IntegrationTestBase {

    Customer homerSimpson;
    Customer margeSimpson;
    Customer moeSzyslak;
    
    @Before
    public void initData() {
        homerSimpson = customer("Homer", "Simpson").street("Evergreen Terrace").build(em);
        margeSimpson = customer("Marge", "Simpson").street("Evergreen Terrace").build(em);
        moeSzyslak = customer("MOE", "Szyslak").street("Unknown").build(em);
    }
    
    @Test
    public void filtersFirstLevelPropertyIgnoringCase() {
        LikeIgnoreCase<Customer> lastNameSimpson = new LikeIgnoreCase<>(queryCtx, "lastName", "sIMPSOn");
        lastNameSimpson.setLocale(Locale.getDefault());
        List<Customer> result = customerRepo.findAll(lastNameSimpson);
        assertThat(result)
            .hasSize(2)
            .containsOnly(homerSimpson, margeSimpson);
        
        LikeIgnoreCase<Customer> firstNameWithO = new LikeIgnoreCase<>(queryCtx, "firstName", "o");
        firstNameWithO.setLocale(Locale.getDefault());
        result = customerRepo.findAll(firstNameWithO);
        assertThat(result)
            .hasSize(2)
            .containsOnly(homerSimpson, moeSzyslak);
    }

    @Test
    public void filtersByNestedPropertyIgnoringCase() {
        LikeIgnoreCase<Customer> streetWithEvergreen = new LikeIgnoreCase<>(queryCtx, "address.street", "EvErGReeN");
        streetWithEvergreen.setLocale(Locale.getDefault());
        List<Customer> result = customerRepo.findAll(streetWithEvergreen);
        assertThat(result).hasSize(2).containsOnly(homerSimpson, margeSimpson);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsMissingArgument() {
        new LikeIgnoreCase<>(queryCtx, "path", new String[] {});
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsInvalidNumberOfArguments() {
        new LikeIgnoreCase<>(queryCtx, "path", new String[] { "a", "b" });
    }

    @Test
    public void equalsAndHashCodeContract() {
        EqualsVerifier.forClass(LikeIgnoreCase.class)
                .usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }
}

