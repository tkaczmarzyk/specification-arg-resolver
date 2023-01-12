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
public class IsNotMemberTest extends IntegrationTestBase {

    private Customer homerSimpson;
    private Customer maggieSimpson;
    private Customer margeSimpson;
    private Customer moeSzyslak;
    private Customer barryBenson;

    @Before
    public void initData() {
        homerSimpson = customer("Homer", "Simpson")
                .phoneNumbers("123456789")
                .luckyNumbers(8L)
                .build(em);

        maggieSimpson = customer("Maggie", "Simpson")
                .phoneNumbers("444444444")
                .luckyNumbers(1L)
                .build(em);

        margeSimpson = customer("Marge", "Simpson")
                .phoneNumbers("123456789")
                .luckyNumbers(1L, 8L)
                .build(em);

        moeSzyslak = customer("Moe", "Szyslak")
                .phoneNumbers("222222222")
                .luckyNumbers(25L, 45L)
                .build(em);

        barryBenson = customer("Barry", "Benson")
                .build(em);
    }

    @Test
    public void filtersByNotMemberOfStringCollectionIncludingResultsWithEmptyCollections() {
        //given
        IsNotMember<Customer> isNotMember = new IsNotMember<>(queryCtx, "phoneNumbers", new String[] {"123456789"}, defaultConverter);

        //when
        List<Customer> result = customerRepo.findAll(isNotMember);

        //then
        assertThat(result)
                .hasSize(3)
                .containsOnly(maggieSimpson, moeSzyslak, barryBenson);
    }

    @Test
    public void filtersByNotMemberOfLongCollectionIncludingResultsWithEmptyCollections() {
        //given
        IsNotMember<Customer> isNotMember = new IsNotMember<>(queryCtx, "luckyNumbers", new String[] {"1"}, defaultConverter);

        //when
        List<Customer> result = customerRepo.findAll(isNotMember);

        //then
        assertThat(result)
                .hasSize(3)
                .containsOnly(homerSimpson, moeSzyslak, barryBenson);
    }

    @Test
    public void equalsAndHashCodeContract() {
        //when + then
        EqualsVerifier.forClass(IsNotMember.class)
                .usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }

    @Test
    public void toStringVerifier() {
        //when + then
        ToStringVerifier.forClass(IsNotMember.class)
                .withIgnoredFields("queryContext")
                .verify();
    }
}