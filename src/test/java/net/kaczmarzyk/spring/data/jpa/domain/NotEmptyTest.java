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

import com.jparams.verifier.tostring.ToStringVerifier;
import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.IntegrationTestBase;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static net.kaczmarzyk.spring.data.jpa.OrderBuilder.order;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Hubert Gotfryd (Tratif sp. z o.o.)
 */
public class NotEmptyTest extends IntegrationTestBase {

    private Customer homerSimpson;
    private Customer maggieSimpson;
    private Customer margeSimpson;
    private Customer moeSzyslak;
    private Customer barryBenson;

    @BeforeEach
    public void initData() {
        homerSimpson = customer("Homer", "Simpson").phoneNumbers("123456789").build(em);
        maggieSimpson = customer("Maggie", "Simpson").phoneNumbers("444444444").build(em);
        margeSimpson = customer("Marge", "Simpson").orders(order("ring")).build(em);
        moeSzyslak = customer("Moe", "Szyslak").orders(order("snowboard")).build(em);
        barryBenson = customer("Barry", "Benson").orders(order("ball")).phoneNumbers("222222222").build(em);
    }

    @Test
    public void filtersByNotEmptyOrders_oneToManyAssociation() {
        //given
        NotEmpty<Customer> notEmptyOrders = new NotEmpty<>(queryCtx, "orders", new String[]{"true"}, defaultConverter);

        //when
        List<Customer> result = customerRepo.findAll(notEmptyOrders);

        //then
        assertThat(result)
                .hasSize(3)
                .containsOnly(margeSimpson, moeSzyslak, barryBenson);
    }

    @Test
    public void filtersByNotEmptyPhoneNumbers_elementCollectionWithSimpleNonEntityValues() {
        //given
        NotEmpty<Customer> notEmptyPhoneNumbers = new NotEmpty<>(queryCtx, "phoneNumbers", new String[]{"true"}, defaultConverter);

        //when
        List<Customer> result = customerRepo.findAll(notEmptyPhoneNumbers);

        //then
        assertThat(result)
                .hasSize(3)
                .containsOnly(homerSimpson, maggieSimpson, barryBenson);
    }

    @Test
    public void filtersByEmptyOrders_oneToManyAssociation() {
        //given
        NotEmpty<Customer> emptyOrders = new NotEmpty<>(queryCtx, "orders", new String[]{"false"}, defaultConverter);

        //when
        List<Customer> result = customerRepo.findAll(emptyOrders);

        //then
        assertThat(result)
                .hasSize(2)
                .containsOnly(homerSimpson, maggieSimpson);
    }

    @Test
    public void filtersByEmptyPhoneNumbers_elementCollectionWithSimpleNonEntityValues() {
        //given
        NotEmpty<Customer> emptyPhoneNumbers = new NotEmpty<>(queryCtx, "phoneNumbers", new String[]{"false"}, defaultConverter);

        //when
        List<Customer> result = customerRepo.findAll(emptyPhoneNumbers);

        //then
        assertThat(result)
                .hasSize(2)
                .containsOnly(margeSimpson, moeSzyslak);
    }

    @Test
    public void equalsAndHashCodeContract() {
        //when + then
        EqualsVerifier.forClass(NotEmpty.class)
                .usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }

    @Test
    public void toStringVerifier() {
        //when + then
        ToStringVerifier.forClass(NotEmpty.class)
                .withIgnoredFields("queryContext")
                .verify();
    }
}
