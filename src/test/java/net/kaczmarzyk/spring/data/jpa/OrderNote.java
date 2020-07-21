package net.kaczmarzyk.spring.data.jpa;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class OrderNote {

	@Id
	@GeneratedValue
	private Long id;

	private String title;

	@OneToOne
	private Customer customer;

	OrderNote() {}

	public OrderNote(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}
}
