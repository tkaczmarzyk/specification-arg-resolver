/*
 * Copyright 2014-2025 the original author or authors.
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

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "badges")
public class Badge {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String badgeType;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private Customer customer;
    
    Badge() {
	}
    
    public Badge(Customer customer, String type) {
    	this.badgeType = type;
    	this.customer = customer;
    	customer.getBadges().add(this);
    }
}
