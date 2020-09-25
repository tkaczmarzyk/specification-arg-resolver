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