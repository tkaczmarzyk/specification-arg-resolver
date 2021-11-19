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
package net.kaczmarzyk.spring.data.jpa;

import net.kaczmarzyk.spring.data.jpa.utils.Converter;
import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;
import net.kaczmarzyk.spring.data.jpa.web.WebRequestQueryContext;
import net.kaczmarzyk.spring.data.jpa.web.annotation.OnTypeMismatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static net.kaczmarzyk.spring.data.jpa.web.utils.NativeWebRequestBuilder.nativeWebRequest;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Tomasz Kaczmarzyk
 * @author TP Diffenbach
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { Application.class })
@WebAppConfiguration
@Transactional
public abstract class IntegrationTestBase {

	private static final Customer[] EMPTY_LIST = {};

	@Autowired
	protected CustomerRepository customerRepo;

	@PersistenceContext
	protected EntityManager em;

	protected Converter defaultConverter = Converter.withTypeMismatchBehaviour(OnTypeMismatch.EMPTY_RESULT, null);

	protected QueryContext queryCtx = new WebRequestQueryContext(nativeWebRequest().build());

	@Autowired
	WebApplicationContext wac;

	protected MockMvc mockMvc;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@BeforeEach
	public void setupMockMvc() {
		mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
	}

	/**
	 * Call findAll with the Specification, and assert its members match the expectedMembers
	 * @param spec the Specification
	 * @param expectedMembers the expected members after the Specification has filtered.
	 */
	protected void assertFilterMembers(Specification<Customer> spec, Customer... expectedMembers) {
		assertThat(customerRepo.findAll(spec)).hasSize(expectedMembers.length).containsOnly(expectedMembers);
	}

	/**
	 * Call findAll with the Specification, and assert its returns the empty list.
	 * @param spec Specification
	 */
	protected void assertFilterEmpty(Specification<Customer> spec) {
		assertFilterMembers(spec, EMPTY_LIST);
	}

	protected void doInNewTransaction(Runnable callback) {
		TransactionTemplate template = new TransactionTemplate(transactionManager);
		template.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
		template.execute(tx -> {
			callback.run();
			return null;
		});
	}
}
