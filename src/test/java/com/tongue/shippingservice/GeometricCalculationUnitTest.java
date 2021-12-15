package com.tongue.shippingservice;

import com.tongue.shippingservice.services.GeometricCalculation;
import org.junit.Test;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Point;

import static org.junit.Assert.*;

public class GeometricCalculationUnitTest {

    @Test
    public void shouldReturnFalseWhenPointOutsideCircle(){
        Point point = new Point(0,0);
        Point center = new Point(10,10);
        Circle circle = new Circle(center,1);
        GeometricCalculation geometricCalculation = new GeometricCalculation();
        assertFalse(geometricCalculation.pointInsideCircle(point,circle));
    }

    @Test
    public void shouldReturnTrueWhenPointInsideCircle(){
        Point point = new Point(0,0);
        Point center = new Point(0,0);
        Circle circle = new Circle(center,1);
        GeometricCalculation geometricCalculation = new GeometricCalculation();
        assertTrue(geometricCalculation.pointInsideCircle(point,circle));
    }

}