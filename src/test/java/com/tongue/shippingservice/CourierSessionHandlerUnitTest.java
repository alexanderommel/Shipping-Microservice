package com.tongue.shippingservice;

import com.tongue.shippingservice.domain.Courier;
import com.tongue.shippingservice.domain.Position;
import com.tongue.shippingservice.services.CourierSessionHandler;
import com.tongue.shippingservice.services.CourierWsSessionHandler;
import org.hibernate.internal.AbstractSessionImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import java.util.*;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class CourierSessionHandlerUnitTest {

    @Mock
    private FindByIndexNameSessionRepository sessionRepository;
    @Mock
    private CourierWsSessionHandler wsSessionHandler;
    private CourierSessionHandler sessionHandler;
    private Map<String,  Session> sessionsBunny;
    private Map<String, Session> sessionsDummy;
    @Mock
    private Session statusReadySession;
    @Mock
    private Session statusBusySession;
    @Mock
    private SimpUser bunny;
    @Mock
    private SimpUser dummy;

    @Before
    public void setting(){
        sessionsBunny = new HashMap<>();
        sessionsDummy = new HashMap<>();
        Mockito.when(statusReadySession.getAttribute("STATUS")).thenReturn(Courier.status.READY);
        Mockito.when(statusReadySession.getAttribute("POSITION")).thenReturn(Position.builder().build());
        Mockito.when(statusBusySession.getAttribute("STATUS")).thenReturn(Courier.status.BUSY);
        Mockito.when(statusBusySession.getAttribute("POSITION")).thenReturn(Position.builder().build());
        sessionsBunny.put("session",statusReadySession);
        sessionsDummy.put("session",statusBusySession);
        this.sessionHandler = new CourierSessionHandler(sessionRepository,wsSessionHandler);
        Mockito.when(sessionRepository.findByPrincipalName("bunny")).thenReturn(sessionsBunny);
        Mockito.when(sessionRepository.findByPrincipalName("dummy")).thenReturn(sessionsDummy);
        Mockito.when(dummy.getName()).thenReturn("bunny");
        Mockito.when(bunny.getName()).thenReturn("dummy");
        List<SimpUser> simpUsers = Arrays.asList(dummy,bunny);
        Mockito.when(wsSessionHandler.getAll()).thenReturn(simpUsers);
    }

    @Test
    public void shouldReturnOnlyOneCourier_givenThatIsTheOnlyOneWithStatusReady(){
        int expected = 1;
        List<Courier> couriers = sessionHandler.getAllCouriersWithStatus(Courier.status.READY);
        assertEquals(expected,couriers.size());
    }

    @Test
    public void shouldNotReturnAnyCourier_givenThatNobodyHasStatusReady(){
        int expected = 0;
        Mockito.when(statusReadySession.getAttribute("STATUS")).thenReturn(Courier.status.BUSY);
        List<Courier> couriers = sessionHandler.getAllCouriersWithStatus(Courier.status.READY);
        assertEquals(expected,couriers.size());
    }
}