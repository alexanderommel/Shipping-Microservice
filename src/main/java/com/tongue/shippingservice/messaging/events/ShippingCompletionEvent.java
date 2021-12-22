package com.tongue.shippingservice.messaging.events;

import com.tongue.shippingservice.domain.Courier;
import com.tongue.shippingservice.domain.Shipping;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 *
 * Published when the Courier decides to finish the already accepted shipment.
 *
 * **/
public class ShippingCompletionEvent extends ApplicationEvent {

    @Getter @Setter
    private Shipping shipping;
    @Getter @Setter
    private Courier courier;

    public ShippingCompletionEvent(Object source, Shipping shipping, Courier courier){
        super(source);
        this.shipping=shipping;
        this.courier=courier;
    }
}
