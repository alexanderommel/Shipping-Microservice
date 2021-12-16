package com.tongue.shippingservice;

import com.tongue.shippingservice.services.CourierWsSessionHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpSession;
import org.springframework.messaging.simp.user.SimpSubscription;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CourierWsSessionHandlerUnitTest {

    private CourierWsSessionHandler wsSessionHandler;

    @Mock
    private SimpUserRegistry userRegistry;
    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    public static String shippingCouriersSubscriptionDestination;

    @Mock
    SimpSubscription shippingCouriersSubscription;
    @Mock
    SimpSubscription randomSubscription;
    @Mock
    SimpSession hasShippingCouriersSub;
    @Mock
    SimpSession hasWhateverSub;
    @Mock
    SimpSession hasBothSubscriptions;
    @Mock
    SimpUser bunny;
    @Mock
    SimpUser dummy;
    @Mock
    SimpUser funny;

    @Autowired
    public void setCouriersSubDestination(@Value("${shipping.stomp.couriers.deliver}") String value){
        this.shippingCouriersSubscriptionDestination = value;
    }

    @Before
    public void setUp(){
        /** Create some sessions **/
        Mockito.when(shippingCouriersSubscription.getDestination()).thenReturn(shippingCouriersSubscriptionDestination);
        Mockito.when(randomSubscription.getDestination()).thenReturn("whatever");
        Set<SimpSubscription> subscriptions1 = Set.of(shippingCouriersSubscription);
        Set<SimpSubscription> subscriptions2 = Set.of(randomSubscription);
        Set<SimpSubscription> subscriptions3 = Set.of(shippingCouriersSubscription,randomSubscription);
        Mockito.when(hasShippingCouriersSub.getSubscriptions()).thenReturn(subscriptions1);
        Mockito.when(hasWhateverSub.getSubscriptions()).thenReturn(subscriptions2);
        Mockito.when(hasBothSubscriptions.getSubscriptions()).thenReturn(subscriptions3);
        /** Create some users and attach a set of only one session **/
        Mockito.when(bunny.getName()).thenReturn("bunny");
        Mockito.when(bunny.getSessions()).thenReturn(Set.of(hasShippingCouriersSub));
        Mockito.when(dummy.getName()).thenReturn("dummy");
        Mockito.when(dummy.getSessions()).thenReturn(Set.of(hasWhateverSub));
        Mockito.when(funny.getName()).thenReturn("funny");
        Mockito.when(funny.getSessions()).thenReturn(Set.of(hasBothSubscriptions));
        /** Add everything to UserRegistry **/
        Mockito.when(userRegistry.getUsers()).thenReturn(Set.of(bunny,dummy,funny));
        /** Tested class**/
        wsSessionHandler = new CourierWsSessionHandler(userRegistry,simpMessagingTemplate,shippingCouriersSubscriptionDestination);
    }

    @Test
    public void givenThatTwoUsersAreSubscribedToExpectedDestination_whenRetrievingUsers_ThenListSizeMustBeTwo(){
        int expected = 2;
        String expectedDestination = shippingCouriersSubscriptionDestination;
        List<SimpUser> simpUserList =
                wsSessionHandler.getAllSimpUsersSubscribedTo(expectedDestination);
        assertEquals(expected, simpUserList.size());
    }

    @Test
    public void givenThatZeroUsersAreSubscribedToExpectedDestination_whenRetrievingUsers_thenListSizeMustBeZero(){
        int expected = 0;
        String expectedDestination = "/expected/queue/hello";
        List<SimpUser> simpUserList =
                wsSessionHandler.getAllSimpUsersSubscribedTo(expectedDestination);
        assertEquals(expected, simpUserList.size());
    }


}