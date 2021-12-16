package com.tongue.shippingservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DispatcherMessage {

    private DispatchStatus status;
    private String error;

    public enum DispatchStatus{
        DISPATCHED,NOT_FOUND,BAD_REQUEST,INTERNAL_ERROR
    }
}
