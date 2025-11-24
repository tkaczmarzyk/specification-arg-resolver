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
import net.kaczmarzyk.spring.data.jpa.Gender;
import net.kaczmarzyk.spring.data.jpa.IntegrationTestBase;
import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 * @author Michal Jankowski, Hazecod, cschierle
 *
 */
public class NotInIgnoreCaseTest extends IntegrationTestBase {

    private Customer homerSimpson;
    private Customer margeSimpson;
    private Customer moeSzyslak;
    private Customer joeQuimby;

    @BeforeEach
    public void initData() {
        homerSimpson = customer("Homer", "Simpson").gender(Gender.MALE).registrationDate(2015, 3, 1).weight(121).build(em);
        margeSimpson = customer("Marge", "Simpson").gender(Gender.FEMALE).registrationDate(2015, 3, 1).weight(55).build(em);
        moeSzyslak = customer("Moe", "Szyslak").gender(Gender.MALE).registrationDate(2015, 3, 2).weight(65).build(em);

        joeQuimby = customer("Joe", "Quimby").gender(Gender.OTHER).build(em); // Weight nor Registration Date not specified
    }

    @Test
    public void filtersByEnumValueCaseInsensitive_singleValue() {
        NotInIgnoreCase<Customer> genderMale = notInIgnoreCaseSpec("gender", new String[]{"male"});
        List<Customer> males = customerRepo.findAll(genderMale);
        assertThat(males).hasSize(2).containsOnly(margeSimpson, joeQuimby);

        NotInIgnoreCase<Customer> genderFemale = notInIgnoreCaseSpec("gender", new String[]{"feMALE"});
        List<Customer> females = customerRepo.findAll(genderFemale);
        assertThat(females).hasSize(3).containsOnly(homerSimpson, moeSzyslak, joeQuimby);

        NotInIgnoreCase<Customer> genderOther = notInIgnoreCaseSpec("gender", new String[]{"OtHeR"});
        List<Customer> others = customerRepo.findAll(genderOther);
        assertThat(others).hasSize(3).containsOnly(homerSimpson, margeSimpson, moeSzyslak);
    }

    @Test
    public void filtersByEnumValueCaseInsensitive_multiValue() {
        NotInIgnoreCase<Customer> genderMaleOrFemale = notInIgnoreCaseSpec("gender", new String[]{"male", "feMALE"});
        List<Customer> malesOrFemales = customerRepo.findAll(genderMaleOrFemale);
        assertThat(malesOrFemales).hasSize(1).containsOnly(joeQuimby);
    }

    @Test
    public void filtersByLongValue() {
        NotInIgnoreCase<Customer> simpsonsIds = notInIgnoreCaseSpec("id", new String[]{homerSimpson.getId().toString(), margeSimpson.getId().toString()});

        List<Customer> simpsons = customerRepo.findAll(simpsonsIds);

        assertThat(simpsons).hasSize(2).containsOnly(moeSzyslak, joeQuimby);
    }

    @Test
    public void filtersByLongValue_withAdditionalNonExistingValue() {
        NotInIgnoreCase<Customer> simpsonsIdsWithTrash = notInIgnoreCaseSpec("id", new String[]{"12345", homerSimpson.getId().toString(), margeSimpson.getId().toString()});

        List<Customer> simpsons = customerRepo.findAll(simpsonsIdsWithTrash);

        assertThat(simpsons).hasSize(2).containsOnly(moeSzyslak, joeQuimby);
    }

    @Test
    public void filtersByIntegerValue() {
        NotInIgnoreCase<Customer> weights = notInIgnoreCaseSpec("weight", new String[]{"121", "65"});

        List<Customer> found = customerRepo.findAll(weights);

        assertThat(found).hasSize(1).containsOnly(margeSimpson);
    }

    @Test
    public void filtersByStringCaseInsensitive() {
        NotInIgnoreCase<Customer> lastNameSimpson = notInIgnoreCaseSpec("lastName", "sIMPSOn");
        List<Customer> result = customerRepo.findAll(lastNameSimpson);
        assertThat(result)
                .hasSize(2)
                .containsOnly(moeSzyslak, joeQuimby);

        NotInIgnoreCase<Customer> firstNameWithO = notInIgnoreCaseSpec("firstName", new String[]{"moe", "HOMER"});
        result = customerRepo.findAll(firstNameWithO);
        assertThat(result)
                .hasSize(2)
                .containsOnly(margeSimpson, joeQuimby);
    }

    @Test
    public void filtersByDateWithDefaultDateFormat() {
        NotInIgnoreCase<Customer> registered1stMarch = notInIgnoreCaseSpec("registrationDate", new String[]{"2015-03-02"});
        List<Customer> found = customerRepo.findAll(registered1stMarch);

        assertThat(found).hasSize(2).containsOnly(homerSimpson, margeSimpson);

        NotInIgnoreCase<Customer> registered1stOr2ndMarch = notInIgnoreCaseSpec("registrationDate", new String[]{"2015-03-02", "2015-03-01"});
        found = customerRepo.findAll(registered1stOr2ndMarch);

        assertThat(found).hasSize(0);
    }

    @Test
    public void filterByDateWithCustomDateFormat() {
        InIgnoreCase<Customer> registered1stMarch = notInIgnoreCaseSpec("registrationDate", new String[]{"02-03-2015"},
                Converter.withDateFormat("dd-MM-yyyy", OnTypeMismatch.EMPTY_RESULT, null));

        List<Customer> found = customerRepo.findAll(registered1stMarch);

        assertThat(found).hasSize(2).containsOnly(homerSimpson, margeSimpson);

        NotInIgnoreCase<Customer> registered1stOr2ndMarch = notInIgnoreCaseSpec("registrationDate", new String[]{"01-03-2015", "02-03-2015"},
                Converter.withDateFormat("dd-MM-yyyy", OnTypeMismatch.EMPTY_RESULT, null));

        found = customerRepo.findAll(registered1stOr2ndMarch);

        assertThat(found).hasSize(0);
    }

    @Test
    public void rejectsMissingArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> notInIgnoreCaseSpec("path", new String[]{}));
    }

    @Test
    public void rejectsNullArgumentArray() {
        assertThrows(IllegalArgumentException.class,
                () -> new NotInIgnoreCase<>(queryCtx, "path", null, defaultConverter));
    }

    @Test
    public void equalsAndHashCodeContract() {
        EqualsVerifier.forClass(NotInIgnoreCase.class)
                .usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }

    @Test
    public void toStringVerifier() {
        ToStringVerifier.forClass(NotInIgnoreCase.class)
                .withIgnoredFields("queryContext")
                .verify();
    }

    private <T> NotInIgnoreCase<T> notInIgnoreCaseSpec(String path, Object expectedValue) {
        return notInIgnoreCaseSpec(path, expectedValue, null);
    }

    private <T> NotInIgnoreCase<T> notInIgnoreCaseSpec(String path, Object expectedValue, Converter converter) {
        var effectiveConverter = converter != null ? converter : defaultConverter;
        if (expectedValue instanceof String[]) {
            return new NotInIgnoreCase<>(queryCtx, path, (String[]) expectedValue, effectiveConverter);
        }
        return new NotInIgnoreCase<>(queryCtx, path, new String[]{expectedValue.toString()}, effectiveConverter);
    }
}
