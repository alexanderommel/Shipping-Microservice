package com.tongue.shippingservice.messaging.events;

import com.tongue.shippingservice.messaging.domain.ShippingRequestRejection;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/** Throw when a Shipping Request failed **/

public class ShippingRequestRejectedEvent extends ApplicationEvent {

    @Getter @Setter
    private ShippingRequestRejection requestRejection;

    public ShippingRequestRejectedEvent(Object source, ShippingRequestRejection requestRejection){
        super(source);
        this.requestRejection=requestRejection;
    }
}
