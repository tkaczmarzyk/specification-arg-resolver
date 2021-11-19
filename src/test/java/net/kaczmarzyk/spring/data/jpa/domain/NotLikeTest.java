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
 * @author Tomasz Kaczmarzyk
 */
public class NotLikeTest extends IntegrationTestBase {

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
        NotLike<Customer> lastNameNotSimpson = new NotLike<>(queryCtx, "lastName", "Simpson");
        List<Customer> result = customerRepo.findAll(lastNameNotSimpson);
        assertThat(result)
            .hasSize(1)
            .containsOnly(moeSzyslak);
        
        NotLike<Customer> firstNameWithoutO = new NotLike<>(queryCtx, "firstName", "o");
        result = customerRepo.findAll(firstNameWithoutO);
        assertThat(result)
            .hasSize(1)
            .containsOnly(margeSimpson);
    }

    @Test
    public void filtersByNestedProperty() {
    	NotLike<Customer> streetWithoutEvergreen = new NotLike<>(queryCtx, "address.street", "Evergreen");
        List<Customer> result = customerRepo.findAll(streetWithoutEvergreen);
        assertThat(result).hasSize(1).containsOnly(moeSzyslak);
    }

    @Test
    public void rejectsMissingArgument() {
        assertThrows(IllegalArgumentException.class, () -> new NotLike<>(queryCtx, "path", new String[] {}));
    }

    @Test
    public void rejectsInvalidNumberOfArguments() {
        assertThrows(IllegalArgumentException.class, () -> new NotLike<>(queryCtx, "path", new String[] {"a", "b"}));
    }
}
