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
package net.kaczmarzyk.spring.data.jpa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * @author Tomasz Kaczmarzyk
 * @author TP Diffenbach
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { ApplicationWithConfiguredCache.class })
@WebAppConfiguration
@Transactional
public abstract class IntegrationTestBaseWithConfiguredCache {

	private static final Customer[] EMPTY_LIST = {};

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Autowired
	protected CustomerRepository customerRepo;

	@PersistenceContext
	protected EntityManager em;

	@Autowired
	WebApplicationContext wac;

	protected MockMvc mockMvc;

	@BeforeEach
	public void setupMockMvc() {
		mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
	}

}
