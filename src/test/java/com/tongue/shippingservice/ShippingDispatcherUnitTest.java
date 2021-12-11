package com.tongue.shippingservice;

import com.tongue.shippingservice.domain.Courier;
import com.tongue.shippingservice.domain.DispatcherMessage;
import com.tongue.shippingservice.services.Dispatcher;
import org.junit.Before;
import org.junit.jupiter.api.Test;

import java.util.Stack;

import static org.junit.jupiter.api.Assertions.*;

public class ShippingDispatcherUnitTest {

    private Stack<Courier> stack;

    @Before
    public void setUp(){
        /** Configure a STOMP Client and create a stack of couriers**/
    }


    @Test
    public void givenThat_couriersNotResponding_when_notifyingDrivers_then_dispatchStatusIsNotFound(){
        Dispatcher dispatcher = new Dispatcher();
        DispatcherMessage message = dispatcher.notify(stack);
        DispatcherMessage.DispatchStatus status = message.getStatus();
        Boolean asserting = status== DispatcherMessage.DispatchStatus.NOT_FOUND;
        assertTrue(asserting);
    }

    @Test
    public void givenThat_courierResponds_when_notifyingDrivers_then_dispatchStatusIsDispatched(){
        Dispatcher dispatcher = new Dispatcher();
        DispatcherMessage message = dispatcher.notify(stack);
        DispatcherMessage.DispatchStatus status = message.getStatus();
        Boolean asserting = status== DispatcherMessage.DispatchStatus.NOT_FOUND;
        assertTrue(asserting);
    }

}
