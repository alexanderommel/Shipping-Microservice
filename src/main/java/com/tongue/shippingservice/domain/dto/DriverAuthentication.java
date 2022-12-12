package com.tongue.shippingservice.domain.dto;

import com.tongue.shippingservice.domain.replication.Driver;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DriverAuthentication {

    private String jwt;
    private Driver driver;

}
