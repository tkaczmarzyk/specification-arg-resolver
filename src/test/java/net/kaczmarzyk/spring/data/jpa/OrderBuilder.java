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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class OrderBuilder {

	private String itemName;
	private Set<ItemTag> tags = new HashSet<>();
	private OrderNote note;

	private OrderBuilder(String itemName) {
		this.itemName = itemName;
		this.note = new OrderNote("Note" + itemName);
	}

	public static OrderBuilder order(String itemName) {
		return new OrderBuilder(itemName);
	}

	public OrderBuilder withTags(ItemTag... tags) {
		this.tags.addAll(Arrays.asList(tags));
		return this;
	}

	public Order build(Customer customer) {
		return new Order(customer, itemName, tags, note);
	}
}
