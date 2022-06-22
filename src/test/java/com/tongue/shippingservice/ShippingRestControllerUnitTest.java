package com.tongue.shippingservice;

import com.tongue.shippingservice.domain.Position;
import com.tongue.shippingservice.domain.ShippingSummary;
import com.tongue.shippingservice.domain.TemporalAccessToken;
import com.tongue.shippingservice.resources.ShippingRestController;
import com.tongue.shippingservice.services.ShippingConsultant;
import com.tongue.shippingservice.services.ShippingTokenDecoder;
import com.tongue.shippingservice.services.ShippingTokenSupplier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.geo.CustomMetric;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metric;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class ShippingRestControllerUnitTest {

    private ShippingRestController restController;

    @Mock
    private ShippingConsultant consultant;
    @Mock
    private ShippingTokenSupplier tokenSupplier;
    @Mock
    private ShippingTokenDecoder tokenDecoder;

    private TemporalAccessToken temporalAccessToken;


    @Before
    public void setUp(){

        LocalTime expiration = LocalTime.now();
        expiration = expiration.plusMinutes(11);
        temporalAccessToken = TemporalAccessToken.builder()
                .key("fee")
                .expirationSecond(expiration.getSecond())
                .expirationMinute(expiration.getMinute())
                .expirationHour(expiration.getHour())
                .build();
        Metric metric = new CustomMetric(1,"km");
        Distance distance = new Distance(10.5, metric);

        Mockito.when(tokenDecoder.
                decodeBase64TemporalAccessToken(ArgumentMatchers.anyString()))
                .thenReturn(temporalAccessToken);
        Mockito.when(tokenSupplier
                .createBase64TemporalAccessToken(ArgumentMatchers.any(),ArgumentMatchers.any()))
                .thenReturn(temporalAccessToken);
        Mockito.when(consultant
                .shippingFee(ArgumentMatchers.any(),ArgumentMatchers.any()))
                .thenReturn(BigDecimal.TEN);
        Mockito.when(consultant
                .arrivalTime(ArgumentMatchers.any(),ArgumentMatchers.any()))
                .thenReturn(expiration);
        Mockito.when(consultant
                .tripDistance(ArgumentMatchers.any(),ArgumentMatchers.any()))
                .thenReturn(distance);

        restController = new ShippingRestController(consultant,tokenSupplier,tokenDecoder);

    }

    @Test
    public void givenPositions_whenGettingSummary_thenHttpStatusIsOK(){
        HttpStatus expected = HttpStatus.OK;
        Position pos1 = Position.builder().latitude("0").longitude("0").build();
        Position pos2 = Position.builder().latitude("1.222").longitude("1.1222").build();
        ShippingRestController.PositionWrapper wrapper = ShippingRestController.PositionWrapper.builder()
                .origin(pos1)
                .destination(pos2)
                .build();
        ResponseEntity<ShippingSummary> response =
                restController.getSummary(wrapper);
        System.out.println(response.toString());
        assertEquals(expected,response.getStatusCode());
    }

    @Test
    public void givenEmptyPosition_whenGettingSummary_thenHttpStatusIsBadRequest(){
        HttpStatus expected = HttpStatus.BAD_REQUEST;
        Position pos1 = Position.builder().latitude("0").longitude("0").build();
        ShippingRestController.PositionWrapper wrapper = ShippingRestController.PositionWrapper.builder()
                .origin(pos1)
                .destination(null)
                .build();
        ResponseEntity<ShippingSummary> response =
                restController.getSummary(wrapper);
        System.out.println(response.toString());
        assertEquals(expected,response.getStatusCode());
    }

    @Test
    public void givenValidToken_whenValidating_thenHttpStatusIsOK(){
        HttpStatus expected = HttpStatus.OK;
        ResponseEntity<String> response =
                restController.validateShippingFeeToken("whatever");
        assertEquals(expected,response.getStatusCode());
    }


}
