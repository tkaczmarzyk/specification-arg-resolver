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

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.IntegrationTestBase;
import net.kaczmarzyk.utils.TestLogAppender;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

import static java.time.ZoneOffset.UTC;
import static java.time.ZoneOffset.ofHours;
import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static net.kaczmarzyk.spring.data.jpa.Gender.FEMALE;
import static net.kaczmarzyk.spring.data.jpa.Gender.MALE;


public abstract class E2eTestBase extends IntegrationTestBase {

    protected Customer homerSimpson;
    protected Customer margeSimpson;
    protected Customer bartSimpson;
    protected Customer lisaSimpson;
    protected Customer maggieSimpson;

    protected Customer minnieSzyslak;
    protected Customer moeSzyslak;
    protected Customer nedFlanders;

    @BeforeEach
    public void initialize() {

        homerSimpson = customer("Homer", "Simpson")
                .nickName("Homie")
                .registrationDate(2014, 3, 15)
                .gender(MALE)
                .street("Evergreen Terrace")
                .orders("Duff Beer", "Donuts", "Pizza", "Tomacco")
                .badges("Beef Eater", "Hard Drinker", "Tomacco Eater")
                .birthDate(LocalDate.of(1970, 03, 21))
                .lastOrderTime(LocalDateTime.of(2016, 8, 21, 14, 51,0))
                .nextSpecialOffer(OffsetDateTime.of(2020, 6, 16, 16, 17, 0, 0, ofHours(9)))
                .build(em);
        margeSimpson = customer("Marge", "Simpson").registrationDate(2014, 03, 20)
                .gender(FEMALE)
                .street("Evergreen Terrace")
                .badges("Tomacco Eater")
                .weight(50)
                .occupation("Housewife")
                .birthDate(LocalDate.of(1972, 7, 13))
                .lastOrderTime(LocalDateTime.of(2017, 12, 20, 11, 13,0))
                .nextSpecialOffer(OffsetDateTime.of(2020, 6, 16, 16, 17, 0, 0, ofHours(7)))
                .build(em);
        bartSimpson = customer("Bart", "Simpson").nickName("El Barto")
                .registrationDate(2014, 03, 25)
                .gender(MALE)
                .street("Evergreen Terrace")
                .orders("Tomacco")
                .badges("Tomacco Eater")
                .birthDate(LocalDate.of(1992, 2, 23))
                .lastOrderTime(LocalDateTime.of(2017, 11, 21, 11, 13,01))
                .nextSpecialOffer(OffsetDateTime.of(2020, 7, 16, 16, 17, 0, 0, ofHours(4)))
                .build(em);
        lisaSimpson = customer("Lisa", "Simpson").registrationDate(2014, 03, 30)
                .gender(FEMALE)
                .street("Evergreen Terrace")
                .weight(30)
                .birthDate(LocalDate.of(1994, 11, 7))
                .lastOrderTime(LocalDateTime.of(2017, 8, 22, 9, 18,0))
                .nextSpecialOffer(OffsetDateTime.of(2020, 7, 16, 16, 17, 0, 0, ofHours(3)))
                .build(em);
        maggieSimpson = customer("Maggie", "Simpson").registrationDate(2014, 03, 31)
                .gender(FEMALE)
                .street("Evergreen Terrace")
                .weight(10)
                .birthDate(LocalDate.of(1998, 10, 7))
                .refCode(UUID.fromString("31CFE6A0-7450-48B0-BB0E-5E6CD5071131"))
                .nextSpecialOffer(OffsetDateTime.of(2020, 7, 16, 16, 17, 0, 0, UTC))
                .build(em);
        moeSzyslak = customer("Moe", "Szyslak")
                .registrationDate(2014, 03, 15)
                .gender(MALE)
                .street("Unknown")
                .orders("Duff Beer")
                .badges("Suicide Attempt", "Depression", "Troll Face")
                .birthDate(LocalDate.of(1966, 4, 1))
                .lastOrderTime(LocalDateTime.of(2017, 12, 13, 10, 29, 0))
                .refCode(UUID.fromString("05B79D32-7A97-44D9-9AD7-93FB0CBECC80"))
                .nextSpecialOffer(OffsetDateTime.of(2020, 7, 17, 16, 17, 0, 0, ofHours(4)))
                .build(em);
        minnieSzyslak = customer("Minnie", "Szyslak")
                .registrationDate(2020, 05, 27)
                .gender(FEMALE)
                .nickName("minnie")
                .nextSpecialOffer(OffsetDateTime.of(2020, 7, 18, 16, 17, 0, 0, ofHours(11)))
                .build(em);
        nedFlanders = customer("Ned", "Flanders").golden()
                .nickName("Flanders")
                .registrationDate(2014, 03, 25)
                .gender(MALE)
                .street("Evergreen Terrace")
                .orders("Bible")
                .birthDate(LocalDate.of(1974, 5, 4))
                .refCode(UUID.fromString("63F7714E-594A-44E1-B75B-9D76EA1F42DB"))
                .lastOrderTime(LocalDateTime.of(2016, 10, 17, 18, 29,0))
                .nextSpecialOffer(OffsetDateTime.of(2020, 7, 19, 16, 17, 0, 0, ofHours(4)))
                .build(em);

        TestLogAppender.clearInterceptedLogs();
    }


}
