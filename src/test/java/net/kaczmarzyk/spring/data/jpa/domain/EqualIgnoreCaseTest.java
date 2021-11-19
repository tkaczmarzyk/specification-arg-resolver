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
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Ricardo Pardinho
 */
public class EqualIgnoreCaseTest extends EqualTest {

    @Test
    public void filtersByStringCaseInsensitive() {
        EqualIgnoreCase<Customer> simpsons = new EqualIgnoreCase<>(queryCtx, "lastName", new String[] { "SIMpsOn" }, defaultConverter);
        List<Customer> simpsonsFound = customerRepo.findAll(simpsons);

        assertThat(simpsonsFound).hasSize(2).containsOnly(homerSimpson, margeSimpson);


        EqualIgnoreCase<Customer> lastNameS = new EqualIgnoreCase<>(queryCtx, "lastName", new String[] { "s" }, defaultConverter);
        List<Customer> found = customerRepo.findAll(lastNameS);

        assertThat(found).isEmpty();


        EqualIgnoreCase<Customer> firstName = new EqualIgnoreCase<>(queryCtx, "firstName", new String[] { "Moe" }, defaultConverter);
        List<Customer> moeFound = customerRepo.findAll(firstName);

        assertThat(moeFound).hasSize(1).containsOnly(moeSzyslak);
    }

    @Test
    public void filtersByEnumCaseInsensitive() {
        EqualIgnoreCase<Customer> simpsons = new EqualIgnoreCase<>(queryCtx, "gender", new String[] { "fEmAlE" }, defaultConverter);
        List<Customer> simpsonsFound = customerRepo.findAll(simpsons);
        assertThat(simpsonsFound).hasSize(1).containsOnly(margeSimpson);


        EqualIgnoreCase<Customer> firstName = new EqualIgnoreCase<>(queryCtx, "gender", new String[] { "mAlE" }, defaultConverter);
        List<Customer> moeFound = customerRepo.findAll(firstName);
        assertThat(moeFound).hasSize(2).containsOnly(homerSimpson, moeSzyslak);
    }

}
