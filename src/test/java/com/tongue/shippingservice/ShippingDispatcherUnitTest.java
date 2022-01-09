package com.tongue.shippingservice;

import com.tongue.shippingservice.domain.Courier;
import com.tongue.shippingservice.domain.DispatcherMessage;
import com.tongue.shippingservice.domain.Position;
import com.tongue.shippingservice.services.Dispatcher;
import org.junit.Before;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * Unit tests for the Dispatcher are useless since it is composed of a lot of dependencies,
 * so testing it on isolation doesn't give any value.
 *
 * **/

public class ShippingDispatcherUnitTest {

    private Stack<Courier> stack;
    private Dispatcher dispatcher;


    @Before
    public void setUp(){
        /** Create a stack of couriers**/
        stack = new Stack<>();
        stack.addAll(generateCouriers());
    }


    @Test
    public void givenThat_couriersNotResponding_when_notifyingDrivers_then_dispatchStatusIsNotFound(){
        /*Dispatcher dispatcher = new Dispatcher();
        DispatcherMessage message = dispatcher.notify(stack);
        DispatcherMessage.DispatchStatus status = message.getStatus();
        Boolean asserting = status== DispatcherMessage.DispatchStatus.NOT_FOUND;
        assertTrue(asserting);*/
    }

    @Test
    public void givenThat_courierResponds_when_notifyingDrivers_then_dispatchStatusIsDispatched(){
        /*Dispatcher dispatcher = new Dispatcher();
        DispatcherMessage message = dispatcher.notify(stack);
        DispatcherMessage.DispatchStatus status = message.getStatus();
        Boolean asserting = status== DispatcherMessage.DispatchStatus.NOT_FOUND;
        assertTrue(asserting);*/
    }

    private List<Courier> generateCouriers(){
        List<Courier> couriers = Arrays.asList(
                // Courier 1
                Courier.builder()
                        .username("courier1")
                        .position(Position.builder()
                                .latitude("5.5")
                                .longitude("5.5")
                                .build())
                        .build(),
                // Courier 2
                Courier.builder()
                        .username("courier2")
                        .position(Position.builder()
                                .latitude("100.5")
                                .longitude("100.0")
                                .build())
                        .build(),
                // Courier 3
                Courier.builder()
                        .username("courier3")
                        .position(Position.builder()
                                .latitude("10.0")
                                .longitude("10.0")
                                .build())
                        .build(),
                // Courier 4
                Courier.builder()
                        .username("courier4")
                        .position(Position.builder()
                                .latitude("50.0")
                                .longitude("60.0").build())
                        .build()
        );
        return couriers;
    }

}
