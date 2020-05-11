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
package net.kaczmarzyk;

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static net.kaczmarzyk.spring.data.jpa.Gender.FEMALE;
import static net.kaczmarzyk.spring.data.jpa.Gender.MALE;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.IntegrationTestBase;
import net.kaczmarzyk.utils.TestLogAppender;


public abstract class E2eTestBase extends IntegrationTestBase {

    protected Customer homerSimpson;
    protected Customer margeSimpson;
    protected Customer bartSimpson;
    protected Customer lisaSimpson;
    protected Customer maggieSimpson;

    protected Customer moeSzyslak;
    protected Customer nedFlanders;

    @Autowired
    WebApplicationContext wac;

    protected MockMvc mockMvc;

    @Before
    public void initialize() {
        homerSimpson = customer("Homer", "Simpson")
                .nickName("Homie")
                .registrationDate(2014, 3, 15)
                .gender(MALE)
                .street("Evergreen Terrace")
                .orders("Duff Beer", "Donuts", "Pizza")
                .badges("Beef Eater", "Hard Drinker")
                .birthDate(LocalDate.of(1970, 03, 21))
                .lastOrderTime(LocalDateTime.of(2016, 8, 21, 14, 51,0))
                .build(em);
        margeSimpson = customer("Marge", "Simpson").registrationDate(2014, 03, 20)
                .gender(FEMALE)
                .street("Evergreen Terrace")
                .birthDate(LocalDate.of(1972, 7, 13))
                .lastOrderTime(LocalDateTime.of(2017, 12, 20, 11, 13,0))
                .build(em);
        bartSimpson = customer("Bart", "Simpson").nickName("El Barto")
                .registrationDate(2014, 03, 25)
                .gender(MALE)
                .street("Evergreen Terrace")
                .birthDate(LocalDate.of(1992, 2, 23))
                .lastOrderTime(LocalDateTime.of(2017, 11, 21, 11, 13,01))
                .build(em);
        lisaSimpson = customer("Lisa", "Simpson").registrationDate(2014, 03, 30)
                .gender(FEMALE)
                .street("Evergreen Terrace")
                .birthDate(LocalDate.of(1994, 11, 7))
                .lastOrderTime(LocalDateTime.of(2017, 8, 22, 9, 18,0))
                .build(em);
        maggieSimpson = customer("Maggie", "Simpson").registrationDate(2014, 03, 31)
                .gender(FEMALE)
                .street("Evergreen Terrace")
                .birthDate(LocalDate.of(1998, 10, 7))
                .build(em);
        moeSzyslak = customer("Moe", "Szyslak")
                .registrationDate(2014, 03, 15)
                .gender(MALE)
                .street("Unknown")
                .orders("Duff Beer")
                .badges("Suicide Attempt", "Depression", "Troll Face")
                .birthDate(LocalDate.of(1966, 4, 1))
                .lastOrderTime(LocalDateTime.of(2017, 12, 13, 10, 29,0))
                .build(em);
        nedFlanders = customer("Ned", "Flanders").golden()
                .nickName("Flanders")
                .registrationDate(2014, 03, 25)
                .gender(MALE)
                .street("Evergreen Terrace")
                .orders("Bible")
                .birthDate(LocalDate.of(1974, 5, 4))
                .lastOrderTime(LocalDateTime.of(2016, 10, 17, 18, 29,0))
                .build(em);

        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        
        TestLogAppender.clearInterceptedLogs();
    }


}
