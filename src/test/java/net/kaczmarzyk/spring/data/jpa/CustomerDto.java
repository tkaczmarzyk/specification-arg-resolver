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

import java.util.Collection;
import java.util.Objects;

public class CustomerDto {
	private String firstName;
	private String tagOfFirstCustomerItem;

	public CustomerDto(String firstName, String tagOfFirstCustomerItem) {
		this.firstName = firstName;
		this.tagOfFirstCustomerItem = tagOfFirstCustomerItem;
	}

	public static CustomerDto from(Customer customer) {
		String tagOfFirstOrderedItem = customer.getOrders().stream()
				.map(Order::getTags)
				.filter(Objects::nonNull)
				.flatMap(Collection::stream)
				.map(ItemTag::getName)
				.findFirst().orElse(null);

		return new CustomerDto(
				customer.getFirstName(),
				tagOfFirstOrderedItem
		);
	}

	public String getFirstName() {
		return firstName;
	}

	public String getTagOfFirstCustomerItem() {
		return tagOfFirstCustomerItem;
	}
}