package com.tongue.shippingservice.resources;

import com.tongue.shippingservice.domain.*;
import com.tongue.shippingservice.repositories.ShippingRepository;
import com.tongue.shippingservice.services.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.geo.Distance;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@Slf4j
public class ShippingRestController {

    private ShippingConsultant consultant;
    private ShippingTokenSupplier tokenSupplier;
    private ShippingTokenDecoder tokenDecoder;

    public ShippingRestController(@Autowired ShippingConsultant consultant,
                                  @Autowired ShippingTokenSupplier tokenSupplier,
                                  @Autowired ShippingTokenDecoder tokenDecoder){

        this.consultant=consultant;
        this.tokenSupplier=tokenSupplier;
        this.tokenDecoder=tokenDecoder;
    }

    @GetMapping(value = "/shipping/tokens/validate",params = {"sessionId"})
    public ResponseEntity<String> validateShippingFeeToken(@RequestParam(name = "sessionId") String sessionId){
        log.info("Decoding token '"+sessionId+"'");
        TemporalAccessToken temporalAccessToken =
                tokenDecoder.decodeBase64TemporalAccessToken(sessionId);
        log.info("Validating");
        if (temporalAccessToken==null){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (!temporalAccessToken.validate()){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        log.info("Successful Session Validation!");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(value = "/shipping/summary")
    public ResponseEntity<ShippingSummary> getSummary(@RequestBody PositionWrapper wrapper){
        Position origin = wrapper.getOrigin();
        Position destination = wrapper.getDestination();
        if (origin==null || destination==null){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (origin.getLatitude()==null || origin.getLongitude()==null
                || destination.getLatitude()==null || destination.getLongitude()==null){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        log.info("Summary retrieving for {origin:"+origin+",destination:"+destination+"}");
        log.info("Consultant Computing Fee, Distance and Time");
        BigDecimal fee = consultant.shippingFee(origin,destination);
        Distance distance = consultant.tripDistance(origin,destination);
        LocalTime arrivalTime = consultant.arrivalTime(origin,destination);
        LocalTime expiration = LocalTime.now();
        expiration = expiration.plusMinutes(3);
        log.info("Creating AccessToken with expiration on {"+expiration+"}");
        TemporalAccessToken temporalAccessToken = tokenSupplier.createBase64TemporalAccessToken(fee.toPlainString(),expiration);
        ShippingFee shippingFee =
                ShippingFee.builder().fee(fee).temporalAccessToken(temporalAccessToken).build();
        ShippingSummary summary =
                ShippingSummary.builder().shippingFee(shippingFee).distance(distance).arrivalTime(arrivalTime).build();
        log.info("Successful ShippingSummary computed");
        return new ResponseEntity<>(summary,HttpStatus.OK);
    }

    /** Unsecured **/

    @Profile("test")
    @GetMapping("/shipping/test")
    public Boolean test(){
        return true;
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PositionWrapper{
        Position origin;
        Position destination;
    }

}
