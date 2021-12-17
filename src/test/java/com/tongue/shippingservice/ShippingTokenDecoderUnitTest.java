package com.tongue.shippingservice;


import com.tongue.shippingservice.domain.TemporalAccessToken;
import com.tongue.shippingservice.services.ShippingTokenDecoder;
import com.tongue.shippingservice.services.ShippingTokenSupplier;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ShippingTokenDecoderUnitTest {

    private ShippingTokenDecoder tokenDecoder;
    private ShippingTokenSupplier tokenSupplier;

    @Before
    public void setUp(){
        tokenDecoder = new ShippingTokenDecoder();
        tokenSupplier = new ShippingTokenSupplier();
    }

    @Test
    public void givenBase64EncodedToken_whenDecoding_thenDecodedTokenKeyIsEqualToExpected(){
        String expected = String.valueOf(BigDecimal.valueOf(2.50));
        LocalTime expiration = LocalTime.now();
        expiration = expiration.minusMinutes(3);
        TemporalAccessToken token = tokenSupplier.createBase64TemporalAccessToken(expected,expiration);
        String base64Encoded = token.getBase64Encoding();
        /** Evaluate **/
        TemporalAccessToken decoded = tokenDecoder.decodeBase64TemporalAccessToken(base64Encoded);
        String actual = decoded.getKey();
        assertEquals(expected,actual);
    }

    @Test
    public void givenBase64ExpiredEncodedToken_whenDecoding_thenDecodedTokenIsNotValid(){
        String key = String.valueOf(BigDecimal.valueOf(2.50));
        LocalTime expiration = LocalTime.now();
        expiration = expiration.minusMinutes(5);
        TemporalAccessToken token = tokenSupplier.createBase64TemporalAccessToken(key,expiration);
        String base64Encoded = token.getBase64Encoding();
        /** Evaluate **/
        TemporalAccessToken decoded = tokenDecoder.decodeBase64TemporalAccessToken(base64Encoded);
        assertFalse(decoded.validate());
    }
}
