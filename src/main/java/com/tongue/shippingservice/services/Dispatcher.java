package com.tongue.shippingservice.services;

import com.tongue.shippingservice.domain.*;
import com.tongue.shippingservice.domain.replication.Driver;
import com.tongue.shippingservice.repositories.ShippingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.scheduling.annotation.Async;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalTime;
import java.util.Optional;
import java.util.Stack;

@Service
@Slf4j
public class Dispatcher {

    @Value("${shipping.stomp.couriers.deliver}")
    private String couriersSubscriptionDestination;
    private ShippingTokenSupplier tokenSupplier;
    private DispatchTracking dispatchTracking;
    private DispatchStatusActuator statusActuator;
    private CourierWsSessionHandler wsSessionHandler;
    public Dispatcher(@Autowired ShippingTokenSupplier tokenSupplier,
                      @Autowired DispatchTracking dispatchTracking,
                      @Autowired DispatchStatusActuator statusActuator,
                      @Autowired CourierWsSessionHandler wsSessionHandler){

        this.tokenSupplier=tokenSupplier;
        this.dispatchTracking=dispatchTracking;
        this.statusActuator=statusActuator;
        this.wsSessionHandler=wsSessionHandler;

    }

    @Async
    public DispatcherMessage dispatchShipping(Stack<Courier> couriers,
                                              Shipping shipping,
                                              DispatchParameters parameters){

        DispatcherMessage dispatcherMessage =
                DispatcherMessage.builder().status(DispatcherMessage.DispatchStatus.NOT_FOUND).build();

        log.info("DispatchParameters="+parameters.toString());
        log.info("Declaring ShippingNotification");
        ShippingNotification notification = ShippingNotification.builder().
                shippingId(String.valueOf(shipping.getId())).
                artifactId(String.valueOf(shipping.getArtifactId())).
                origin(shipping.getOrigin()).
                artifactResource(shipping.getResource()).build();

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
            /** STOMP Publication**/
            wsSessionHandler.sendShippingNotificationToSubscribedCourier(notification,
                    courier,
                    couriersSubscriptionDestination);
            /** **/
            wait(parameters);
            dispatcherMessage = dispatchTracking.shippingStatus(shipping,courier);
            DispatcherMessage.DispatchStatus status = dispatcherMessage.getStatus();
            if (status== DispatcherMessage.DispatchStatus.INTERNAL_ERROR){
                statusActuator.onInternalErrorShippingDispatch(shipping);
                break;
            }
            if (status== DispatcherMessage.DispatchStatus.NOT_FOUND){
                continue;
            }
            if (status== DispatcherMessage.DispatchStatus.DISPATCHED){
                statusActuator.onSuccessfulShippingDispatch(courier);
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
