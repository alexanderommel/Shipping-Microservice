package com.tongue.shippingservice.messaging.events;

import com.tongue.shippingservice.domain.Shipping;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 *
 * Published when there's an internal error when dispatching the Artifact
 *
 * **/

public class ShippingRequestDeletedEvent extends ApplicationEvent {

    @Getter @Setter
    private Shipping shipping;

    public ShippingRequestDeletedEvent(Object source, Shipping shipping){
        super(source);
        this.shipping=shipping;
    }
}
