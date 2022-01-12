package com.tongue.shippingservice.messaging.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * Created when a shipping request couldn't be accepted
 *
 * **/

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShippingRequestRejection {

    private String artifactId;
    private Long shippingId;
    private Reason reason;
    private String details;

    public enum Reason{
        NO_DRIVERS_FOUND,NOT_ACCEPTED,BAD_TOKEN,TOKEN_EXPIRED,BAD_FORMAT
    }

}
