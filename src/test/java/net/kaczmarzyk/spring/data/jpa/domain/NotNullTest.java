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

import java.text.ParseException;
import java.util.List;

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;


/**
 * @author Steven McLaughlin
 */
public class NotNullTest extends IntegrationTestBase {

    private static final String INVALID_PARAMETER_ARRAY_SIZE_EXCEPTION_MESSAGE = "Invalid size of 'httpParamValues' array, Expected 1 but was ";

    Customer bartSimpson;
    Customer lisaSimpson;

    @Before
    public void initData() {
        bartSimpson = customer("Bart", "Simpson").nickName("El Barto").street("Evergreen Terrace").build(em);
        lisaSimpson = customer("Lisa", "Simpson").registrationDate(2014, 03, 20).street("Evergreen Terrace").build(em);
    }

    @Test
    public void findsCustomersWithNotNullField() {
        NotNull<Customer> spec = new NotNull<>(queryCtx, "nickName", new String[]{"true"}, defaultConverter);

        List<Customer> found = customerRepo.findAll(spec);

        assertThat(found).hasSize(1).containsOnly(bartSimpson);
    }

    @Test
    public void findsCustomersWithNullField() {
        NotNull<Customer> spec = new NotNull<>(queryCtx, "nickName", new String[]{"false"}, defaultConverter);

        List<Customer> found = customerRepo.findAll(spec);

        assertThat(found).hasSize(1).containsOnly(lisaSimpson);
    }

    @Test
    public void rejectsNullArgumentArray() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new NotNull<>(queryCtx, "path", null, defaultConverter));

        assertThat(exception.getMessage())
                .isEqualTo(INVALID_PARAMETER_ARRAY_SIZE_EXCEPTION_MESSAGE + "null");
    }

    @Test
    public void rejectsMissingArguments() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new NotNull<>(queryCtx, "path", new String[] {}, defaultConverter));

        assertThat(exception.getMessage())
                .isEqualTo(INVALID_PARAMETER_ARRAY_SIZE_EXCEPTION_MESSAGE + "[]");
    }

    @Test
    public void rejectsTooManyArguments() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new NotNull<>(queryCtx, "path", new String[] {"2014-03-10", "2014-03-11"}, defaultConverter));

        assertThat(exception.getMessage())
                .isEqualTo(INVALID_PARAMETER_ARRAY_SIZE_EXCEPTION_MESSAGE + "[2014-03-10, 2014-03-11]");
    }

    @Test
    public void equalsAndHashCodeContract() {
        EqualsVerifier.forClass(NotNull.class)
                .usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }

    @Test
    public void toStringVerifier() {
        ToStringVerifier.forClass(NotNull.class)
                .withIgnoredFields("queryContext")
                .verify();
    }
}
