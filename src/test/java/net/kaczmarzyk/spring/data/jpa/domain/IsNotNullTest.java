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

import java.util.List;

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Hubert Gotfryd (Tratif sp. z o.o.)
 */
public class IsNotNullTest extends IntegrationTestBase {

    private Customer bartSimpson;
    private Customer lisaSimpson;

    @BeforeEach
    public void initData() {
        bartSimpson = customer("Bart", "Simpson")
                .nickName("El Barto")
                .build(em);

        lisaSimpson = customer("Lisa", "Simpson")
                .build(em);
    }

    @Test
    public void findsCustomersWithNotNullField() {
        //given
        IsNotNull<Customer> spec = new IsNotNull<>(queryCtx, "nickName", new String[0]);

        //when
        List<Customer> found = customerRepo.findAll(spec);

        //then
        assertThat(found)
                .hasSize(1)
                .containsOnly(bartSimpson);
    }

    @Test
    public void equalsAndHashCodeContract() {
        //when + then
        EqualsVerifier.forClass(IsNotNull.class)
                .usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }

    @Test
    public void toStringVerifier() {
        //when + then
        ToStringVerifier.forClass(IsNotNull.class)
                .withIgnoredFields("queryContext")
                .verify();
    }
}
