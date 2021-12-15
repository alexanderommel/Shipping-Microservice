package com.tongue.shippingservice;

import com.tongue.shippingservice.domain.AccessToken;
import com.tongue.shippingservice.domain.TemporalAccessToken;
import com.tongue.shippingservice.services.ShippingTokenSupplier;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import java.time.Instant;
import java.time.LocalTime;

import static org.junit.Assert.*;

public class ShippingTokenSupplyUnitTest {

    @Test
    public void shouldCreateNotNullBase64TemporalAccessToken(){
        ShippingTokenSupplier supplier = new ShippingTokenSupplier();
        String driverId = "124";
        LocalTime now = LocalTime.of(1,1,10);
        Integer lifeTime = 20;
        LocalTime expiration = LocalTime.of(now.getHour(),now.getMinute(),now.getSecond()+lifeTime);
        TemporalAccessToken accessToken = supplier.createBase64TemporalAccessToken(driverId,expiration);
        assertNotNull(accessToken);
    }

    @Test
    public void givenExpiredBase64TemporalAccessToken_whenValidating_ThenReturnFalse(){
        ShippingTokenSupplier supplier = new ShippingTokenSupplier();
        String driverId = "124";
        LocalTime expiration = LocalTime.now();
        expiration = expiration.minusMinutes(3);
        AccessToken accessToken = supplier.createBase64TemporalAccessToken(driverId,expiration);
        assertFalse(accessToken.validate());
    }

    @Test
    public void givenBase64TemporalAccessToken_whenValidating_thenReturnTrue(){
        ShippingTokenSupplier supplier = new ShippingTokenSupplier();
        String driverId = "124";
        LocalTime expiration = LocalTime.now();
        expiration = expiration.plusMinutes(3);
        AccessToken accessToken = supplier.createBase64TemporalAccessToken(driverId,expiration);
        assertTrue(accessToken.validate());
    }


}
