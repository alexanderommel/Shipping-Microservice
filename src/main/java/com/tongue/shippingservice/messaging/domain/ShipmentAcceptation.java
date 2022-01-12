package com.tongue.shippingservice.messaging.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * Artifact courier found
 *
 * **/

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShipmentAcceptation {

    private String artifactId;
    private String shippingId;
    private String driverUsername;
}
