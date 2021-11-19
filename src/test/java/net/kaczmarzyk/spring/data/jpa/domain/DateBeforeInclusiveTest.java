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

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.IntegrationTestBase;
import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.List;

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Kamil Sutkowski
 */
public class DateBeforeInclusiveTest extends IntegrationTestBase {

    Customer homerSimpson;
    Customer margeSimpson;
    Customer moeSzyslak;

    @BeforeEach
    public void initData() {
        homerSimpson = customer("Homer", "Simpson").registrationDate(2014, 03, 07).build(em);
        margeSimpson = customer("Marge", "Simpson").registrationDate(2014, 03, 12).build(em);
        moeSzyslak = customer("Moe", "Szyslak").registrationDate(2014, 03, 18).build(em);
    }

    @Test
    public void filtersByRegistrationDateWithDefaultDateFormat() throws ParseException {
        DateBeforeInclusive<Customer> before12th = new DateBeforeInclusive<>(queryCtx ,"registrationDate", new String[] { "2014-03-12" }, defaultConverter);

        List<Customer> result = customerRepo.findAll(before12th);
        assertThat(result)
                .hasSize(2)
                .containsOnly(homerSimpson, margeSimpson);

        DateBeforeInclusive<Customer> before11th = new DateBeforeInclusive<>(queryCtx, "registrationDate", new String[] { "2014-03-11" }, defaultConverter);

        result = customerRepo.findAll(before11th);
        assertThat(result)
                .hasSize(1)
                .containsOnly(homerSimpson);
    }

    @Test
    public void filtersByRegistrationDateWithCustomDateFormat() throws ParseException {
        DateBeforeInclusive<Customer> before12th = new DateBeforeInclusive<>(queryCtx, "registrationDate", new String[]{"12-03-2014"},
        		Converter.withDateFormat("dd-MM-yyyy", OnTypeMismatch.EMPTY_RESULT, null));

        List<Customer> result = customerRepo.findAll(before12th);
        assertThat(result)
                .hasSize(2)
                .containsOnly(homerSimpson, margeSimpson);
    }

    @Test
    public void rejectsInvalidNumberOfArguments() throws ParseException {
        assertThrows(IllegalArgumentException.class,
                () -> new DateBeforeInclusive<>(queryCtx, "path", new String[] { "2014-03-10", "2014-03-11" }, defaultConverter));
    }

    @Test
    public void rejectsMissingArgument() throws ParseException {
        assertThrows(IllegalArgumentException.class, () -> new DateBeforeInclusive<>(queryCtx, "path", new String[]{}, defaultConverter));
    }
}
