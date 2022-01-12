package com.tongue.shippingservice.messaging.domain;

import com.tongue.shippingservice.domain.Artifact;
import com.tongue.shippingservice.domain.Position;
import com.tongue.shippingservice.domain.Shipping;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShippingRequest {

    private Artifact artifact;
    private String shippingFeeToken;
    private Shipping.Billing billing;
    private Position origin;
    private Position destination;

}
