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
import net.kaczmarzyk.spring.data.jpa.IntegrationTestBase;
import net.kaczmarzyk.spring.data.jpa.ItemTag;
import net.kaczmarzyk.spring.data.jpa.Order;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Sort;

import javax.persistence.criteria.JoinType;
import java.util.List;

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static net.kaczmarzyk.spring.data.jpa.ItemTagBuilder.itemTag;
import static net.kaczmarzyk.spring.data.jpa.OrderBuilder.order;
import static net.kaczmarzyk.spring.data.jpa.utils.ThrowableAssertions.assertThrows;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Tomasz Kaczmarzyk
 */
public class JoinFetchTest extends IntegrationTestBase {

    Customer homerSimpson;
    Customer margeSimpson;
    Customer bartSimpson;
    
    @BeforeEach
    public void initData() {
        ItemTag books = itemTag("Books").build(em);

        homerSimpson = customer("Homer", "Simpson")
                .orders("Duff Beer", "Donuts")
                .build(em);
        margeSimpson = customer("Marge", "Simpson")
                .build(em);
        bartSimpson = customer("Bart", "Simpson")
                .orders(order("Comic Books").withTags(books))
                .build(em);
        
        em.flush();
        em.clear();
    }
    
    @Test
    public void fetchesLazyCollection() {
        JoinFetch<Customer> spec = new JoinFetch<Customer>(queryCtx, new String[] { "orders" }, "o", JoinType.LEFT, true);
        
        List<Customer> customers = customerRepo.findAll(spec, Sort.by("id"));

        assertThat(customers)
                .extracting(Customer::getFirstName)
                .containsExactly("Homer", "Marge", "Bart");
        
        for (Customer customer : customers) {
            assertTrue(Hibernate.isInitialized(customer.getOrders()));
        }
    }

    @Test
    public void performsTwoFetchesUsingSingleLeftJoinFetchDefinition() {
        JoinFetch<Customer> joinFetch = new JoinFetch<Customer>(queryCtx, new String[] { "orders", "orders2" }, JoinType.LEFT, true);

        List<Customer> customers = customerRepo.findAll(joinFetch, Sort.by("id"));

        assertThat(customers)
                .extracting(Customer::getFirstName)
                .containsExactly("Homer", "Marge", "Bart");

        for (Customer customer : customers) {
            assertTrue(Hibernate.isInitialized(customer.getOrders()));
            assertTrue(Hibernate.isInitialized(customer.getOrders2()));
        }
    }

    @Test
    public void performsTwoFetchesUsingSingleInnerJoinFetchDefinition() {
        JoinFetch<Customer> joinFetch = new JoinFetch<Customer>(queryCtx, new String[] { "orders", "orders2" }, JoinType.INNER, true);

        List<Customer> customers = customerRepo.findAll(joinFetch, Sort.by("id"));

        assertThat(customers)
                .extracting(Customer::getFirstName)
                .containsExactly("Homer", "Bart");

        for (Customer customer : customers) {
            assertTrue(Hibernate.isInitialized(customer.getOrders()));
            assertTrue(Hibernate.isInitialized(customer.getOrders2()));
        }
    }

