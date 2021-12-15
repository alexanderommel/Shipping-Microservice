package com.tongue.shippingservice;

import com.tongue.shippingservice.domain.Courier;
import com.tongue.shippingservice.domain.Position;
import com.tongue.shippingservice.services.CourierFiltering;
import com.tongue.shippingservice.services.GeometricCalculation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Point;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class CourierFilteringUnitTest {

    private CourierFiltering courierFiltering;

    @Before
    public void setUp(){
        GeometricCalculation geometricCalculation = new GeometricCalculation();
        courierFiltering = new CourierFiltering(geometricCalculation);
    }

    @Test
    public void givenTwoCouriers_whenFiltering_thenOneCourierPassTheFilter(){
        Position pos1 = Position.builder().latitude("0").longitude("0").build();
        Position origin = pos1;
        Position pos2 = Position.builder().latitude("100").longitude("100").build();
        Courier courier1 = new Courier();
        courier1.setPosition(pos1);
        Courier courier2 = new Courier();
        courier2.setPosition(pos2);
        List<Courier> courierList = Arrays.asList(courier1,courier2);
        courierList = courierFiltering.basicCircularFiltering(courierList,origin,1F);
        assertEquals(courierList.size(),1);
    }

    @Test
    public void givenTwoCouriers_whenFiltering_thenTwoCourierPassTheFilter(){
        Position pos1 = Position.builder().latitude("0").longitude("0").build();
        Position origin = pos1;
        Position pos2 = Position.builder().latitude("0.12").longitude("0.1").build();
        Courier courier1 = new Courier();
        courier1.setPosition(pos1);
        Courier courier2 = new Courier();
        courier2.setPosition(pos2);
        List<Courier> courierList = Arrays.asList(courier1,courier2);
        courierList = courierFiltering.basicCircularFiltering(courierList,origin,1F);
        assertEquals(courierList.size(),2);
    }

}