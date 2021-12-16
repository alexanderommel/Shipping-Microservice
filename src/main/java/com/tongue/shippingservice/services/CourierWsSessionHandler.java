package com.tongue.shippingservice.services;

import com.tongue.shippingservice.core.utils.SimpUtils;
import com.tongue.shippingservice.domain.Courier;
import com.tongue.shippingservice.domain.ShippingNotification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpSession;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CourierWsSessionHandler {

    private SimpUserRegistry userRegistry;
    private SimpMessagingTemplate simpMessagingTemplate;
    public  String shippingCourierSubscriptionDestination;


    public CourierWsSessionHandler(@Autowired SimpUserRegistry userRegistry,
                                   @Autowired SimpMessagingTemplate simpMessagingTemplate,
                                   @Value("${shipping.stomp.couriers.deliver}") String shippingCourierDest){
        this.userRegistry=userRegistry;
        this.shippingCourierSubscriptionDestination=shippingCourierDest;
    }

    public void sendShippingNotificationToSubscribedCourier(ShippingNotification notification,
                                                            Courier courier,
                                                            String destination){
        log.info("Sending ShippingNotification to user '"
                +courier.getUsername()
                +"' to destination '"+destination+"'");

        this.simpMessagingTemplate.
                convertAndSendToUser(courier.getUsername(), destination, notification);
    }

    public List<SimpUser> getAllSimpUsersSubscribedTo(String destination){
        log.info("Getting all users subscribed to: "+destination);

        List<SimpUser> subscribedUsers1 =
                userRegistry.getUsers().stream()
                        .filter(simpUser -> simpUser.getSessions().stream()
                                .filter((SimpSession s) -> s.getSubscriptions().stream()
                                        .filter(subscription -> subscription.getDestination()
                                                .equalsIgnoreCase(destination))
                                        .count()>=1)
                                .count()>=1)
                        .collect(Collectors.toList());

        return subscribedUsers1;
    }

    public List<SimpUser> getAll(){
        List<SimpUser> simpUsers = userRegistry.getUsers().stream().collect(Collectors.toList());
        return simpUsers;
    }
}
