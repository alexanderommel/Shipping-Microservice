package com.tongue.shippingservice.services;

import com.tongue.shippingservice.domain.Courier;
import com.tongue.shippingservice.domain.Position;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class CourierStacking {

    public Stack<Courier> basicStackingByEuclideanDistance(List<Courier> couriers, Point origin, int max_couriers){
        log.info("Basic Euclidean Distance Stacking");
        Stack<Courier> courierStack = new Stack<>();
        Map<String, Double> distances = new HashMap<>();
        int index = 0;
        for (Courier courier:
             couriers) {
            Point courierPos = courier.getPosition().geometricPoint();
            Double x_distance = Math.abs(origin.getX()-courierPos.getX());
            Double y_distance = Math.abs(origin.getY()-courierPos.getY());
            x_distance = Math.pow(x_distance,2);
            y_distance = Math.pow(y_distance,2);
            Double euclidean_distance = Math.sqrt(x_distance+y_distance);
            distances.put(String.valueOf(index), euclidean_distance);
            index++;
        }
        /** Order and pick the top n Couriers based on distance **/
        Stream<Map.Entry<String, Double>> sortedMap =
                distances.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue()));
        Map<String, Double> orderedMap =
        sortedMap.limit(max_couriers).collect(Collectors.toMap(
                Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new)
        );
        /** Add to stack**/
        for (Map.Entry<String,Double> entry:
                orderedMap.entrySet()) {
            int i = Integer.parseInt(entry.getKey());
            courierStack.push(couriers.get(i));
        }
        return courierStack;
    }
}
