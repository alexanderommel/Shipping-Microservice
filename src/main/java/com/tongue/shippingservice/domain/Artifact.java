package com.tongue.shippingservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Artifact {

    private String artifactId;
    private String resource;
    private String resourceKey;
    private String owner;

}
