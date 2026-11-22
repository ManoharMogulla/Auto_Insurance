package com.ltm.insurance.customermicroservices.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "customer_profiles", uniqueConstraints = {
        @UniqueConstraint(name = "uk_customer_profiles_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_customer_profiles_phone", columnNames = "phone_number")
})
public class CustomerProfile {

    @Id
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 80)
    private String lastName;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(name = "phone_number", nullable = false, length = 24)
    private String phoneNumber;

    protected CustomerProfile() {
    }

    public CustomerProfile(Long memberId, String firstName, String lastName, String email, String phoneNumber) {
        this.memberId = memberId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public Long getMemberId() { return memberId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
}
