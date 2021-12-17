package com.tongue.shippingservice.services;

import com.tongue.shippingservice.domain.Courier;
import com.tongue.shippingservice.domain.Position;
import com.tongue.shippingservice.domain.SearchParameters;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

@Service
@Slf4j
public class CourierTracking {

    private CourierFiltering courierFiltering;
    private CourierStacking courierStacking;
    private CourierSessionHandler sessionHandler;

    public CourierTracking(@Autowired CourierFiltering courierFiltering,
                           @Autowired CourierStacking courierStacking,
                           @Autowired CourierSessionHandler sessionHandler){
        this.courierFiltering=courierFiltering;
        this.courierStacking=courierStacking;
        this.sessionHandler=sessionHandler;
    }

    public Stack<Courier> searchNearestDrivers(Position origin, SearchParameters parameters){
        log.info("Searching drivers near to {"+origin.getLatitude()+","+origin.getLongitude()+"}");
        log.info("Searching parameters: "+parameters.toString());
        List<Courier> candidates = new ArrayList<>();
        log.info("Retrieving Couriers from Redis...");
        List<Courier> couriers = sessionHandler.getAllCouriersWithStatus(Courier.status.READY);
        Stack<Courier> nearestDrivers;
        int ROUND = 0;
        while (ROUND< parameters.getMax_rounds()){
            log.info("Round "+ROUND);
            Float INCREMENT = (ROUND * parameters.getIncrease_ratio());
            ROUND++;
            Float RADIUS = parameters.getInitial_radius() + INCREMENT;
            log.info("Radius: "+RADIUS);
            log.info("Filtering...");
            /** Filtering candidates **/
            if (parameters.getSearchFilter()== SearchParameters.SearchFilter.BASIC_CIRCULAR){
                candidates = courierFiltering.basicCircularFiltering(couriers,origin,RADIUS);
            }
            if (!candidates.isEmpty()){
                log.info("Search has been successful");
                if (parameters.getStackingMethod()== SearchParameters.StackingMethod.BASIC_EUCLIDEAN){
                    System.out.println("Stacking...");
                    nearestDrivers = courierStacking.basicStackingByEuclideanDistance(
                            candidates,origin.geometricPoint(), parameters.getMax_stack_size());
                    return nearestDrivers;
                }
            }
        }
        log.info("Search has failed");
        return null;
    }

}
