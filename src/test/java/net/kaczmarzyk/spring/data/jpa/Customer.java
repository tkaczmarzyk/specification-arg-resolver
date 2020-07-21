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

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;


/**
 * A simple entity for specification testing
 * 
 * @author Tomasz Kaczmarzyk
 */
@Entity
public class Customer {

    @Id
    @GeneratedValue
    private Long id;

    private Gender gender;
    
    @Enumerated(EnumType.STRING)
    private Gender genderAsString;

    private String firstName;

    private String lastName;

    private String nickName;
    
    @Embedded
    private Address address = new Address();

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date registrationDate;

    private LocalDate birthDate;

    private String occupation;

    private LocalDateTime lastOrderTime;

    private Integer weight;

    private int weightInt;
    private long weightLong;
    private float weightFloat;
    private Double weightDouble;
    private BigDecimal weightBigDecimal;
    
    private boolean gold;
    private Boolean goldObj;
    
    private Instant dateOfNextSpecialOfferInstant;
    private OffsetDateTime dateOfNextSpecialOffer;
    
    private UUID refCode;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private Set<Order> orders;
    
    @OneToMany(mappedBy = "customer2", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private Set<Order> orders2;
    
    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private Set<Badge> badges;
    
    
    public Customer() {
    }

    public Customer(String firstName, String lastName, Gender gender, Date registrationDate, String street) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.genderAsString = gender;
        this.registrationDate = registrationDate;
        this.address.setStreet(street);
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
        this.genderAsString = gender;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public Address getAddress() {
        return address;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
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
	
	public Collection<Order> getOrders2() {
		if (orders2 == null) {
			orders2 = new HashSet<>();
		}
		return orders2;
	}
	
	@Override
	public String toString() {
		return "Customer[" + firstName + " " + lastName + "]";
	}

	public Gender getGenderAsString() {
		return genderAsString;
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
	
	public Set<Badge> getBadges() {
		if (badges == null) {
			badges = new HashSet<>();
		}
		return badges;
	}
	
}
