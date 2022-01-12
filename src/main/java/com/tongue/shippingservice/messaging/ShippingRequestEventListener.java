package com.tongue.shippingservice.messaging;

import com.tongue.shippingservice.domain.Artifact;
import com.tongue.shippingservice.domain.Courier;
import com.tongue.shippingservice.messaging.domain.ShipmentAcceptation;
import com.tongue.shippingservice.domain.Shipping;
import com.tongue.shippingservice.domain.replication.Customer;
import com.tongue.shippingservice.messaging.domain.ShippingRequest;
import com.tongue.shippingservice.messaging.domain.ShippingRequestRejection;
import com.tongue.shippingservice.messaging.events.ShippingRequestAcceptedEvent;
import com.tongue.shippingservice.messaging.events.ShippingRequestDeletedEvent;
import com.tongue.shippingservice.messaging.events.ShippingRequestRejectedEvent;
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

        ShipmentAcceptation acceptation = ShipmentAcceptation.builder()
                .shippingId(String.valueOf(shippingId))
                .artifactId(artifact.getArtifactId())
                .driverUsername(courier.getUsername())
                .build();

        queuePublisher.sendShippingAcceptedMessageToRabbitMQ(acceptation);

    }

    @Async
    @EventListener
    public void handleShippingRequestRejectedEvent(ShippingRequestRejectedEvent event){
        log.info("Handling ShippingRequestRejectedEvent");
        ShippingRequestRejection rejection = event.getRequestRejection();
        log.info("Rejection details: "+rejection);
        if (rejection.getReason()== ShippingRequestRejection.Reason.NOT_ACCEPTED){
            shippingRepository.delete(Shipping.builder().id(rejection.getShippingId()).build());
        }
        queuePublisher.sendShippingRequestRejectionToRabbitMQ(rejection);
    }


    /** Throw when corrupted calls are executed**/

    @Async
    @EventListener
    public void handleShippingRequestDeletedEvent(ShippingRequestDeletedEvent event){
        Shipping shipping = event.getShipping();
        shipping.setStatus(Shipping.Status.CANCELLED_BY_SYSTEM);
        shipping.setDriver(null);
        shippingRepository.save(shipping);
    }


}
