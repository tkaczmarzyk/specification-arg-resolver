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

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Calendar;
import java.util.UUID;

/**
 * Helper class for building test data
 *
 * @author Tomasz Kaczmarzyk
 */
public class CustomerBuilder {
	
	private Customer customer = new Customer();
	
	private CustomerBuilder(String firstName, String lastName) {
		customer.setFirstName(firstName);
		customer.setLastName(lastName);
	}
	
	public CustomerBuilder street(String street) {
		customer.getAddress().setStreet(street);
		return this;
	}
	
	public CustomerBuilder gender(Gender gender) {
		customer.setGender(gender);
		return this;
	}
	
	public CustomerBuilder nickName(String nickName) {
		customer.setNickName(nickName);
		return this;
	}
	
	public CustomerBuilder birthDate(LocalDate birthDate) {
		customer.setBirthDate(birthDate);
		return this;
	}
	
	public CustomerBuilder occupation(String occupation) {
		customer.setOccupation(occupation);
		return this;
	}
	
	public CustomerBuilder lastOrderTime(LocalDateTime lastOrderTime) {
		customer.setLastOrderTime(lastOrderTime);
		return this;
	}
	
	public CustomerBuilder registrationDate(int year, int month, int day) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month - 1);
		cal.set(Calendar.DAY_OF_MONTH, day);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		customer.setRegistrationDate(cal.getTime());
		return this;
	}
	
	public CustomerBuilder weight(int weight) {
		customer.setWeight(weight);
		return this;
	}
	
	public CustomerBuilder golden() {
		customer.setGold(true);
		return this;
	}
	
	public CustomerBuilder notGolden() {
		customer.setGold(false);
		return this;
	}
	
	public CustomerBuilder refCode(UUID refCode) {
		customer.setRefCode(refCode);
		return this;
	}
	
	public CustomerBuilder nextSpecialOffer(OffsetDateTime dateOfNextSpecialOffer) {
		customer.setDateOfNextSpecialOffer(dateOfNextSpecialOffer);
		return this;
	}
	
	public CustomerBuilder orders(String... orderItems) {
		for (String orderItem : orderItems) {
			new Order(customer, orderItem);
		}
		return this;
	}

	public CustomerBuilder orders(OrderBuilder... orderBuilders) {
		for(OrderBuilder order: orderBuilders) {
			order.build(customer);
		}
		return this;
	}
	
	public CustomerBuilder badges(String... badgeTypes) {
		for (String badgeType : badgeTypes) {
			new Badge(customer, badgeType);
		}
		return this;
	}
	
	public Customer build(EntityManager em) {
		em.persist(customer);
		return customer;
	}
	
	public static CustomerBuilder customer(String firstName, String lastName) {
		return new CustomerBuilder(firstName, lastName);
	}
}
