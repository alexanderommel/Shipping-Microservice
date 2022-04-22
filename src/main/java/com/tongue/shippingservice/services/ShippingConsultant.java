package com.tongue.shippingservice.services;

import com.tongue.shippingservice.domain.Position;
import com.tongue.shippingservice.domain.Shipping;
import org.springframework.data.geo.CustomMetric;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metric;
import org.springframework.data.geo.Metrics;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalTime;

@Service
public class ShippingConsultant {

    /** Simple responses in order to advance faster with the development **/

    public BigDecimal shippingFee(Position origin, Position destination){
        return BigDecimal.valueOf(2.25);
    }

    public Distance tripDistance(Position origin, Position destination){
        Distance distance = new Distance(10.5, Metrics.KILOMETERS);
        return distance;
    }

    public LocalTime arrivalTime(Position origin, Position destination){
        LocalTime time = LocalTime.of(0,30);
        return time;
    }

}
