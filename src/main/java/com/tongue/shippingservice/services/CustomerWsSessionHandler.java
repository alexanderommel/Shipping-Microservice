package com.tongue.shippingservice.services;

import com.tongue.shippingservice.domain.Courier;
import com.tongue.shippingservice.domain.Position;
import com.tongue.shippingservice.domain.Shipping;
import com.tongue.shippingservice.domain.ShippingNotification;
import com.tongue.shippingservice.domain.replication.Customer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CustomerWsSessionHandler {

    private SimpMessagingTemplate simpMessagingTemplate;
    public String shippingGeolocationSubscriptionDestination;
    public String shippingCandidatePositionDestination;
    private String shippingRequestStatusDestination;

    public CustomerWsSessionHandler(@Autowired SimpMessagingTemplate simpMessagingTemplate,
                                   @Value("${shipping.stomp.customers.geolocation}") String shippingGeolocationDest,
                                    @Value("${shipping.stomp.candidate.position}") String shippingCandidateDest,
                                    @Value("${shipping.stomp.request.status}") String requestStatusDest){

        this.simpMessagingTemplate=simpMessagingTemplate;
        this.shippingGeolocationSubscriptionDestination=shippingGeolocationDest;
        this.shippingCandidatePositionDestination=shippingCandidateDest;
        this.shippingRequestStatusDestination=requestStatusDest;

    }

    public void sendShippingPositionToCustomer(Position position,
                                               Customer customer,
                                               String destination){
        log.info("Sending Shipping Position to user '"
                +customer.getUsername()
                +"' with destination '"+destination+"'");

        this.simpMessagingTemplate.
                convertAndSendToUser(customer.getUsername(), destination, position);
    }

    public void sendCandidatePositionToCustomer(Position position,
                                                Customer customer){

        log.info("Sending Shipping Candidate Position to user '"+customer.getUsername()+"'");
        log.info("Position: "+position);

        this.simpMessagingTemplate.
                convertAndSendToUser(customer.getUsername(),shippingCandidatePositionDestination,position);

    }

    public void sendShippingRequestStatus(Customer customer,
                                          Shipping.Status status){

        log.info("Sending Shipping Status to user '"+customer.getUsername()+"'");
        log.info("Status: "+status);

        this.simpMessagingTemplate
                .convertAndSendToUser(customer.getUsername(),shippingRequestStatusDestination,status);
    }


}
