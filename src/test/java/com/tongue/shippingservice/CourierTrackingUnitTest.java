package com.tongue.shippingservice;

import com.tongue.shippingservice.domain.Courier;
import com.tongue.shippingservice.domain.Position;
import com.tongue.shippingservice.domain.SearchParameters;
import com.tongue.shippingservice.services.CourierTracking;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import java.util.Stack;
import static org.junit.jupiter.api.Assertions.*;

public class CourierTrackingUnitTest {

    @Before
    public void setUp(){
        /** Register a set of couriers**/
    }

    @Test
    public void givenThat_courierPositionIsNearToOrigin_when_searchingDrivers_then_returnFilledStack(){
        Position origin = Position.builder().latitude("10.4").longitude("10.2").build();
        SearchParameters params = new SearchParameters();
        CourierTracking courierTracking = new CourierTracking();
        Stack<Courier> couriers = courierTracking.searchNearestDrivers(origin, params);
        assertFalse(couriers.isEmpty());
    }

    @Test
    public void givenThat_courierPositionIsFarFromOrigin_when_searchingDrivers_then_returnEmptyStack(){
        Position origin = Position.builder().latitude("10.4").longitude("10.2").build();
        SearchParameters params = new SearchParameters();
        CourierTracking courierTracking = new CourierTracking();
        Stack<Courier> couriers = courierTracking.searchNearestDrivers(origin, params);
        assertTrue(couriers.isEmpty());
    }
}
