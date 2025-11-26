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

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.text.ParseException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.IntegrationTestBase;
import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch;


/**
 * (In 3.0 DateAfterInclusive was removed in favour of GreaterThanOrEqual)
 * 
 * @author Kamil Sutkowski
 */
public class DateAfterInclusiveTest extends IntegrationTestBase {

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
        GreaterThanOrEqual<Customer> after13th = new GreaterThanOrEqual<>(queryCtx, "registrationDate", new String[] { "2014-03-13" }, defaultConverter);

        List<Customer> result = customerRepo.findAll(after13th);
        assertThat(result)
                .hasSize(1)
                .containsOnly(moeSzyslak);

        GreaterThanOrEqual<Customer> after12th = new GreaterThanOrEqual<>(queryCtx, "registrationDate", new String[] { "2014-03-12" }, defaultConverter);

        result = customerRepo.findAll(after12th);
        assertThat(result)
                .hasSize(2)
                .containsOnly(moeSzyslak, margeSimpson);

        GreaterThanOrEqual<Customer> after10th = new GreaterThanOrEqual<>(queryCtx, "registrationDate", new String[] { "2014-03-10" }, defaultConverter);

        result = customerRepo.findAll(after10th);
        assertThat(result)
                .hasSize(2)
                .containsOnly(margeSimpson, moeSzyslak);
    }

    @Test
    public void filtersByRegistrationDateWithCustomDateFormat() throws ParseException {
        GreaterThanOrEqual<Customer> after13th = new GreaterThanOrEqual<>(queryCtx, "registrationDate", new String[] { "13-03-2014" },
        		Converter.withDateFormat("dd-MM-yyyy", OnTypeMismatch.EMPTY_RESULT, null));

        List<Customer> result = customerRepo.findAll(after13th);
        assertThat(result)
                .hasSize(1)
                .containsOnly(moeSzyslak);
    }

    @Test
    public void rejectsInvalidNumberOfArguments() throws ParseException {
        assertThatThrownBy(() -> new GreaterThanOrEqual<>(queryCtx, "path", new String[] {"2014-03-10", "2014-03-11"}, defaultConverter))
        		.isInstanceOf(IllegalArgumentException.class);;
    }

    @Test
    public void rejectsMissingArgument() throws ParseException {
        assertThatThrownBy(() -> new GreaterThanOrEqual<>(queryCtx, "path", new String[] {}, defaultConverter))
        		.isInstanceOf(IllegalArgumentException.class);;
    }
}
