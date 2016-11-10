/**
 * Copyright 2014-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package net.kaczmarzyk.spring.data.jpa.domain;

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.IntegrationTestBase;


/**
 * @author Matt S.Y. Ho
 */
public class EndingWithTest extends IntegrationTestBase {

  Customer homerSimpson;
  Customer margeSimpson;
  Customer moeSzyslak;

  @Before
  public void initData() {
    homerSimpson = customer("Homer", "Simpson").street("Evergreen Terrace").build(em);
    margeSimpson = customer("Marge", "Simpson").street("Evergreen Terrace").build(em);
    moeSzyslak = customer("Moe", "Szyslak").street("Unknown").build(em);
  }

  @Test
  public void filtersByFirstLevelProperty() {
    EndingWith<Customer> lastNameSimpson = new EndingWith<>("lastName", "Simpson");
    List<Customer> result = customerRepo.findAll(lastNameSimpson);
    assertThat(result).hasSize(2).containsOnly(homerSimpson, margeSimpson);

    EndingWith<Customer> firstNameWithO = new EndingWith<>("firstName", "er");
    result = customerRepo.findAll(firstNameWithO);
    assertThat(result).hasSize(1).containsOnly(homerSimpson);
  }

  @Test
  public void filtersByNestedProperty() {
    EndingWith<Customer> streetWithEvergreen = new EndingWith<>("address.street", "Terrace");
    List<Customer> result = customerRepo.findAll(streetWithEvergreen);
    assertThat(result).hasSize(2).containsOnly(homerSimpson, margeSimpson);

    EndingWith<Customer> streetWithSpaceEvergreen =
        new EndingWith<>("address.street", "Evergreen");
    result = customerRepo.findAll(streetWithSpaceEvergreen);
    assertThat(result).hasSize(0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void rejectsMissingArgument() {
    new EndingWith<>("path", new String[] {});
  }

  @Test(expected = IllegalArgumentException.class)
  public void rejectsInvalidNumberOfArguments() {
    new EndingWith<>("path", new String[] {"a", "b"});
  }
}
