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
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Hubert Gotfryd (Tratif sp. z o.o.)
 */
public class FalseTest extends IntegrationTestBase {

    private Customer homerSimpson;
    private Customer maggieSimpson;
    private Customer margeSimpson;
    private Customer moeSzyslak;

    @Before
    public void initData() {
        homerSimpson = customer("Homer", "Simpson").golden().build(em);
        maggieSimpson = customer("Maggie", "Simpson").golden().build(em);
        margeSimpson = customer("Marge", "Simpson").notGolden().build(em);
        moeSzyslak = customer("Moe", "Szyslak").notGolden().build(em);
    }

    @Test
    public void filtersByFalseValueOfPrimitiveBooleanType() {
        //given
        False<Customer> nonGoldenCustomers = new False<>(queryCtx, "gold", new String[]{"true"}, defaultConverter);

        //when
        List<Customer> result = customerRepo.findAll(nonGoldenCustomers);

        //then
        assertThat(result)
                .hasSize(2)
                .containsOnly(margeSimpson, moeSzyslak);
    }

    @Test
    public void filtersByFalseValueOfObjectBooleanType() {
        //given
        False<Customer> nonGoldenCustomers = new False<>(queryCtx, "goldObj", new String[]{"true"}, defaultConverter);

        //when
        List<Customer> result = customerRepo.findAll(nonGoldenCustomers);

        //then
        assertThat(result)
                .hasSize(2)
                .containsOnly(margeSimpson, moeSzyslak);
    }

    @Test
    public void filtersByTrueValueOfPrimitiveBooleanType() {
        //given
        False<Customer> goldenCustomers = new False<>(queryCtx, "gold", new String[]{"false"}, defaultConverter);

        //when
        List<Customer> result = customerRepo.findAll(goldenCustomers);

        //then
        assertThat(result)
                .hasSize(2)
                .containsOnly(homerSimpson, maggieSimpson);
    }

    @Test
    public void filtersByTrueValueOfObjectBooleanType() {
        //given
        False<Customer> goldenCustomers = new False<>(queryCtx, "goldObj", new String[]{"false"}, defaultConverter);

        //when
        List<Customer> result = customerRepo.findAll(goldenCustomers);

        //then
        assertThat(result)
                .hasSize(2)
                .containsOnly(homerSimpson, maggieSimpson);
    }

    @Test
    public void equalsAndHashCodeContract() {
        //when + then
        EqualsVerifier.forClass(False.class)
                .usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }

    @Test
    public void toStringVerifier() {
        //when + then
        ToStringVerifier.forClass(False.class)
                .withIgnoredFields("queryContext")
                .verify();
    }
}