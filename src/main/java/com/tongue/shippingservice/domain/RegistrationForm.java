package com.tongue.shippingservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegistrationForm {

    private String email;
    private String firstName;
    private String lastName;
    private String car;
    private String brand;
    private String password;

}
