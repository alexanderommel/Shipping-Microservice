package com.tongue.shippingservice.messaging;

import com.tongue.shippingservice.domain.Artifact;
import com.tongue.shippingservice.domain.Courier;
import com.tongue.shippingservice.domain.ShipmentAcceptation;
import com.tongue.shippingservice.domain.Shipping;
import com.tongue.shippingservice.domain.replication.Customer;
import com.tongue.shippingservice.messaging.events.ShippingRequestAcceptedEvent;
import com.tongue.shippingservice.messaging.events.ShippingRequestDeletedEvent;
import com.tongue.shippingservice.repositories.ShippingRepository;
import com.tongue.shippingservice.services.CourierSessionHandler;
import com.tongue.shippingservice.services.CustomerWsSessionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ShippingRequestEventListener {

    private CourierSessionHandler sessionHandler;
    private ShippingRepository shippingRepository;
    private ShippingQueuePublisher queuePublisher;
    private CustomerWsSessionHandler customerWsSessionHandler;

    public ShippingRequestEventListener(@Autowired CourierSessionHandler sessionHandler,
                                        @Autowired ShippingRepository shippingRepository,
                                        @Autowired ShippingQueuePublisher queuePublisher,
                                        @Autowired CustomerWsSessionHandler customerWsSessionHandler){

        this.sessionHandler=sessionHandler;
        this.queuePublisher=queuePublisher;
        this.shippingRepository=shippingRepository;
        this.customerWsSessionHandler=customerWsSessionHandler;
    }

    @Async
    @EventListener
    public void handleShippingRequestAcceptedEvent(ShippingRequestAcceptedEvent event){
        log.info("Handling ShippingRequestAcceptedEvent");
        Artifact artifact = event.getArtifact();
        Courier courier = event.getCourier();
        Long shippingId = event.getShippingId();
        sessionHandler.updateCourierStatus(Courier.status.BUSY, courier);
        sessionHandler.attachArtifact(artifact,courier);
        /** Send the courier info to the customer**/

        customerWsSessionHandler.sendShippingRequestStatus(
                Customer.builder().username(artifact.getOwner()).build(),
                Shipping.Status.ACCEPTED);

        /** Publishing the message to the respective Queue **/


    }

    @Async
    @EventListener
    public void handleShippingRequestDeletedEvent(ShippingRequestDeletedEvent event){
        Shipping shipping = event.getShipping();
        shipping.setStatus(Shipping.Status.CANCELLED_BY_SYSTEM);
        shipping.setDriver(null);
        shippingRepository.save(shipping);
    }


}
