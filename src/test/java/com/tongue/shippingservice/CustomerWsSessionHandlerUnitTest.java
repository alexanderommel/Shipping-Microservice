package com.tongue.shippingservice;

import com.tongue.shippingservice.domain.Position;
import com.tongue.shippingservice.domain.Shipping;
import com.tongue.shippingservice.domain.replication.Customer;
import com.tongue.shippingservice.services.CustomerWsSessionHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@RunWith(MockitoJUnitRunner.class)
public class CustomerWsSessionHandlerUnitTest {

    CustomerWsSessionHandler customerWsSessionHandler;

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;
    private String shippingGeolocationDest="a";
    private String shippingCandidateDest="b";
    private String requestStatusDest="c";



    @Before
    public void setUp(){
        this.customerWsSessionHandler = new CustomerWsSessionHandler(
                simpMessagingTemplate
                ,shippingGeolocationDest
                ,shippingCandidateDest
                ,requestStatusDest);
    }

    @Test
    public void shouldInvokeOneTimeConvertAndSendToUser_givenThatWsSessionHandlerIsSendingRequestStatus(){
        Customer customer = Customer.builder().username("dani").build();
        customerWsSessionHandler.sendShippingRequestStatus(customer, Shipping.Status.ACCEPTED);
        Mockito.verify(simpMessagingTemplate,Mockito.times(1)).convertAndSendToUser("dani",
                requestStatusDest,
                Shipping.Status.ACCEPTED);
    }

    @Test
    public void shouldInvokeOneTimeConvertAndSendToUser_givenThatWsSessionHandlerIsSendingShippingPosition(){
        Customer customer = Customer.builder().username("alex").build();
        Position pos = Position.builder().latitude("12.2").longitude("1.11").build();
        customerWsSessionHandler.sendShippingPositionToCustomer(pos, customer, shippingGeolocationDest);
        Mockito.verify(simpMessagingTemplate,Mockito.times(1)).convertAndSendToUser("alex",
                shippingGeolocationDest,
                pos);
    }

    @Test
    public void shouldInvokeOneTimeConvertAndSendToUser_givenThatWsSessionHandlerIsSendingCandidatePosition(){
        Customer customer = Customer.builder().username("alex1").build();
        Position pos = Position.builder().latitude("12.2").longitude("1.11").build();
        customerWsSessionHandler.sendCandidatePositionToCustomer(pos, customer);
        Mockito.verify(simpMessagingTemplate,Mockito.times(1)).convertAndSendToUser("alex1",
                shippingCandidateDest,
                pos);
    }


}
