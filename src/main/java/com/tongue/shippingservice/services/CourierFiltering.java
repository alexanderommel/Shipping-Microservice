package com.tongue.shippingservice.services;

import com.tongue.shippingservice.domain.Courier;
import com.tongue.shippingservice.domain.Position;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CourierFiltering {

    private GeometricCalculation geometricCalculation;
    public CourierFiltering(@Autowired GeometricCalculation geometricCalculation){
        this.geometricCalculation=geometricCalculation;
    }

    public List<Courier> basicCircularFiltering(List<Courier> couriers, Position origin, Float radius){
        log.info("Basic Circular Filtering");
        List<Courier> filteredCouriers = new ArrayList<>();
        Circle circle = new Circle(origin.geometricPoint(), radius);
        for (Courier courier:
             couriers) {
            log.info("Test Courier: "+courier.getUsername());
            Boolean passTest = geometricCalculation.pointInsideCircle(courier.getPosition().geometricPoint(),
                    circle);
            if (!passTest) {
                log.info("Rejected");
                continue;
            }
            filteredCouriers.add(courier);
            log.info("Pass");
        }
        return filteredCouriers;
    }
}
