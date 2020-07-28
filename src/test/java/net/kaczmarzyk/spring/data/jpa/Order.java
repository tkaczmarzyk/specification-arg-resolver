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

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.CascadeType.REMOVE;


@Entity
@Table(name = "orders")
public class Order {

    @Id @GeneratedValue
    private Long id;

    private String itemName;

    @ManyToMany(fetch = FetchType.LAZY)
    private Set<ItemTag> tags;

    @ManyToMany(fetch = FetchType.LAZY)
    private List<ItemTag> tagsList;

    @ManyToMany(fetch = FetchType.LAZY)
    private Collection<ItemTag> tagsCollection;

    @ManyToOne(fetch = FetchType.LAZY)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    private Customer customer2;

    @OneToOne(fetch = FetchType.LAZY, cascade = { PERSIST, REMOVE })
    @JoinColumn(name = "note_id")
    private OrderNote note;


    Order() {
    }

    public Order(Customer customer, String itemName) {
        this.itemName = itemName;
        this.customer = customer;
        customer.getOrders().add(this);
        this.customer2 = customer;
        customer.getOrders2().add(this);
    }

    public Order(Customer customer, String itemName, Set<ItemTag> tags, OrderNote note) {
        this(customer, itemName);
        this.tags = tags;
        this.tagsList = new ArrayList<>(tags);
        this.tagsCollection = tags;
        this.note = note;
    }

    public Set<ItemTag> getTags() {
        return tags;
    }

    public List<ItemTag> getTagsList() {
        return tagsList;
    }

    public Collection<ItemTag> getTagsCollection() {
        return tagsCollection;
    }

    public OrderNote getNote() {
        return note;
    }

    public void setNote(OrderNote note) {
        this.note = note;
    }

    public Long getId() {
        return id;
    }

    public String getItemName() {
        return itemName;
    }
}
