package com.tongue.shippingservice.messaging.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 *  Courier has reached the restaurant and has the package in the motorbike
 *
 * **/

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShipmentContinuation {

    private String artifactId;
    private String shippingId;

}
