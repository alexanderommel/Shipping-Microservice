package com.tongue.shippingservice.messaging.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * The customer wants to cancel the shipping request
 *
 * **/

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShippingRequestCancellation {

    private String artifactId;

}
