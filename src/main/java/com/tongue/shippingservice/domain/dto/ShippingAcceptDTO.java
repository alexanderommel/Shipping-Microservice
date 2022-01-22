package com.tongue.shippingservice.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.servlet.http.HttpServletRequest;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShippingAcceptDTO {

    private Long shippingId;
    private String accessToken;

    @JsonIgnore
    private HttpServletRequest request;

}
