package com.tongue.shippingservice.core.contracts;

import lombok.Data;

@Data
public class ApiResponse<T> {

    private Boolean ok;
    private Success<T> success = null;
    private Error error = null;

    private ApiResponse(){}

    private ApiResponse(T payload){
        this.ok = true;
        Success success1 = Success.builder().payload(payload).build();
        this.success = success1;
        this.error = null;
    }

    private ApiResponse(String errorMessage){
        this.ok = false;
        this.error = Error.builder().message(errorMessage).build();
        this.success = null;
    }

    public static <type> ApiResponse<type> success(type payload){
        return new ApiResponse(payload);
    }

    public static ApiResponse error(String message){
        return new ApiResponse(message);
    }

}
