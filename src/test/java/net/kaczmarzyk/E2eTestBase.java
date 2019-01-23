/**
 * Copyright 2014-2019 the original author or authors.
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
import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.IntegrationTestBase;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


@WebAppConfiguration
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
        		.registrationDate(2014, 03, 15)
        		.gender(MALE).street("Evergreen Terrace")
        		.orders("Duff Beer", "Donuts", "Pizza")
        		.badges("Beef Eater", "Hard Drinker")
        		.build(em);
        margeSimpson = customer("Marge", "Simpson").registrationDate(2014, 03, 20).gender(FEMALE).street("Evergreen Terrace").build(em);
        bartSimpson = customer("Bart", "Simpson").nickName("El Barto").registrationDate(2014, 03, 25).gender(MALE).street("Evergreen Terrace").build(em);
        lisaSimpson = customer("Lisa", "Simpson").registrationDate(2014, 03, 30).gender(FEMALE).street("Evergreen Terrace").build(em);
        maggieSimpson = customer("Maggie", "Simpson").registrationDate(2014, 03, 31).gender(FEMALE).street("Evergreen Terrace").build(em);
        moeSzyslak = customer("Moe", "Szyslak")
        		.registrationDate(2014, 03, 15)
        		.gender(MALE).street("Unknown")
        		.orders("Duff Beer")
        		.badges("Suicide Attempt", "Depression", "Troll Face")
        		.build(em);
        nedFlanders = customer("Ned", "Flanders").golden().nickName("Flanders").registrationDate(2014, 03, 25).gender(MALE).street("Evergreen Terrace").orders("Bible").build(em);
        
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }
    
    
}
