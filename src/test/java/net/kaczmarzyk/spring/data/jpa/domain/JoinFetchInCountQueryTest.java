/**
 * Copyright 2014-2022 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.kaczmarzyk.spring.data.jpa.domain;

import static jakarta.persistence.criteria.JoinType.INNER;
import static jakarta.persistence.criteria.JoinType.LEFT;
import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static net.kaczmarzyk.spring.data.jpa.ItemTagBuilder.itemTag;
import static net.kaczmarzyk.spring.data.jpa.OrderBuilder.order;
import static net.kaczmarzyk.utils.InterceptedStatementsAssert.assertThatInterceptedStatements;
import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.criteria.JoinType;

import net.kaczmarzyk.utils.interceptor.HibernateStatementInspector;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.jpa.domain.Specification;

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.IntegrationTestBase;
import net.kaczmarzyk.spring.data.jpa.ItemTag;


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
                .orders("Duff Beer", "Donuts", "More Donuts")
                .orders(order("Duff MegaPack").withTags(BLACK_FRIDAY_TAG))
                .build(em);
        margeSimpson = customer("Marge", "Simpson")
                .build(em);
        bartSimpson = customer("Bart", "Simpson")
                .orders(order("Comic Books").withTags(books))
                .build(em);


        em.createQuery("select o from Order o where o.id > 1"); // dummy query to fill Hibernate Query Plan so that LoggedQueryAssertions can track all queries in the test
        em.flush();
        em.clear();

        HibernateStatementInspector.clearInterceptedStatements();
    }

    @Test
    public void doesNotJoinLazyCollectionWhenExecutedInContextOfACountQueryAndNoFilteringOnFetchedPart() {
        JoinFetch<Customer> spec = new JoinFetch<Customer>(queryCtx, new String[]{"orders"}, "", LEFT, true); // empty alias = for sure no filtering

        Number customerCount = customerRepo.count(spec);

        assertThat(customerCount.intValue())
                .isEqualTo(3);

        assertThatInterceptedStatements()
                .hasSelects(1)
                .hasNumberOfJoins(0);
    }

    @Test
    public void executesJoinOnLazyCollectionWhenExecutedInContextOfACountQueryButThereIsFilteringOnFetchedPart() {
        JoinFetch<Customer> fetchSpec = new JoinFetch<Customer>(queryCtx, new String[]{"orders"}, "o", LEFT, true);

        Specification<Customer> orderForMoreDuff = new Like<>(queryCtx, "o.itemName", "Duff");

        Specification<Customer> specWithFilterOnJoin = Specification.where(fetchSpec).and(orderForMoreDuff);

        Number customerCount = customerRepo.count(specWithFilterOnJoin);

        assertThat(customerCount.intValue())
                .isEqualTo(1);

        assertThatInterceptedStatements()
                .hasOnlyOneQueryThatWasExecuted()
                .hasNumberOfJoins(1);
    }

    @Test
    public void doesNotJoinLazyCollectionWhenExecutedInContextOfACountQueryAndNoFilteringOnFetchedPart_aliasExistsButNoFiltering() {
        JoinFetch<Customer> fetchSpec = new JoinFetch<Customer>(queryCtx, new String[]{"orders"}, "o", LEFT, true); // there is an alias for the join
        // but it is not used for filtering
        Number customerCount = customerRepo.count(fetchSpec);

        assertThat(customerCount.intValue())
                .isEqualTo(3);

        assertThatInterceptedStatements()
                .hasOnlyOneQueryThatWasExecuted()
                .hasNumberOfJoins(0);
    }

    @Test
    public void doesNotJoinLazyCollectionWhenExecutedInContextOfACountQueryAndNoFilteringOnFetchedPart_aliasExistsButNotNotUsedforFiltering_butIsUsedForNestedFetch() {
        JoinFetch<Customer> ordersFetch = new JoinFetch<Customer>(queryCtx, new String[]{"orders"}, "o", LEFT, true);
        JoinFetch<Customer> tagsFetch = new JoinFetch<Customer>(queryCtx, new String[]{"o.tags"}, "t", LEFT, true);
        Specification<Customer> fullSpec = Specification.where(ordersFetch).and(tagsFetch);

        Number customerCount = customerRepo.count(fullSpec);

        assertThat(customerCount.intValue())
                .isEqualTo(3);

        assertThatInterceptedStatements()
                .hasOnlyOneQueryThatWasExecuted()
                .hasNumberOfJoins(0);
    }

    @Test
    public void executesNestedJoinInCountQueryWhenUsedForFiltering() {
        JoinFetch<Customer> ordersFetch = new JoinFetch<>(queryCtx, new String[]{"orders"}, "o", LEFT, false);
        JoinFetch<Customer> tagsFetch = new JoinFetch<>(queryCtx, new String[]{"o.tags"}, "t", LEFT, false);
        Specification<Customer> tagsFilter = new Like<>(queryCtx, "t.name", BLACK_FRIDAY_TAG);
        Specification<Customer> fullSpec = Specification.where(ordersFetch).and(tagsFetch).and(tagsFilter);

        Number customerCount = customerRepo.count(fullSpec);

        assertThat(customerCount.intValue())
                .isEqualTo(1);

        assertThatInterceptedStatements()
                .hasOnlyOneQueryThatWasExecuted()
                .hasNumberOfJoins(3)
                .hasNumberOfTableJoins("orders", LEFT, 1)
                .hasNumberOfTableJoins("orders_tags", LEFT, 1)
                .hasNumberOfTableJoins("item_tags", INNER, 1);
    }

    @Test
    public void countQueryIgnoresTwoLeftJoinFetchesInSingleLeftDefinition() {
        JoinFetch<Customer> joinFetch = new JoinFetch<Customer>(queryCtx, new String[]{"orders", "orders2"}, LEFT, true);

        Number customerCount = customerRepo.count(joinFetch);

        assertThat(customerCount.intValue())
                .isEqualTo(3);

        assertThatInterceptedStatements()
                .hasOnlyOneQueryThatWasExecuted()
                .hasNumberOfJoins(0);
    }

    @Test
    public void countQueryIgnoresTwoFetchesUsingSingleInnerJoinFetchDefinition() {
        JoinFetch<Customer> joinFetch = new JoinFetch<Customer>(queryCtx, new String[]{"orders", "orders2"}, JoinType.INNER, true);

        Number customerCount = customerRepo.count(joinFetch);

        assertThat(customerCount.intValue())
                .isEqualTo(3);

        assertThatInterceptedStatements()
                .hasOnlyOneQueryThatWasExecuted()
                .hasNumberOfJoins(0);
    }

    @Test
    public void countQueryIgnoresTwoFetchesUsingTwoJoinFetchDefinition() {
        JoinFetch<Customer> spec1 = new JoinFetch<Customer>(queryCtx, new String[]{"orders"}, "o", LEFT, true);
        JoinFetch<Customer> spec2 = new JoinFetch<Customer>(queryCtx, new String[]{"orders2"}, "o", JoinType.INNER, true);

        Conjunction<Customer> spec = new Conjunction<Customer>(spec1, spec2);

        Number customerCount = customerRepo.count(spec);

        assertThat(customerCount.intValue())
                .isEqualTo(3);

        assertThatInterceptedStatements()
                .hasOnlyOneQueryThatWasExecuted()
                .hasNumberOfJoins(0);
    }

    @Test
    public void countQueryExecutesTwoJoinsUsingTwoJoinFetchDefinitionWhenTheyAreUsedForFiltering() {
        JoinFetch<Customer> fetch1 = new JoinFetch<Customer>(queryCtx, new String[]{"orders"}, "o1", LEFT, true);
        Specification<Customer> filter1 = new Like<>(queryCtx, "o1.itemName", "Duff");
        JoinFetch<Customer> fetch2 = new JoinFetch<Customer>(queryCtx, new String[]{"orders2"}, "o2", JoinType.INNER, true);
        Specification<Customer> filter2 = new Like<>(queryCtx, "o2.itemName", "Comic");

        Specification<Customer> fullSpec = Specification.where(fetch1).and(fetch2).and(Specification.where(filter1).or(filter2));

        Number customerCount = customerRepo.count(fullSpec);

        assertThat(customerCount.intValue())
                .isEqualTo(2);

        assertThatInterceptedStatements()
                .hasOnlyOneQueryThatWasExecuted()
                .hasNumberOfJoins(2);
    }

    @Test
    public void skipsJoinNotUsedForFilteringButExecutesTheOneUsedForFiltering() {
        JoinFetch<Customer> fetch1 = new JoinFetch<Customer>(queryCtx, new String[]{"orders"}, "o1", LEFT, true);
        Specification<Customer> filter1 = new Like<>(queryCtx, "o1.itemName", "Duff");
        JoinFetch<Customer> fetch2 = new JoinFetch<Customer>(queryCtx, new String[]{"orders2"}, "o2", LEFT, true);

        Specification<Customer> fullSpec = Specification.where(fetch1).and(fetch2).and(filter1);

        Number customerCount = customerRepo.count(fullSpec);

        assertThat(customerCount.intValue())
                .isEqualTo(1);

        assertThatInterceptedStatements()
                .hasOnlyOneQueryThatWasExecuted()
                .hasNumberOfJoins(1);
    }

    @Test
    public void usesNotDistinctQueryInCountWhenDistinctSetToFalse() {
        JoinFetch<Customer> fetch = new JoinFetch<Customer>(queryCtx, new String[]{"orders"}, "o", LEFT, false);
        Specification<Customer> filter = new Like<>(queryCtx, "o.itemName", "o"); // Homer's Donuts (x2) and Bart's Comic Book

        Specification<Customer> spec = Specification.where(fetch).and(filter);

        Number count = customerRepo.count(spec);

        assertThat(count.intValue())
                .isEqualTo(3); // Homer is counted twice (2 orders and no distinct in query)
    }

    @Test
    public void usesDistinctQueryInCountWhenDistinctSetToTrue() {
        JoinFetch<Customer> fetch = new JoinFetch<Customer>(queryCtx, new String[]{"orders"}, "o", LEFT, true);
        Specification<Customer> filter = new Like<>(queryCtx, "o.itemName", "o"); // Homer's Donuts (x2) and Bart's Comic Book

        Specification<Customer> spec = Specification.where(fetch).and(filter);

        Number count = customerRepo.count(spec);

        assertThat(count.intValue())
                .isEqualTo(2);
    }

    @Test
    public void preservesJoinTypeWhenConvertingJoinFetchForCountQuery_innerJoin() {
        JoinFetch<Customer> fetch = new JoinFetch<Customer>(queryCtx, new String[]{"orders"}, "o", JoinType.INNER, true);
        Specification<Customer> filter = new Like<>(queryCtx, "o.itemName", "o");
        Specification<Customer> spec = Specification.where(fetch).and(filter);
        customerRepo.count(spec);

        assertThatInterceptedStatements()
                .hasOnlyOneQueryThatWasExecuted()
                .hasNumberOfTableJoins("orders", INNER, 1);
    }

    @Test
    public void preservesJoinTypeWhenConvertingJoinFetchForCountQuery_leftJoin() {
        JoinFetch<Customer> fetch = new JoinFetch<Customer>(queryCtx, new String[]{"orders"}, "o", LEFT, true);
        Specification<Customer> filter = new Like<>(queryCtx, "o.itemName", "o");
        Specification<Customer> spec = Specification.where(fetch).and(filter);
        customerRepo.count(spec);

        assertThatInterceptedStatements()
                .hasOnlyOneQueryThatWasExecuted()
                .hasNumberOfTableJoins("orders", LEFT, 1);
    }
}
