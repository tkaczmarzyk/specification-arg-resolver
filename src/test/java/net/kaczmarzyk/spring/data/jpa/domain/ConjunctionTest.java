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


public class ConjunctionTest extends IntegrationTestBase {

    Customer homerSimpson;
    Customer margeSimpson;
    Customer bartSimpson;
    Customer moeSzyslak;
    Customer nedFlanders;
    
    @BeforeEach
    public void initData() {
        homerSimpson = customer("Homer", "Simpson").registrationDate(2014, 03, 15).street("Evergreen Terrace").build(em);
        margeSimpson = customer("Marge", "Simpson").registrationDate(2014, 03, 20).street("Evergreen Terrace").build(em);
        bartSimpson = customer("Bart", "Simpson").registrationDate(2014, 03, 25).street("Evergreen Terrace").build(em);
        moeSzyslak = customer("Moe", "Szyslak").registrationDate(2014, 03, 15).street("Unknown").build(em);
        nedFlanders = customer("Ned", "Flanders").registrationDate(2014, 03, 25).street("Evergreen Terrace").build(em);
    }
    
    @Test
    public void shouldFilterWithBothSpecs() throws ParseException {
        Like<Customer> streetWithEvergreen = new Like<>(queryCtx, "address.street", "Evergreen");
        LessThan<Customer> registeredBefore21st = new LessThan<>(queryCtx, "registrationDate", new String[] {"2014-03-21"}, defaultConverter);
        
        List<Customer> result = customerRepo.findAll(new Conjunction<>(streetWithEvergreen, registeredBefore21st));
        
        assertThat(result)
            .hasSize(2)
            .containsOnly(homerSimpson, margeSimpson);
    }

    @Test
    public void equalsAndHashCodeContract() {
        EqualsVerifier.forClass(Conjunction.class)
                .usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS)
                .withIgnoredFields("queriesWithInitializedFakes")
                .verify();
    }

    @Test
    public void toStringVerifier() {
        ToStringVerifier.forClass(Conjunction.class)
                .withIgnoredFields("queriesWithInitializedFakes")
                .verify();
    }
}
