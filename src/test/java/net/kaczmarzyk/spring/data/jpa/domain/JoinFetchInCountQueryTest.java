/**
 * Copyright 2014-2022 the original author or authors.
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
import static net.kaczmarzyk.spring.data.jpa.ItemTagBuilder.itemTag;
import static net.kaczmarzyk.spring.data.jpa.OrderBuilder.order;
import static net.kaczmarzyk.utils.LoggedQueryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import javax.persistence.criteria.JoinType;

import org.hibernate.Hibernate;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.IntegrationTestBase;
import net.kaczmarzyk.spring.data.jpa.ItemTag;
import net.kaczmarzyk.spring.data.jpa.Order;
import net.kaczmarzyk.utils.TestLogAppender;


/**
 * See https://github.com/tkaczmarzyk/specification-arg-resolver/issues/138
 * 
 * @author Tomasz Kaczmarzyk
 */
public class JoinFetchInCountQueryTest extends IntegrationTestBase {

    private static final String BLACK_FRIDAY_TAG = "BlackFridayOffer";
    
	Customer homerSimpson;
    Customer margeSimpson;
    Customer bartSimpson;
    
    @Before
    public void initData() {
        ItemTag books = itemTag("Books").build(em);

        homerSimpson = customer("Homer", "Simpson")
                .orders("Duff Beer", "Donuts")
                .orders(order("Duff MegaPack").withTags(BLACK_FRIDAY_TAG))
                .build(em);
        margeSimpson = customer("Marge", "Simpson")
                .build(em);
        bartSimpson = customer("Bart", "Simpson")
                .orders(order("Comic Books").withTags(books))
                .build(em);
        
        em.flush();
        em.clear();
        
        TestLogAppender.clearInterceptedLogs();
    }
    
    @Test
    public void doesNotJoinLazyCollectionWhenExecutedInContextOfACountQueryAndNoFilteringOnFetchedPart() {
    	JoinFetch<Customer> spec = new JoinFetch<Customer>(queryCtx, new String[] { "orders" }, "", JoinType.LEFT, true); // empty alias = for sure no filtering
        
        Number customerCount = customerRepo.count(spec);

        assertThat(customerCount.intValue())
                .isEqualTo(3);
    	
    	assertThat()
			.theOnlyOneQueryThatWasExecuted()
			.hasNumberOfJoins(0);
    }
    
    @Test
    public void executesJoinOnLazyCollectionWhenExecutedInContextOfACountQueryButThereIsFilteringOnFetchedPart() {
    	JoinFetch<Customer> fetchSpec = new JoinFetch<Customer>(queryCtx, new String[] { "orders" }, "o", JoinType.LEFT, true);
    	
    	Specification<Customer> orderForMoreDuff = new Like<>(queryCtx, "o.itemName", "Duff");
    	
    	Specification<Customer> specWithFilterOnJoin = Specification.where(fetchSpec).and(orderForMoreDuff);
        
        Number customerCount = customerRepo.count(specWithFilterOnJoin);

        assertThat(customerCount.intValue())
                .isEqualTo(1);
    	
    	assertThat()
			.theOnlyOneQueryThatWasExecuted()
			.hasNumberOfJoins(1);
    }
    
    @Test
    public void doesNotJoinLazyCollectionWhenExecutedInContextOfACountQueryAndNoFilteringOnFetchedPart_aliasExistsButNoFiltering() {
    	JoinFetch<Customer> fetchSpec = new JoinFetch<Customer>(queryCtx, new String[] { "orders" }, "o", JoinType.LEFT, true); // there is an alias for the join
    																															// but it is not used for filtering
        Number customerCount = customerRepo.count(fetchSpec); 

        assertThat(customerCount.intValue())
                .isEqualTo(3);
    	
    	assertThat()
			.theOnlyOneQueryThatWasExecuted()
			.hasNumberOfJoins(0);
    }
    
    @Test
    public void doesNotJoinLazyCollectionWhenExecutedInContextOfACountQueryAndNoFilteringOnFetchedPart_aliasExistsButNotNotUsedforFiltering_butIsUsedForNestedFetch() {
    	JoinFetch<Customer> ordersFetch = new JoinFetch<Customer>(queryCtx, new String[] { "orders" }, "o", JoinType.LEFT, true);
    	JoinFetch<Customer> tagsFetch = new JoinFetch<Customer>(queryCtx, new String[] { "o.tags" }, "t", JoinType.LEFT, true);
    	Specification<Customer> fullSpec = Specification.where(ordersFetch).and(tagsFetch);
    	
    	Number customerCount = customerRepo.count(fullSpec);
    	
    	assertThat(customerCount.intValue())
        	.isEqualTo(3);
    	
    	assertThat()
			.theOnlyOneQueryThatWasExecuted()
			.hasNumberOfJoins(0);
    }
    
    @Test
    public void executesNestedJoinInCountQueryWhenUsedForFiltering() {
    	JoinFetch<Customer> ordersFetch = new JoinFetch<>(queryCtx, new String[] { "orders" }, "o", JoinType.LEFT, true);
    	JoinFetch<Customer> tagsFetch = new JoinFetch<>(queryCtx, new String[] { "o.tags" }, "t", JoinType.LEFT, true);
    	Specification<Customer> tagsFilter = new Like<>(queryCtx, "t.name", BLACK_FRIDAY_TAG);
    	Specification<Customer> fullSpec = Specification.where(ordersFetch).and(tagsFetch).and(tagsFilter);
    	
    	Number customerCount = customerRepo.count(fullSpec);
    	
    	assertThat(customerCount.intValue())
        	.isEqualTo(1);
    	
    	assertThat()
			.theOnlyOneQueryThatWasExecuted()
			.hasNumberOfJoins(2);
    }

