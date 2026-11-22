package com.ltm.insurance.customermicroservices.model;


import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.*;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "drivers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Driver {

    @Id
    @Column(
            name = "member_id",
            unique = true,
            nullable = false
    )
    private Long memberId;
    private Long customerId;
    private boolean namedInsured;
    private String firstName;
    private String lastName;
    @Temporal(TemporalType.DATE)
    private Date dateOfBirth;
    private String licenseNumber;
    private String licenseState;
    private String licenseExpirationDate;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String phoneNumber;
    private String email;
    private String gender;
    private String maritalStatus;
//
//    @OneToMany(cascade = CascadeType.ALL)
//    @JoinColumn(name = "member_id")
//    private List<Vehicle> vehicles;

    @OneToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER
    )
    @JoinColumn(name = "member_id")
    private List<Vehicle> vehicles;

    private String drivingHistory;
    private String insuranceHistory;
    private String creditScore;

    @Column(name = "manpolicy_id")
    private String policyId;

    @JsonSetter(value = "namedInsured", nulls = Nulls.SET)
    public void setNamedInsured(Boolean namedInsured) {
        this.namedInsured = Boolean.TRUE.equals(namedInsured);
    }

}
