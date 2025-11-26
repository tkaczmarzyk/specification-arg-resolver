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

import com.jparams.verifier.tostring.ToStringVerifier;
import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.IntegrationTestBase;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.List;

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static org.assertj.core.api.Assertions.assertThat;

public class DisjunctionTest extends IntegrationTestBase {

    Customer homerSimpson;
    Customer margeSimpson;
    Customer bartSimpson;
    Customer moeSzyslak;
    Customer nedFlanders;
    
    @BeforeEach
    public void initData() {
        homerSimpson = customer("Homer", "Simpson").street("Evergreen Terrace").build(em);
        margeSimpson = customer("Marge", "Simpson").street("Evergreen Terrace").build(em);
        bartSimpson = customer("Bart", "Simpson").street("Evergreen Terrace").build(em);
        moeSzyslak = customer("Moe", "Szyslak").street("Unknown").build(em);
        nedFlanders = customer("Ned", "Flanders").street("Evergreen Terrace").build(em);
    }
    
    @Test
    public void shouldIncludeResultsOfBothSpecs() throws ParseException {
        Like<Customer> lastNameSimpson = new Like<>(queryCtx, "lastName", "Simpson");
        Like<Customer> lastNameSzyslak = new Like<>(queryCtx, "lastName", "Szyslak");
        
        List<Customer> result = customerRepo.findAll(new Disjunction<>(lastNameSimpson, lastNameSzyslak));
        
        assertThat(result)
            .hasSize(4)
            .containsOnly(homerSimpson, margeSimpson, bartSimpson, moeSzyslak);
    }

    @Test
    public void equalsAndHashCodeContract() {
        EqualsVerifier.forClass(Disjunction.class)
                .usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }

    @Test
    public void toStringVerifier() {
        ToStringVerifier.forClass(Disjunction.class)
                .verify();
    }
}
