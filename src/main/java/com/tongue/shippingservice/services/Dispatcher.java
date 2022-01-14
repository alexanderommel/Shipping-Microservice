package com.tongue.shippingservice.services;

import com.tongue.shippingservice.domain.*;
import com.tongue.shippingservice.domain.replication.Customer;
import com.tongue.shippingservice.messaging.ShippingEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Stack;

@Service
@Slf4j
public class Dispatcher {


    private String couriersSubscriptionDestination;
    private ShippingTokenSupplier tokenSupplier;
    private DispatchTracking dispatchTracking;
    private ShippingEventPublisher shippingEventPublisher;
    private CourierWsSessionHandler wsSessionHandler;
    private CustomerWsSessionHandler customerWsSessionHandler;

    public Dispatcher(@Autowired ShippingTokenSupplier tokenSupplier,
                      @Autowired DispatchTracking dispatchTracking,
                      @Autowired ShippingEventPublisher shippingEventPublisher,
                      @Autowired CourierWsSessionHandler wsSessionHandler,
                      @Autowired CustomerWsSessionHandler customerWsSessionHandler,
                      @Value("${shipping.stomp.couriers.deliver}") String couriersSubscriptionDestination){

        this.tokenSupplier=tokenSupplier;
        this.dispatchTracking=dispatchTracking;
        this.shippingEventPublisher = shippingEventPublisher;
        this.wsSessionHandler=wsSessionHandler;
        this.customerWsSessionHandler=customerWsSessionHandler;
        this.couriersSubscriptionDestination=couriersSubscriptionDestination;

    }

    public DispatcherMessage dispatchShipping(Stack<Courier> couriers,
                                              Shipping shipping,
                                              DispatchParameters parameters){

        DispatcherMessage dispatcherMessage =
                DispatcherMessage.builder().status(DispatcherMessage.DispatchStatus.NOT_FOUND).build();

        log.info("DispatchParameters="+parameters.toString());
        log.info("Declaring ShippingNotification");
        ShippingNotification notification = ShippingNotification.builder().
                shippingId(String.valueOf(shipping.getId())).
                artifactId(String.valueOf(shipping.getArtifact().getArtifactId())).
                origin(shipping.getOrigin()).
                artifactResource(shipping.getArtifact().getResource()).build();

        for (Courier courier:
             couriers) {
            log.info("Notifying {User:"+courier.getUsername()+"}");
            LocalTime expiration = LocalTime.now();
            expiration.plusSeconds(parameters.getIntervalSeconds());
            TemporalAccessToken accessToken = tokenSupplier.createBase64TemporalAccessToken(
                    courier.getUsername(),
                    expiration
            );
            notification.setAccessToken(accessToken.getBase64Encoding());
            /** Send shipping request notification to courier and send courier pos to customer **/
            wsSessionHandler.sendShippingNotificationToSubscribedCourier(notification,
                    courier,
                    couriersSubscriptionDestination);
            Customer customer = Customer.builder().username(shipping.getArtifact().getOwner()).build();
            customerWsSessionHandler.sendCandidatePositionToCustomer(courier.getPosition(),customer);

            /** Accepted? **/
            wait(parameters);
            dispatcherMessage = dispatchTracking.shippingStatus(shipping,courier);
            DispatcherMessage.DispatchStatus status = dispatcherMessage.getStatus();
            if (status== DispatcherMessage.DispatchStatus.INTERNAL_ERROR){
                shippingEventPublisher.publishShippingRequestDeleted(shipping);
                break;
            }
            if (status== DispatcherMessage.DispatchStatus.DISPATCHED){
                shippingEventPublisher.publishShippingRequestAccepted(shipping.getArtifact(),courier, shipping.getId());
                return dispatcherMessage;
            }
        }
        return dispatcherMessage;
    }

    private void wait(DispatchParameters parameters){
        try {
            log.info("Thread Sleep: "+parameters.getIntervalSeconds()+"[s]");
            int ms = parameters.getIntervalSeconds()*1000;
            ms = ms - (parameters.getK()*1000);
            Thread.sleep(ms);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }
}
