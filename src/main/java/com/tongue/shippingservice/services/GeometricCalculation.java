package com.tongue.shippingservice.services;

import com.tongue.shippingservice.domain.GeometricPoint;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;

@Service
public class GeometricCalculation {

    public Boolean pointInsideCircle(Point point, Circle circle){
        Double radius = circle.getRadius().getNormalizedValue();
        Point center = circle.getCenter();
        Double min_x = center.getX()-radius;
        Double max_x = center.getX()+radius;
        Double min_y = center.getY()-radius;
        Double max_y = center.getY()+radius;
        Double x = point.getX();
        Double y = point.getY();
        return ((x<=max_x && x>=min_x) && (y<=max_y && y>=min_y));
    }
}
