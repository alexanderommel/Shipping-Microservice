package com.tongue.shippingservice;

import com.tongue.shippingservice.domain.Courier;
import com.tongue.shippingservice.domain.Position;
import com.tongue.shippingservice.domain.SearchParameters;
import com.tongue.shippingservice.services.CourierFiltering;
import com.tongue.shippingservice.services.CourierSessionHandler;
import com.tongue.shippingservice.services.CourierStacking;
import com.tongue.shippingservice.services.CourierTracking;
import org.junit.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.junit.Test;

import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CourierTrackingSociableTest {

    private CourierTracking courierTracking;

    @Autowired
    private CourierFiltering courierFiltering;
    @Mock
    private CourierStacking courierStacking;
    @Mock
    private CourierSessionHandler sessionHandler;

    @Before
    public void setUp(){
        /** Active couriers **/
        List<Courier> couriers = generateCouriers();
        Stack<Courier> courierStack = new Stack<>();
        courierStack.addAll(couriers);
        Mockito.when(sessionHandler.getAllCouriersWithStatus(Courier.status.READY)).thenReturn(couriers);
        Mockito.when(courierStacking.basicStackingByEuclideanDistance(
                ArgumentMatchers.any()
                ,ArgumentMatchers.any()
                ,ArgumentMatchers.anyInt()
        )).thenReturn(courierStack);
        courierTracking = new CourierTracking(courierFiltering,courierStacking,sessionHandler);
    }

    @Test
    public void givenGenerousSearchingParameters_whenSearching_thenReturnNotNull(){
        Position origin = Position.builder().latitude("10.4").longitude("10.2").build();
        SearchParameters params = SearchParameters.builder()
                .searchFilter(SearchParameters.SearchFilter.BASIC_CIRCULAR)
                .stackingMethod(SearchParameters.StackingMethod.BASIC_EUCLIDEAN)
                .increase_ratio(20F)
                .max_rounds(5)
                .max_stack_size(3)
                .initial_radius(200F)
                .build();
        Stack<Courier> couriers = courierTracking.searchNearestDrivers(origin, params);
        assertNotNull(couriers);
    }

    @Test
    public void givenBadSearchingParameters_whenSearching_thenReturnNull(){
        Position origin = Position.builder().latitude("500.0").longitude("500.0").build();
        SearchParameters params = SearchParameters.builder()
                .searchFilter(SearchParameters.SearchFilter.BASIC_CIRCULAR)
                .stackingMethod(SearchParameters.StackingMethod.BASIC_EUCLIDEAN)
                .increase_ratio(0.00001F)
                .max_rounds(1)
                .max_stack_size(1)
                .initial_radius(0.00001F)
                .build();
        Stack<Courier> couriers = courierTracking.searchNearestDrivers(origin, params);
        assertNull(couriers);
    }

    @Test
    public void givenNiceSearchingParameters_whenSearching_thenReturnNotNull(){
        Position origin = Position.builder().latitude("500.0").longitude("500.0").build();
        SearchParameters params = SearchParameters.builder()
                .searchFilter(SearchParameters.SearchFilter.BASIC_CIRCULAR)
                .stackingMethod(SearchParameters.StackingMethod.BASIC_EUCLIDEAN)
                .increase_ratio(100F)
                .max_rounds(10)
                .max_stack_size(4)
                .initial_radius(100F)
                .build();
        Stack<Courier> couriers = courierTracking.searchNearestDrivers(origin, params);
        assertNotNull(couriers);
    }

    @Test
    public void givenIncompleteSearchParameters_whenSearching_thenReturnNull(){
        Position origin = Position.builder().latitude("10.4").longitude("10.2").build();
        SearchParameters params = new SearchParameters();
        Stack<Courier> couriers = courierTracking.searchNearestDrivers(origin, params);
        assertNull(couriers);
    }

    private List<Courier> generateCouriers(){
        List<Courier> couriers = Arrays.asList(
                // Courier 1
                Courier.builder()
                        .username("courier1")
                        .position(Position.builder()
                                .latitude("5.5")
                                .longitude("5.5")
                                .build())
                        .build(),
                // Courier 2
                Courier.builder()
                        .username("courier2")
                        .position(Position.builder()
                                .latitude("100.5")
                                .longitude("100.0")
                                .build())
                        .build(),
                // Courier 3
                Courier.builder()
                        .username("courier3")
                        .position(Position.builder()
                                .latitude("10.0")
                                .longitude("10.0")
                                .build())
                        .build(),
                // Courier 4
                Courier.builder()
                        .username("courier4")
                        .position(Position.builder()
                                .latitude("50.0")
                                .longitude("60.0").build())
                        .build()
        );
        return couriers;
    }
}
