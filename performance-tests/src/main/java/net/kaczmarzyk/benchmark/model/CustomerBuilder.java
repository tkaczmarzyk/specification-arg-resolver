package net.kaczmarzyk.benchmark.model;

import java.sql.Timestamp;
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

    public CustomerBuilder lastSeen(Timestamp timestamp) {
        customer.setLastSeen(timestamp);
        return this;
    }

    public CustomerBuilder registrationDate(int year, int month, int day) {
        return registrationDate(year, month, day, 0, 0, 0 ,0);
    }

    public CustomerBuilder registrationDate(int year, int month, int day, int hour, int minute, int second, int millisecond) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);
        cal.set(Calendar.MILLISECOND, millisecond);
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

    public CustomerBuilder phoneNumbers(String... phoneNumbers) {
        for(String phoneNumber: phoneNumbers) {
            customer.addPhoneNumber(phoneNumber);
        }
        return this;
    }

    public CustomerBuilder luckyNumbers(Long... luckyNumbers) {
        for(Long luckyNumber : luckyNumbers) {
            customer.addLuckyNumber(luckyNumber);
        }
        return this;
    }

    public Customer build() {
        return customer;
    }

    public static CustomerBuilder customer(String firstName, String lastName) {
        return new CustomerBuilder(firstName, lastName);
    }
}
