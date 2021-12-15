package com.tongue.shippingservice.services;

import com.tongue.shippingservice.domain.TemporalAccessToken;
import io.netty.handler.codec.base64.Base64Encoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Base64;

@Service
@Slf4j
public class ShippingTokenSupplier {

    private Base64.Encoder base64Encoder = Base64.getEncoder();

    public TemporalAccessToken createBase64TemporalAccessToken(String key, LocalTime expiration){
        log.info("Simple Concatenation Base64 Encoding");

        String simpleConcatenation =
                "key:"+key
                        +",expirationHour:"+expiration.getHour()
                        +",expirationMinute:"+expiration.getMinute()
                        +",expirationSecond:"+expiration.getSecond();

        byte[] simpleBytes = simpleConcatenation.getBytes();
        byte[] base64Encode = base64Encoder.encode(simpleBytes);
        String base64String = base64Encode.toString();

        TemporalAccessToken temporalAccessToken =
                TemporalAccessToken.builder().
                        base64Encoding(base64String).
                        expirationHour(expiration.getHour()).
                        expirationMinute(expiration.getMinute()).
                        expirationSecond(expiration.getSecond()).
                        build();

        return temporalAccessToken;
    }
}
