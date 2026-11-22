package com.ltm.insurance.authentication.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "auth_users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_auth_users_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_auth_users_phone", columnNames = "phone_number"),
        @UniqueConstraint(name = "uk_auth_users_member", columnNames = "member_id")
})
public class AuthUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(name = "phone_number", nullable = false, length = 24)
    private String phoneNumber;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 80)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private Role role = Role.CUSTOMER;

    protected AuthUser() {
    }

    public AuthUser(String email, String phoneNumber, String passwordHash, Long memberId,
                    Long customerId, String firstName, String lastName, Role role) {
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.passwordHash = passwordHash;
        this.memberId = memberId;
        this.customerId = customerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role == null ? Role.CUSTOMER : role;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getPasswordHash() { return passwordHash; }
    public Long getMemberId() { return memberId; }
    public Long getCustomerId() { return customerId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public Role getRole() { return role; }
}
