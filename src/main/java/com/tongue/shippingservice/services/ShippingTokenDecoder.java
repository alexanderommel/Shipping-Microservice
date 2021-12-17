package com.tongue.shippingservice.services;

import com.tongue.shippingservice.domain.ShippingFee;
import com.tongue.shippingservice.domain.TemporalAccessToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
@Slf4j
public class ShippingTokenDecoder {

    private Base64.Decoder decoder = Base64.getDecoder();

    public TemporalAccessToken decodeBase64TemporalAccessToken(String base64Encoding){
        byte[] bytes = base64Encoding.getBytes();
        byte[] decoded = decoder.decode(bytes);
        String object = new String(decoded);
        String[] maps = object.split(",");
        String[] keyMap = maps[0].split(":");
        String key = keyMap[1];
        String[] hourMap = maps[1].split(":");
        String expirationHour = hourMap[1];
        String[] minuteMap = maps[2].split(":");
        String expirationMinute = minuteMap[1];
        String[] secondMap = maps[3].split(":");
        String expirationSecond = secondMap[1];
        TemporalAccessToken temporalAccessToken =
                TemporalAccessToken.builder()
                        .key(key)
                        .expirationHour(Integer.parseInt(expirationHour))
                        .expirationMinute(Integer.parseInt(expirationMinute))
                        .expirationSecond(Integer.parseInt(expirationSecond))
                .build();
        return temporalAccessToken;
    }
}
