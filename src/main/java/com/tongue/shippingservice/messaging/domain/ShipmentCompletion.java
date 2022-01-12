package com.tongue.shippingservice.messaging.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * Shipping Finished
 *
 * **/

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShipmentCompletion {

    private String artifactId;
    private String shippingId;

}
