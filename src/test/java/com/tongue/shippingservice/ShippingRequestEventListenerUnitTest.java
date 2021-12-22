package com.tongue.shippingservice;

import com.tongue.shippingservice.domain.Artifact;
import com.tongue.shippingservice.domain.Courier;
import com.tongue.shippingservice.domain.Shipping;
import com.tongue.shippingservice.messaging.ShippingEventPublisher;
import com.tongue.shippingservice.messaging.ShippingQueuePublisher;
import com.tongue.shippingservice.messaging.ShippingRequestEventListener;
import com.tongue.shippingservice.messaging.events.ShippingRequestAcceptedEvent;
import com.tongue.shippingservice.messaging.events.ShippingRequestDeletedEvent;
import com.tongue.shippingservice.repositories.ShippingRepository;
import com.tongue.shippingservice.services.CourierSessionHandler;
import com.tongue.shippingservice.services.CustomerWsSessionHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ShippingRequestEventListenerUnitTest {

    @Autowired
    ApplicationEventPublisher eventPublisher;
    @MockBean
    ShippingRequestEventListener requestEventListener;


    @Test
    public void shouldHandleShippingRequestAcceptedEvent_whenEventIsPublished(){
        Artifact artifact = Artifact.builder().artifactId("123").owner("alexis").build();
        Courier courier = Courier.builder().username("alexis").build();
        Long shippingId = 3l;
        ShippingRequestAcceptedEvent event = new ShippingRequestAcceptedEvent(this,artifact,courier,shippingId);
        eventPublisher.publishEvent(event);
        Mockito.verify(requestEventListener,Mockito.times(1))
                .handleShippingRequestAcceptedEvent(event);
    }

    @Test
    public void shouldHandleShippingRequestDeletedEvent_whenEventIsPublished(){
        Shipping shipping = Shipping.builder().id(1l).build();
        ShippingRequestDeletedEvent event = new ShippingRequestDeletedEvent(this,shipping);
        eventPublisher.publishEvent(event);
        Mockito.verify(requestEventListener,Mockito.times(1))
                .handleShippingRequestDeletedEvent(event);
    }


}
