package com.tongue.shippingservice.messaging;

import com.tongue.shippingservice.domain.Courier;
import com.tongue.shippingservice.domain.Shipping;
import com.tongue.shippingservice.messaging.events.ShippingCompletionEvent;
import com.tongue.shippingservice.messaging.events.ShippingContinuationEvent;
import com.tongue.shippingservice.repositories.ShippingRepository;
import com.tongue.shippingservice.services.CourierSessionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ShippingEventListener {

    private CourierSessionHandler sessionHandler;
    private ShippingRepository shippingRepository;
    private ShippingQueuePublisher queuePublisher;

    public ShippingEventListener(@Autowired CourierSessionHandler sessionHandler,
                                 @Autowired ShippingRepository shippingRepository,
                                 @Autowired ShippingQueuePublisher queuePublisher){

        this.sessionHandler=sessionHandler;
        this.shippingRepository=shippingRepository;
        this.queuePublisher=queuePublisher;
    }

    @Async
    @EventListener
    public void handleShippingContinuationEvent(ShippingContinuationEvent event){
        Shipping shipping = event.getShipping();
        shipping.setStatus(Shipping.Status.CONFIRMED);
        shippingRepository.save(shipping);
    }

    @Async
    @EventListener
    public void handleShippingCompletionEvent(ShippingCompletionEvent event){
        Shipping shipping = event.getShipping();
        Courier courier = event.getCourier();
        shipping.setStatus(Shipping.Status.FINISHED);
        shippingRepository.save(shipping);
        sessionHandler.updateCourierStatus(Courier.status.READY,courier);
        sessionHandler.removeArtifact(courier);

        /** Publishing the message to the respective Queue **/


    }

}