    @Test
    public void throwsIllegalArgumentExceptionWhenAliasAndMultiplePathsWerePassedToConstructor() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new JoinFetch<Customer>(queryCtx, new String[] { "orders", "orders2" }, "alias", JoinType.INNER, true),
                "Join fetch alias can be defined only for join fetch with a single path! " +
                        "Remove alias from the annotation or repeat @JoinFetch annotation for every path and use unique alias for each join."
        );
    }


    @Test
    public void performsTwoFetchesUsingTwoJoinFetchDefinition() {
    	JoinFetch<Customer> spec1 = new JoinFetch<Customer>(queryCtx, new String[] { "orders" }, "o", JoinType.LEFT, true);
    	JoinFetch<Customer> spec2 = new JoinFetch<Customer>(queryCtx, new String[] { "orders2" }, "o", JoinType.INNER, true);
        
    	Conjunction<Customer> spec = new Conjunction<Customer>(spec1, spec2);
    	
        List<Customer> customers = customerRepo.findAll(spec, Sort.by("id"));

        assertThat(customers)
                .extracting(Customer::getFirstName)
                .containsExactly("Homer", "Bart");
        
        for (Customer customer : customers) {
        	assertTrue(Hibernate.isInitialized(customer.getOrders()));
        	assertTrue(Hibernate.isInitialized(customer.getOrders2()));
        }
    }

    @Test
    public void performsMultilevelJoinFetchOfTypeLeft() {
        JoinFetch<Customer> orders = new JoinFetch<Customer>(queryCtx, new String[] { "orders" }, "o", JoinType.LEFT, true);
        JoinFetch<Customer> tags = new JoinFetch<Customer>(queryCtx, new String[] { "o.tags" }, JoinType.LEFT, true);

        Conjunction<Customer> spec = new Conjunction<Customer>(orders, tags);

        List<Customer> customers = customerRepo.findAll(spec, Sort.by("id"));

        assertThat(customers)
                .extracting(Customer::getFirstName)
                .containsExactly("Homer", "Marge", "Bart");

        for (Customer customer : customers) {
            assertTrue(Hibernate.isInitialized(customer.getOrders()));
            for(Order order: customer.getOrders()) {
                assertTrue(Hibernate.isInitialized(order.getTags()));
            }
        }
    }

    @Test
    public void performsMultilevelJoinFetchOfTypeLeftAndInner() {
        JoinFetch<Customer> orders = new JoinFetch<Customer>(queryCtx, new String[] { "orders" }, "o", JoinType.LEFT, true);
        JoinFetch<Customer> tags = new JoinFetch<Customer>(queryCtx, new String[] { "o.tags" }, JoinType.INNER, true);

        Conjunction<Customer> spec = new Conjunction<Customer>(orders, tags);

        List<Customer> customers = customerRepo.findAll(spec);

        assertThat(customers)
                .extracting(Customer::getFirstName)
                .containsExactly("Bart");

        for (Customer customer : customers) {
            assertTrue(Hibernate.isInitialized(customer.getOrders()));
            for(Order order: customer.getOrders()) {
                assertTrue(Hibernate.isInitialized(order.getTags()));
            }
        }
    }

    @Test
    public void performsMultilevelJoinFetchOfTypeInnerAndLeft() {
        JoinFetch<Customer> orders = new JoinFetch<Customer>(queryCtx, new String[] { "orders" }, "o", JoinType.INNER, true);
        JoinFetch<Customer> tags = new JoinFetch<Customer>(queryCtx, new String[] { "o.tags" }, JoinType.LEFT, true);

        Conjunction<Customer> spec = new Conjunction<Customer>(orders, tags);

        List<Customer> customers = customerRepo.findAll(spec);

        assertThat(customers)
                .extracting(Customer::getFirstName)
                .containsExactly("Homer", "Bart");

        for (Customer customer : customers) {
            assertTrue(Hibernate.isInitialized(customer.getOrders()));
            for(Order order: customer.getOrders()) {
                assertTrue(Hibernate.isInitialized(order.getTags()));
            }
        }
    }

    @Test
    public void performsMultilevelJoinFetchOfTypeInner() {
        JoinFetch<Customer> orders = new JoinFetch<Customer>(queryCtx, new String[] { "orders" }, "o", JoinType.INNER, true);
        JoinFetch<Customer> tags = new JoinFetch<Customer>(queryCtx, new String[] { "o.tags" }, JoinType.INNER, true);

        Conjunction<Customer> spec = new Conjunction<Customer>(orders, tags);

        List<Customer> customers = customerRepo.findAll(spec);

        assertThat(customers)
                .extracting(Customer::getFirstName)
                .containsExactly("Bart");

        for (Customer customer : customers) {
            assertTrue(Hibernate.isInitialized(customer.getOrders()));
            for(Order order: customer.getOrders()) {
                assertTrue(Hibernate.isInitialized(order.getTags()));
            }
        }
    }

    @Test
    public void performsMultilevelFetchWithAttributeOfTypeSet() {
        JoinFetch<Customer> orders = new JoinFetch<Customer>(queryCtx, new String[] { "orders" }, "o", JoinType.LEFT, true);
        JoinFetch<Customer> tags = new JoinFetch<Customer>(queryCtx, new String[] { "o.tags" }, JoinType.LEFT, true);

        Conjunction<Customer> spec = new Conjunction<Customer>(orders, tags);

        List<Customer> customers = customerRepo.findAll(spec, Sort.by("id"));

        assertThat(customers)
                .extracting(Customer::getFirstName)
                .containsExactly("Homer", "Marge", "Bart");

        for (Customer customer : customers) {
            assertTrue(Hibernate.isInitialized(customer.getOrders()));
            for(Order order: customer.getOrders()) {
                assertTrue(Hibernate.isInitialized(order.getTags()));
                assertFalse(Hibernate.isInitialized(order.getTagsCollection()));
                assertFalse(Hibernate.isInitialized(order.getTagsList()));
            }
        }
    }

    @Test
    public void performsMultilevelFetchWithAttributeOfTypeList() {
        JoinFetch<Customer> orders = new JoinFetch<Customer>(queryCtx, new String[] { "orders" }, "o", JoinType.LEFT, true);
        JoinFetch<Customer> tags = new JoinFetch<Customer>(queryCtx, new String[] { "o.tagsList" }, JoinType.LEFT, true);

        Conjunction<Customer> spec = new Conjunction<Customer>(orders, tags);

        List<Customer> customers = customerRepo.findAll(spec, Sort.by("id"));

        assertThat(customers)
                .extracting(Customer::getFirstName)
                .containsExactly("Homer", "Marge", "Bart");

        for (Customer customer : customers) {
            assertTrue(Hibernate.isInitialized(customer.getOrders()));
            for(Order order: customer.getOrders()) {
                assertTrue(Hibernate.isInitialized(order.getTagsList()));
                assertFalse(Hibernate.isInitialized(order.getTagsCollection()));
                assertFalse(Hibernate.isInitialized(order.getTags()));
            }
        }
    }

    @Test
    public void performsMultilevelFetchWithAttributeOfTypeCollection() {
        JoinFetch<Customer> orders = new JoinFetch<Customer>(queryCtx, new String[] { "orders" }, "o", JoinType.LEFT, true);
        JoinFetch<Customer> tags = new JoinFetch<Customer>(queryCtx, new String[] { "o.tagsCollection" }, JoinType.LEFT, true);

        Conjunction<Customer> spec = new Conjunction<Customer>(orders, tags);

        List<Customer> customers = customerRepo.findAll(spec, Sort.by("id"));

        assertThat(customers)
                .extracting(Customer::getFirstName)
                .containsExactly("Homer", "Marge", "Bart");

        for (Customer customer : customers) {
            assertTrue(Hibernate.isInitialized(customer.getOrders()));
            for(Order order: customer.getOrders()) {
                assertTrue(Hibernate.isInitialized(order.getTagsCollection()));
                assertFalse(Hibernate.isInitialized(order.getTagsList()));
                assertFalse(Hibernate.isInitialized(order.getTags()));
            }
        }
    }

    @Test
    public void performsMultilevelFetchWithSimpleEntityAttribute() {
        JoinFetch<Customer> orders = new JoinFetch<Customer>(queryCtx, new String[] { "orders" }, "o", JoinType.LEFT, true);
        JoinFetch<Customer> tags = new JoinFetch<Customer>(queryCtx, new String[] { "o.note" }, JoinType.LEFT, true);

        Conjunction<Customer> spec = new Conjunction<Customer>(orders, tags);

        List<Customer> customers = customerRepo.findAll(spec, Sort.by("id"));

        assertThat(customers)
                .extracting(Customer::getFirstName)
                .containsExactly("Homer", "Marge", "Bart");

        for (Customer customer : customers) {
            assertTrue(Hibernate.isInitialized(customer.getOrders()));
            for(Order order: customer.getOrders()) {
                assertTrue(Hibernate.isInitialized(order.getNote()));
            }
        }
    }

    @Test
    public void throwsIllegalArgumentExceptionWhenFetchJoinsAreDefinedInInvalidOrder() {
        JoinFetch<Customer> tags = new JoinFetch<Customer>(queryCtx, new String[] { "o.tags" }, JoinType.LEFT, true);
        JoinFetch<Customer> orders = new JoinFetch<Customer>(queryCtx, new String[] { "orders" }, "o", JoinType.LEFT, true);

        Conjunction<Customer> spec = new Conjunction<Customer>(tags, orders);

        assertThrows(
                InvalidDataAccessApiUsageException.class,
                () -> customerRepo.findAll(spec),
                "Join fetch definition with alias: 'o' not found! " +
                        "Make sure that join with the alias 'o' is defined before the join with path: 'o.tags'; " +
                        "nested exception is java.lang.IllegalArgumentException: " +
                        "Join fetch definition with alias: 'o' not found! Make sure that join with the alias 'o' is defined before the join with path: 'o.tags'"
        );
    }

    @Test
    public void performsNotDistinctFetchWhenDistinctParamIsSetToFalse() {
        JoinFetch<Customer> spec = new JoinFetch<Customer>(queryCtx, new String[] { "orders" }, "o", JoinType.LEFT, false);
    
        List<Customer> customers = customerRepo.findAll(spec);
        
        assertThat(customers)
                .hasSize(4)
                .extracting(Customer::getFirstName)
                .containsExactly("Homer", "Homer", "Marge", "Bart");
    }
    
    @Test
    public void performsDistinctFetchWhenDistinctParamIsSetToTrue() {
        JoinFetch<Customer> spec = new JoinFetch<Customer>(queryCtx, new String[] { "orders" }, "o", JoinType.LEFT, true);
    
        List<Customer> customers = customerRepo.findAll(spec);
    
        assertThat(customers)
                .hasSize(3)
                .extracting(Customer::getFirstName)
                .containsExactlyInAnyOrder("Bart", "Homer", "Marge");
    }

}
