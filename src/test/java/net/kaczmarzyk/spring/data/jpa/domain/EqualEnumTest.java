/**
 * Copyright 2014-2015 the original author or authors.
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

import java.util.List;

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.Gender;
import net.kaczmarzyk.spring.data.jpa.IntegrationTestBase;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.dao.InvalidDataAccessApiUsageException;


/**
 * @author Maciej Szewczyszyn
 */
public class EqualEnumTest extends IntegrationTestBase {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private Customer homerSimpson;
    private Customer margeSimpson;
    private Customer moeSzyslak;

    @Before
    public void initData() {
        homerSimpson = customer("Homer", "Simpson").gender(Gender.MALE).build(em);
        margeSimpson = customer("Marge", "Simpson").gender(Gender.FEMALE).build(em);
        moeSzyslak = customer("Moe", "Szyslak").gender(Gender.MALE).build(em);

        customer("Joe", "Quimby").build(em); // Gender not specifed
    }

    @Test
    public void filtersGender() {
        EqualEnum<Customer> genderMale = new EqualEnum<>("gender", "MALE");
        List<Customer> males = customerRepo.findAll(genderMale);
        assertThat(males).hasSize(2).containsOnly(homerSimpson, moeSzyslak);

        EqualEnum<Customer> genderFemale = new EqualEnum<>("gender", "FEMALE");
        List<Customer> females = customerRepo.findAll(genderFemale);
        assertThat(females).hasSize(1).containsOnly(margeSimpson);

        EqualEnum<Customer> genderOther = new EqualEnum<>("gender", "OTHER");
        List<Customer> others = customerRepo.findAll(genderOther);
        assertThat(others).hasSize(0);
    }

    @Test
    public void filtersWithTwoGenders() {
        EqualEnum<Customer> genderMaleOrFemale = new EqualEnum<>("gender", new String[] { "MALE", "FEMALE" });
        List<Customer> malesOrFemales = customerRepo.findAll(genderMaleOrFemale);
        assertThat(malesOrFemales).hasSize(3).containsOnly(homerSimpson, margeSimpson, moeSzyslak);
    }

    @Test
    public void rejectsNotExistingEnumConstantName_singleNotExisting() {
        EqualEnum<Customer> genderRobot = new EqualEnum<>("gender", "ROBOT");
        expectedException.expect(InvalidDataAccessApiUsageException.class);
        expectedException.expectCause(CoreMatchers.<IllegalArgumentException> instanceOf(IllegalArgumentException.class));
        expectedException.expectMessage("The following enum constants do not exists: ROBOT");
        customerRepo.findAll(genderRobot);
    }

    @Test
    public void rejectsNotExistingEnumConstantName_twoExistingTwoNot() {
        EqualEnum<Customer> genderRobot = new EqualEnum<>("gender", new String[] { "MALE", "ROBOT", "FEMALE", "ALIEN" });
        expectedException.expect(InvalidDataAccessApiUsageException.class);
        expectedException.expectCause(CoreMatchers.<IllegalArgumentException> instanceOf(IllegalArgumentException.class));
        expectedException.expectMessage("The following enum constants do not exists: ROBOT, ALIEN");
        customerRepo.findAll(genderRobot);
    }

    @Test
    public void rejectsNotEnumType() {
        EqualEnum<Customer> firstNameMale = new EqualEnum<>("firstName", "MALE");
        expectedException.expect(InvalidDataAccessApiUsageException.class);
        expectedException.expectCause(CoreMatchers.<IllegalArgumentException> instanceOf(IllegalArgumentException.class));
        expectedException.expectMessage("Type of field with path firstName is not enum!");
        customerRepo.findAll(firstNameMale);
    }

}
