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
package net.kaczmarzyk.spring.data.jpa.domain;

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import java.util.List;

import javax.persistence.criteria.JoinType;

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.IntegrationTestBase;

import org.hibernate.Hibernate;
import org.junit.Before;
import org.junit.Test;


/**
 * @author Tomasz Kaczmarzyk
 */
public class JoinFetchTest extends IntegrationTestBase {

    Customer homerSimpson;
    Customer margeSimpson;
    Customer bartSimpson;
    
    @Before
    public void initData() {
        homerSimpson = customer("Homer", "Simpson")
                .orders("Duff Beer", "Donuts")
                .build(em);
        margeSimpson = customer("Marge", "Simpson")
                .build(em);
        bartSimpson = customer("Bart", "Simpson")
                .orders("Skateboard", "Comic Books")
                .build(em);
        
        em.flush();
        em.clear();
    }
    
    @Test
    public void fetchesLazyCollection() {
        JoinFetch<Customer> spec = new JoinFetch<Customer>(new String[] { "orders" }, JoinType.LEFT);
        
        List<Customer> customers = customerRepo.findAll(spec);
        
        assertThat(customers).isNotEmpty();
        
        for (Customer customer : customers) {
            assertTrue(Hibernate.isInitialized(customer.getOrders()));
        }
    }
    
    @Test
    public void performsTwoFetches() {
    	JoinFetch<Customer> spec1 = new JoinFetch<Customer>(new String[] { "orders" }, JoinType.LEFT);
    	JoinFetch<Customer> spec2 = new JoinFetch<Customer>(new String[] { "orders2" }, JoinType.INNER);
        
    	Conjunction<Customer> spec = new Conjunction<Customer>(spec1, spec2);
    	
        List<Customer> customers = customerRepo.findAll(spec);
        
        assertThat(customers).isNotEmpty();
        
        for (Customer customer : customers) {
        	assertTrue(Hibernate.isInitialized(customer.getOrders()));
        	assertTrue(Hibernate.isInitialized(customer.getOrders2()));
        }
    }

}
