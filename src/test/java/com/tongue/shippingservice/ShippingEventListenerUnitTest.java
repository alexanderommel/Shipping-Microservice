package com.tongue.shippingservice;

import com.tongue.shippingservice.domain.Courier;
import com.tongue.shippingservice.domain.Shipping;
import com.tongue.shippingservice.messaging.ShippingEventListener;
import com.tongue.shippingservice.messaging.events.ShippingCompletionEvent;
import com.tongue.shippingservice.messaging.events.ShippingContinuationEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ShippingEventListenerUnitTest {

    @Autowired
    ApplicationEventPublisher eventPublisher;
    @MockBean
    ShippingEventListener eventListener;

    @Test
    public void shouldHandleShippingCompletionEvent_whenEventIsPublished(){
        Shipping shipping = Shipping.builder().id(5L).build();
        Courier courier = Courier.builder().username("bunny").build();
        ShippingCompletionEvent event = new ShippingCompletionEvent(this,shipping, courier);
        eventPublisher.publishEvent(event);
        Mockito.verify(eventListener,Mockito.times(1))
                .handleShippingCompletionEvent(event);
    }

    @Test
    public void shouldHandleShippingContinuationEvent_whenEventIsPublished(){
        Shipping shipping = Shipping.builder().id(5L).build();
        ShippingContinuationEvent event = new ShippingContinuationEvent(this,shipping);
        eventPublisher.publishEvent(event);
        Mockito.verify(eventListener,Mockito.times(1))
                .handleShippingContinuationEvent(event);
    }
}
