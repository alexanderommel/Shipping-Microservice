package com.tongue.shippingservice.messaging.events;

import com.tongue.shippingservice.domain.Artifact;
import com.tongue.shippingservice.domain.Courier;
import com.tongue.shippingservice.domain.Shipping;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 *
 * Published when a Courier accepts to deliver the artifact
 *
 * **/

public class ShippingRequestAcceptedEvent extends ApplicationEvent {

    @Getter @Setter
    private Artifact artifact;
    @Getter @Setter
    private Courier courier;
    @Getter @Setter
    private Long shippingId;

    public ShippingRequestAcceptedEvent(Object source, Artifact artifact, Courier courier,Long shippingId){
        super(source);
        this.artifact=artifact;
        this.courier=courier;
        this.shippingId=shippingId;
    }
}
