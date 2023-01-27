/**
 * Copyright 2014-2023 the original author or authors.
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
package net.kaczmarzyk.benchmark.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;

/**
 * A simple entity for specification testing
 *
 * @author Tomasz Kaczmarzyk
 */
@Entity
public class Customer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Gender gender;

	private char genderAsChar;
	private Character genderAsCharacter;

	private String firstName;

	private String lastName;

	private String nickName;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date registrationDate;
	private Calendar registrationCalendar;

	private LocalDate birthDate;

	private String occupation;

	private Timestamp lastSeen;

	private LocalDateTime lastOrderTime;

	private Integer weight;
	private int weightInt;
	private long weightLong;

	@Column(columnDefinition = "NUMBER(6,2)")
	private float weightFloat;
	private Double weightDouble;
	private BigDecimal weightBigDecimal;

	private boolean gold;
	private Boolean goldObj;

	private Instant dateOfNextSpecialOfferInstant;
	private Timestamp dateOfNextSpecialOfferTimestamp;
	private OffsetDateTime dateOfNextSpecialOffer;
	private ZonedDateTime dateOfNextSpecialOfferZoned;

	@Column(columnDefinition = "uuid")
	private UUID refCode;

	@OneToMany(mappedBy = "customer", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
	private Set<Order> orders;

	@ElementCollection
	@CollectionTable(name = "customer_phone_numbers", joinColumns = @JoinColumn(name = "id"))
	private Set<String> phoneNumbers;

	@ElementCollection
	@CollectionTable(name = "customer_lucky_numbers", joinColumns = @JoinColumn(name = "id"))
	private Set<Long> luckyNumbers;

	public Customer() {
	}

	public Customer(String firstName, String lastName, Gender gender, Date registrationDate) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.gender = gender;
		this.genderAsChar = gender.name().charAt(0);
		this.genderAsCharacter = this.genderAsChar;
		this.registrationDate = registrationDate;
	}

	public Long getId() {
		return id;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
		this.genderAsChar = gender.name().charAt(0);
		this.genderAsCharacter = this.genderAsChar;
	}

	public Timestamp getLastSeen() {
		return lastSeen;
	}

	public void setLastSeen(Timestamp lastSeen) {
		this.lastSeen = lastSeen;
	}

	public Date getRegistrationDate() {
		return registrationDate;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setRegistrationDate(Date registrationDate) {
		this.registrationDate = registrationDate;

		this.registrationCalendar = Calendar.getInstance();
		this.registrationCalendar.setTime(registrationDate);
	}

	public LocalDate getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(LocalDate birthDate) {
		this.birthDate = birthDate;
	}

	public String getOccupation() {
		return occupation;
	}

	public void setOccupation(String occupation) {
		this.occupation = occupation;
	}

	public LocalDateTime getLastOrderTime() {
		return lastOrderTime;
	}

	public void setLastOrderTime(LocalDateTime lastOrderTime) {
		this.lastOrderTime = lastOrderTime;
	}

	/**
	 *
	 * @param weight
	 * NOTE: weightFloat has 0.1 added, weightDouble has 0.2 added, weightBigDecimal has 0.3 added
	 */
	public void setWeight(int weight) {
		this.weight = weight;
		this.weightInt = weight;
		this.weightLong = weight;
		this.weightFloat = weight + 0.1f;
		this.weightDouble = weight + 0.2;
		this.weightBigDecimal = BigDecimal.valueOf(weight).add(new BigDecimal("0.3"));
	}

	public boolean isGold() {
		return gold;
	}

	public void setGold(boolean gold) {
		this.gold = gold;
		this.goldObj = gold;
	}

	public OffsetDateTime getDateOfNextSpecialOffer() {
		return dateOfNextSpecialOffer;
	}

	public void setDateOfNextSpecialOffer(OffsetDateTime dateOfNextSpecialOffer) {
		this.dateOfNextSpecialOffer = dateOfNextSpecialOffer;
		this.dateOfNextSpecialOfferInstant = dateOfNextSpecialOffer.toInstant();
		this.dateOfNextSpecialOfferTimestamp = Timestamp.from(dateOfNextSpecialOffer.toInstant());
		this.dateOfNextSpecialOfferZoned = dateOfNextSpecialOffer.toZonedDateTime();
	}

	public UUID getRefCode() {
		return refCode;
	}

	public void setRefCode(UUID refCode) {
		this.refCode = refCode;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getNickName() {
		return nickName;
	}

	public Collection<Order> getOrders() {
		if (orders == null) {
			orders = new HashSet<>();
		}
		return orders;
	}

	@Override
	public String toString() {
		return "Customer[" + firstName + " " + lastName + "]";
	}

	public Integer getWeight() {
		return weight;
	}

	public int getWeightInt() {
		return weightInt;
	}

	public long getWeightLong() {
		return weightLong;
	}

	public float getWeightFloat() {
		return weightFloat;
	}

	public Double getWeightDouble() {
		return weightDouble;
	}

	public Boolean getGoldObj() {
		return goldObj;
	}

	public Set<String> getPhoneNumbers() {
		if (phoneNumbers == null) {
			phoneNumbers = new HashSet<>();
		}
		return phoneNumbers;
	}

	public Set<Long> getLuckyNumbers() {
		if (luckyNumbers == null) {
			luckyNumbers = new HashSet<>();
		}
		return luckyNumbers;
	}

	public  void addLuckyNumber(Long luckyNumber) {
		getLuckyNumbers().add(luckyNumber);
	}

	public void addPhoneNumber(String phoneNumber) {
		getPhoneNumbers().add(phoneNumber);
	}
}

