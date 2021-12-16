package com.tongue.shippingservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShippingNotification {

    private Position origin;
    private String shippingId;
    private String artifactId;
    private String artifactResource;
    private String accessToken;

}
