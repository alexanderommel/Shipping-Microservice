package com.tongue.shippingservice.domain.replication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder

/**
 *
 * Replicated Entity from Driver Management Service
 *
 */
public class Driver {

    private @Id @GeneratedValue Long id;
    private String username;
    private String firstname;
    private String lastname;
    private String imageUrl;
    private Type type;
    private VehicleInfo vehicleInfo;

    public enum Type{
        RIDER,COURIER
    }

    @Embeddable
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class VehicleInfo{
        String vehicle_color;
        String vehicle_brand;
        String vehicle_imageUrl;
        String vehicle_licensePlate;
    }

}
