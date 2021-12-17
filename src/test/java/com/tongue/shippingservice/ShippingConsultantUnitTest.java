package com.tongue.shippingservice;

import com.tongue.shippingservice.domain.Position;
import com.tongue.shippingservice.services.ShippingConsultant;
import org.junit.jupiter.api.Test;
import org.springframework.data.geo.Distance;

import java.math.BigDecimal;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

public class ShippingConsultantUnitTest {

    @Test
    public void givenOriginAndDestinationPositions_whenComputingFee_thenFeeIsEqualToExpected(){
        BigDecimal expected = BigDecimal.valueOf(1.50);
        ShippingConsultant consultant = new ShippingConsultant();
        Position origin = Position.builder().latitude("125.11").longitude("98.102").build();
        Position destination = Position.builder().latitude("211.5").longitude("205.25").build();
        BigDecimal fee = consultant.shippingFee(origin,destination);
        assertEquals(expected,fee);
    }

    @Test
    public void givenOriginAndDestinationPositions_whenEstimatingDistance_thenDistanceIsAroundToExpected(){
        Double expected = 10.0; // Meters
        Double limit = 10.10;
        ShippingConsultant consultant = new ShippingConsultant();
        Position origin = Position.builder().latitude("125.11").longitude("98.102").build();
        Position destination = Position.builder().latitude("211.5").longitude("205.25").build();
        Distance distance = consultant.tripDistance(origin,destination);
        Double difference = Math.abs(expected-distance.getValue());
        Boolean delta = difference>limit;
        assertFalse(delta);
    }

    @Test
    public void givenOriginAndDestinationPositions_whenEstimatingTime_thenExpectedTimeIsAroundExpected(){
        Double expected = 40.0; // Minutes
        Double limit = 20.0;
        ShippingConsultant consultant = new ShippingConsultant();
        Position origin = Position.builder().latitude("125.11").longitude("98.102").build();
        Position destination = Position.builder().latitude("211.5").longitude("205.25").build();
        LocalTime time = consultant.arrivalTime(origin,destination);
        Double difference = Math.abs(expected-time.getMinute());
        Boolean delta = difference>limit;
        assertFalse(delta);
    }
}
