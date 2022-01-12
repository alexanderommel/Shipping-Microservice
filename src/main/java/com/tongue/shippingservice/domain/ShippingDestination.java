package com.tongue.shippingservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * Couriers get customer position only after they have taken the artifact
 *
 * **/

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShippingDestination {

    private String artifactId;
    private Position destination;
    private Shipping.Billing billing;

}
