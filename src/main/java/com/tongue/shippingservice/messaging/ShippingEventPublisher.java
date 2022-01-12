package com.tongue.shippingservice.messaging;

import com.tongue.shippingservice.domain.Artifact;
import com.tongue.shippingservice.domain.Courier;
import com.tongue.shippingservice.domain.Shipping;
import com.tongue.shippingservice.messaging.domain.ShippingRequest;
import com.tongue.shippingservice.messaging.domain.ShippingRequestRejection;
import com.tongue.shippingservice.messaging.events.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class ShippingEventPublisher {

    private final ApplicationEventPublisher publisher;

    ShippingEventPublisher(ApplicationEventPublisher publisher){
        this.publisher=publisher;
    }

    public void publishShippingRequestAccepted(Artifact artifact, Courier courier,Long shippingId){
        publisher.publishEvent(new ShippingRequestAcceptedEvent(this,artifact,courier,shippingId));
    }

    public void publishShippingRequestDeleted(Shipping shipping){
        publisher.publishEvent(new ShippingRequestDeletedEvent(this,shipping));
    }

    public void publishShippingContinuation(Shipping shipping){
        publisher.publishEvent(new ShippingContinuationEvent(this,shipping));
    }

    public void publishShippingCompletion(Shipping shipping, Courier courier){
        publisher.publishEvent(new ShippingCompletionEvent(this,shipping,courier));
    }

    public void publishShippingRequestRejection(ShippingRequestRejection requestRejection){
        publisher.publishEvent(new ShippingRequestRejectedEvent(this, requestRejection));
    }
}
