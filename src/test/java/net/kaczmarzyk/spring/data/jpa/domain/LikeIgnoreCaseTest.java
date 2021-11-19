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

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.IntegrationTestBase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 
 * @author Michal Jankowski, Hazecod
 *
 */
public class LikeIgnoreCaseTest extends IntegrationTestBase {

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
        LikeIgnoreCase<Customer> lastNameSimpson = new LikeIgnoreCase<>(queryCtx, "lastName", "sIMPSOn");
        List<Customer> result = customerRepo.findAll(lastNameSimpson);
        assertThat(result)
            .hasSize(2)
            .containsOnly(homerSimpson, margeSimpson);
        
        LikeIgnoreCase<Customer> firstNameWithO = new LikeIgnoreCase<>(queryCtx, "firstName", "o");
        result = customerRepo.findAll(firstNameWithO);
        assertThat(result)
            .hasSize(2)
            .containsOnly(homerSimpson, moeSzyslak);
    }

    @Test
    public void filtersByNestedPropertyIgnoringCase() {
        LikeIgnoreCase<Customer> streetWithEvergreen = new LikeIgnoreCase<>(queryCtx, "address.street", "EvErGReeN");
        List<Customer> result = customerRepo.findAll(streetWithEvergreen);
        assertThat(result).hasSize(2).containsOnly(homerSimpson, margeSimpson);
    }

    @Test
    public void rejectsMissingArgument() {
        assertThrows(IllegalArgumentException.class, () -> new LikeIgnoreCase<>(queryCtx, "path", new String[] {}));
    }

    @Test
    public void rejectsInvalidNumberOfArguments() {
        assertThrows(IllegalArgumentException.class, () -> new LikeIgnoreCase<>(queryCtx, "path", new String[] { "a", "b" }));
    }

}

