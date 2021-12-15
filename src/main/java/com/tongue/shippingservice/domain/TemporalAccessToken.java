package com.tongue.shippingservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TemporalAccessToken implements AccessToken{

    private String key;
    private int expirationHour;
    private int expirationMinute;
    private int expirationSecond;
    private String base64Encoding;

    @Override
    public Boolean validate() {
        LocalTime localTime = LocalTime.now();
        LocalTime expiration = LocalTime.of(expirationHour,expirationMinute,expirationSecond);
        int expired = localTime.compareTo(expiration);
        return !(expired>=0);
    }
}
