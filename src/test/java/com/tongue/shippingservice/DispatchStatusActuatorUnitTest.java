package com.tongue.shippingservice;

import com.tongue.shippingservice.domain.Shipping;
import com.tongue.shippingservice.repositories.ShippingRepository;
import com.tongue.shippingservice.services.CourierSessionHandler;
import com.tongue.shippingservice.services.DispatchStatusActuator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class DispatchStatusActuatorUnitTest {

    private DispatchStatusActuator actuator;
    @Mock
    private CourierSessionHandler sessionHandler;
    @Mock
    private ShippingRepository shippingRepository;

    @Before
    public void setUp(){
        actuator = new DispatchStatusActuator(sessionHandler,shippingRepository);
    }

    @Test
    public void givenCorruptedShipping_whenActuatingOnError_thenStatusIsModifiedToCANCELLEDBYSYSTEM(){
        Shipping.Status expected = Shipping.Status.CANCELLED_BY_SYSTEM;
        Shipping shipping = Shipping.builder().id(1l).status(Shipping.Status.ACCEPTED).build();
        Mockito.when(shippingRepository.save(shipping)).thenReturn(shipping);
        Shipping shipping1 = actuator.onInternalErrorShippingDispatch(shipping);
        assertEquals(shipping1.getStatus(),expected);
    }

}