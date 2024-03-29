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
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;


/**
 * @author Tomasz Kaczmarzyk
 */
public class LikeTest extends IntegrationTestBase {

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
        Like<Customer> lastNameSimpson = new Like<>(queryCtx, "lastName", "Simpson");
        List<Customer> result = customerRepo.findAll(lastNameSimpson);
        assertThat(result)
            .hasSize(2)
            .containsOnly(homerSimpson, margeSimpson);
        
        Like<Customer> firstNameWithO = new Like<>(queryCtx, "firstName", "o");
        result = customerRepo.findAll(firstNameWithO);
        assertThat(result)
            .hasSize(2)
            .containsOnly(homerSimpson, moeSzyslak);
    }

    @Test
    public void filtersByNestedProperty() {
        Like<Customer> streetWithEvergreen = new Like<>(queryCtx, "address.street", "Evergreen");
        List<Customer> result = customerRepo.findAll(streetWithEvergreen);
        assertThat(result).hasSize(2).containsOnly(homerSimpson, margeSimpson);
    }

    @Test
    public void rejectsMissingArgument() {
        assertThatThrownBy(() -> new Like<>(queryCtx, "path", new String[] {}))
                .isInstanceOf(IllegalArgumentException.class);;
    }

    @Test
    public void rejectsInvalidNumberOfArguments() {
        assertThatThrownBy(() -> new Like<>(queryCtx, "path", new String[] {"a", "b"}))
                .isInstanceOf(IllegalArgumentException.class);;
    }

    @Test
    public void equalsAndHashCodeContract() {
        EqualsVerifier.forClass(Like.class)
                .usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }

    @Test
    public void toStringVerifier() {
        ToStringVerifier.forClass(Like.class)
                .withIgnoredFields("queryContext")
                .verify();
    }
}
