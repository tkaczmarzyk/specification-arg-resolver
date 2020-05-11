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

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


@Entity
@Table(name = "orders")
public class Order {

    @Id @GeneratedValue
    private Long id;
    
    private String itemName;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private Customer customer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private Customer customer2;
    
    
    Order() {
    }
    
    public Order(Customer customer, String itemName) {
        this.itemName = itemName;
        this.customer = customer;
        customer.getOrders().add(this);
        this.customer2 = customer;
        customer.getOrders2().add(this);
    }
}
