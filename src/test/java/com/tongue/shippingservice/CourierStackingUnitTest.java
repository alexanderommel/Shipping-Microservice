package com.tongue.shippingservice;

import com.tongue.shippingservice.domain.Courier;
import com.tongue.shippingservice.domain.Position;
import com.tongue.shippingservice.services.CourierStacking;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import static org.junit.Assert.*;

public class CourierStackingUnitTest {

    @Test
    public void givenTwoCouriers_whenEuclideanStacking_thenOneIsFirst(){
        int max_couriers=2;
        String expected = "one";
        CourierStacking stacking = new CourierStacking();
        Position pos1 = Position.builder().latitude("0").longitude("0").build();
        Position origin = pos1;
        Position pos2 = Position.builder().latitude("1.222").longitude("1.1222").build();
        Courier courier1 = new Courier();
        courier1.setUsername("one");
        courier1.setPosition(pos1);
        Courier courier2 = new Courier();
        courier2.setUsername("two");
        courier2.setPosition(pos2);
        List<Courier> courierList = Arrays.asList(courier1,courier2);
        Stack<Courier> stack =
                stacking.basicStackingByEuclideanDistance(courierList,origin.geometricPoint(),max_couriers);
        assertEquals(expected,stack.pop().getUsername());
    }

    @Test
    public void givenTwoCouriers_whenEuclideanStacking_thenStackSizeIsOne(){
        int max_couriers=1;
        int expected = 1;
        CourierStacking stacking = new CourierStacking();
        Position pos1 = Position.builder().latitude("0").longitude("0").build();
        Position origin = pos1;
        Position pos2 = Position.builder().latitude("1.222").longitude("1.1222").build();
        Courier courier1 = new Courier();
        courier1.setUsername("one");
        courier1.setPosition(pos1);
        Courier courier2 = new Courier();
        courier2.setUsername("two");
        courier2.setPosition(pos2);
        List<Courier> courierList = Arrays.asList(courier1,courier2);
        Stack<Courier> stack =
                stacking.basicStackingByEuclideanDistance(courierList,origin.geometricPoint(),max_couriers);
        assertEquals(expected,stack.size());
    }

    @Test
    public void givenTwoCouriers_whenEuclideanStacking_thenStackSizeIsTwo(){
        int max_couriers=2;
        int expected = 2;
        CourierStacking stacking = new CourierStacking();
        Position pos1 = Position.builder().latitude("0").longitude("0").build();
        Position origin = pos1;
        Position pos2 = Position.builder().latitude("1.222").longitude("1.1222").build();
        Courier courier1 = new Courier();
        courier1.setUsername("one");
        courier1.setPosition(pos1);
        Courier courier2 = new Courier();
        courier2.setUsername("two");
        courier2.setPosition(pos2);
        List<Courier> courierList = Arrays.asList(courier1,courier2);
        Stack<Courier> stack =
                stacking.basicStackingByEuclideanDistance(courierList,origin.geometricPoint(),max_couriers);
        assertEquals(expected,stack.size());
    }
}