package com.tongue.shippingservice.resources;

import com.tongue.shippingservice.domain.*;
import com.tongue.shippingservice.repositories.ShippingRepository;
import com.tongue.shippingservice.services.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/shipping/tokens/validate")
    public ResponseEntity<Map<String,Object>> validateShippingFeeToken(String base64TemporalToken){
        log.info("Decoding token '"+base64TemporalToken+"'");
        TemporalAccessToken temporalAccessToken =
                tokenDecoder.decodeBase64TemporalAccessToken(base64TemporalToken);
        log.info("Validating");
        System.out.println(temporalAccessToken);
        Boolean valid = temporalAccessToken.validate();
        if (!valid){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "/shipping/summary")
    public ResponseEntity<Map<String,Object>> getSummary(Position origin, Position destination){
        Map<String,Object> response = new HashMap<>();
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
        expiration.plusMinutes(1);
        log.info("Creating AccessToken with expiration on {"+expiration+"}");
        TemporalAccessToken temporalAccessToken = tokenSupplier.createBase64TemporalAccessToken(fee.toPlainString(),expiration);
        ShippingFee shippingFee =
                ShippingFee.builder().fee(fee).temporalAccessToken(temporalAccessToken).build();
        ShippingSummary summary =
                ShippingSummary.builder().shippingFee(shippingFee).distance(distance).arrivalTime(arrivalTime).build();
        log.info("Successful ShippingSummary computed");
        response.put("response",summary);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    /** Unsecured **/

    @GetMapping("/shipping/test")
    public String test(){
        return "Test";
    }

}
