package com.tongue.shippingservice.messaging.events;

import com.tongue.shippingservice.domain.Shipping;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 *
 * Published when the Courier decides to continue the already accepted shipment.
 *
 * **/
public class ShippingContinuationEvent extends ApplicationEvent {

    @Getter @Setter
    private Shipping shipping;

    public ShippingContinuationEvent(Object source, Shipping shipping){
        super(source);
        this.shipping=shipping;
    }
}