    @Test
    public void countQueryIgnoresTwoLeftJoinFetchesInSingleLeftDefinition() {
        JoinFetch<Customer> joinFetch = new JoinFetch<Customer>(queryCtx, new String[] { "orders", "orders2" }, JoinType.LEFT, true);

        Number customerCount = customerRepo.count(joinFetch);
    	
    	assertThat(customerCount.intValue())
        	.isEqualTo(3);
    	
    	assertThat()
			.theOnlyOneQueryThatWasExecuted()
			.hasNumberOfJoins(0);
    }

    @Test
    public void countQueryIgnoresTwoFetchesUsingSingleInnerJoinFetchDefinition() {
        JoinFetch<Customer> joinFetch = new JoinFetch<Customer>(queryCtx, new String[] { "orders", "orders2" }, JoinType.INNER, true);

        Number customerCount = customerRepo.count(joinFetch);
    	
    	assertThat(customerCount.intValue())
        	.isEqualTo(3);
    	
    	assertThat()
			.theOnlyOneQueryThatWasExecuted()
			.hasNumberOfJoins(0);
    }
  
    @Test
    public void countQueryIgnoresTwoFetchesUsingTwoJoinFetchDefinition() {
    	JoinFetch<Customer> spec1 = new JoinFetch<Customer>(queryCtx, new String[] { "orders" }, "o", JoinType.LEFT, true);
    	JoinFetch<Customer> spec2 = new JoinFetch<Customer>(queryCtx, new String[] { "orders2" }, "o", JoinType.INNER, true);

    	Conjunction<Customer> spec = new Conjunction<Customer>(spec1, spec2);

    	Number customerCount = customerRepo.count(spec);
    	
    	assertThat(customerCount.intValue())
        	.isEqualTo(3);
    	
    	assertThat()
			.theOnlyOneQueryThatWasExecuted()
			.hasNumberOfJoins(0);
    }
    
    @Test
    public void countQueryExecutesTwoJoinsUsingTwoJoinFetchDefinitionWhenTheyAreUsedForFiltering() {
    	JoinFetch<Customer> fetch1 = new JoinFetch<Customer>(queryCtx, new String[] { "orders" }, "o1", JoinType.LEFT, true);
    	Specification<Customer> filter1 = new Like<>(queryCtx, "o1.itemName", "Duff");
    	JoinFetch<Customer> fetch2 = new JoinFetch<Customer>(queryCtx, new String[] { "orders2" }, "o2", JoinType.INNER, true);
    	Specification<Customer> filter2 = new Like<>(queryCtx, "o2.itemName", "Comic");
    	
    	Specification<Customer> fullSpec = Specification.where(fetch1).and(fetch2).and(Specification.where(filter1).or(filter2));

    	Number customerCount = customerRepo.count(fullSpec);
    	
    	assertThat(customerCount.intValue())
        	.isEqualTo(2);
    	
    	assertThat()
			.theOnlyOneQueryThatWasExecuted()
			.hasNumberOfJoins(2);
    }
    
    @Test
    public void skipsJoinNotUsedForFilteringButExecutesTheOneUsedForFiltering() {
    	JoinFetch<Customer> fetch1 = new JoinFetch<Customer>(queryCtx, new String[] { "orders" }, "o1", JoinType.LEFT, true);
    	Specification<Customer> filter1 = new Like<>(queryCtx, "o1.itemName", "Duff");
    	JoinFetch<Customer> fetch2 = new JoinFetch<Customer>(queryCtx, new String[] { "orders2" }, "o2", JoinType.INNER, true);
    	
    	Specification<Customer> fullSpec = Specification.where(fetch1).and(fetch2).and(filter1);

    	Number customerCount = customerRepo.count(fullSpec);
    	
    	assertThat(customerCount.intValue())
        	.isEqualTo(1);
    	
    	assertThat()
			.theOnlyOneQueryThatWasExecuted()
			.hasNumberOfJoins(1);
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
        
        fail("think about how what are corresponding scenarios for count query context");
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
        
        fail("think about how what are corresponding scenarios for count query context");
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
        
        fail("think about how what are corresponding scenarios for count query context");
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
        
        fail("think about how what are corresponding scenarios for count query context");
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
        
        fail("think about how what are corresponding scenarios for count query context");
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
        
        fail("think about how what are corresponding scenarios for count query context");
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
        
        fail("think about how what are corresponding scenarios for count query context");
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
        
        fail("think about how what are corresponding scenarios for count query context");
    }

    @Test
    public void performsNotDistinctFetchWhenDistinctParamIsSetToFalse() {
        JoinFetch<Customer> spec = new JoinFetch<Customer>(queryCtx, new String[] { "orders" }, "o", JoinType.LEFT, false);
    
        List<Customer> customers = customerRepo.findAll(spec);
        
        assertThat(customers)
                .hasSize(4)
                .extracting(Customer::getFirstName)
                .containsExactly("Homer", "Homer", "Marge", "Bart");
        
        fail("think about how what are corresponding scenarios for count query context");
    }
    
    @Test
    public void performsDistinctFetchWhenDistinctParamIsSetToTrue() {
        JoinFetch<Customer> spec = new JoinFetch<Customer>(queryCtx, new String[] { "orders" }, "o", JoinType.LEFT, true);
    
        List<Customer> customers = customerRepo.findAll(spec);
    
        assertThat(customers)
                .hasSize(3)
                .extracting(Customer::getFirstName)
                .containsExactlyInAnyOrder("Bart", "Homer", "Marge");
        
        fail("think about how what are corresponding scenarios for count query context");
    }
}
