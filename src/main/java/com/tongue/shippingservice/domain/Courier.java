package com.tongue.shippingservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Courier {

    private Position position;
    private Courier.status status;
    private String username;

    public enum status{
        READY,BUSY,OFFLINE
    }

}
