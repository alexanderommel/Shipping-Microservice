package com.tongue.shippingservice;

import com.tongue.shippingservice.domain.Courier;
import com.tongue.shippingservice.domain.DispatcherMessage;
import com.tongue.shippingservice.domain.Shipping;
import com.tongue.shippingservice.domain.replication.Driver;
import com.tongue.shippingservice.repositories.ShippingRepository;
import com.tongue.shippingservice.services.DispatchTracking;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class DispatchTrackingUnitTest {

    private DispatchTracking dispatchTracking;

    @Mock
    private ShippingRepository shippingRepository;

    private Shipping nullShipping;
    private Shipping statusNotReady=
            Shipping.builder().id(2L).status(Shipping.Status.PENDING).build();
    private Shipping statusAcceptedButNotNullDriver=
            Shipping.builder().id(3L).status(Shipping.Status.ACCEPTED).build();
    private Shipping validShipping =
            Shipping.builder().id(4L).status(Shipping.Status.ACCEPTED).
                    driver(Driver.builder().username("monster").build()).build();

    @Before
    public void setUp(){
        dispatchTracking = new DispatchTracking(shippingRepository);
        Mockito.when(shippingRepository.findById(2L)).thenReturn(java.util.Optional.ofNullable(statusNotReady));
        Mockito.when(shippingRepository.findById(3L)).thenReturn(java.util.Optional.ofNullable(statusAcceptedButNotNullDriver));
        Mockito.when(shippingRepository.findById(4L)).thenReturn(java.util.Optional.ofNullable(validShipping));
    }

    @Test
    public void givenNullShipping_whenTrackingShippingStatus_thenReturnINTERNALERRORStatus(){
        DispatcherMessage.DispatchStatus expected =
                DispatcherMessage.DispatchStatus.INTERNAL_ERROR;
        DispatcherMessage message =
                dispatchTracking.shippingStatus(nullShipping, Courier.builder().build());
        assertEquals(expected,message.getStatus());
    }

    @Test
    public void givenNoAcceptedShipping_whenTrackingShippingStatus_thenReturnNOTFOUNDStatus(){
        DispatcherMessage.DispatchStatus expected =
                DispatcherMessage.DispatchStatus.NOT_FOUND;
        DispatcherMessage message =
                dispatchTracking.shippingStatus(statusNotReady, Courier.builder().build());
        assertEquals(expected,message.getStatus());
    }

    @Test
    public void givenCorruptedShipping_whenTrackingShippingStatus_thenReturnINTERNALERRORStatus(){
        DispatcherMessage.DispatchStatus expected =
                DispatcherMessage.DispatchStatus.INTERNAL_ERROR;
        DispatcherMessage message =
                dispatchTracking.shippingStatus(statusAcceptedButNotNullDriver, Courier.builder().build());
        assertEquals(expected,message.getStatus());
    }

    @Test
    public void givenValidShipping_whenTrackingShippingStatus_thenReturnINTERNALERRORStatus(){
        DispatcherMessage.DispatchStatus expected =
                DispatcherMessage.DispatchStatus.DISPATCHED;
        DispatcherMessage message =
                dispatchTracking.shippingStatus(validShipping, Courier.builder().username("monster").build());
        assertEquals(expected,message.getStatus());
    }
}
