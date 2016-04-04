/**
 * Copyright 2014-2016 the original author or authors.
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

import static org.assertj.core.api.Assertions.assertThat;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Tomasz Kaczmarzyk
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class })
@TransactionConfiguration
@Transactional
public abstract class IntegrationTestBase {

	private static final Customer[] EMPTY_SET = new Customer[]{};

	@Rule
    public ExpectedException expectedException = ExpectedException.none();
	
    @Autowired
    protected CustomerRepository customerRepo;
    
    @PersistenceContext
    protected EntityManager em;
    
    protected void assertFilterMembers(Specification<Customer> spec, Customer... expectedMembers) {
    	assertThat(customerRepo.findAll(spec)).hasSize(expectedMembers.length).containsOnly(expectedMembers);
    }
    
    protected void assertFilterEmpty(Specification<Customer> spec) {
    	assertFilterMembers(spec, EMPTY_SET);
    }
}
