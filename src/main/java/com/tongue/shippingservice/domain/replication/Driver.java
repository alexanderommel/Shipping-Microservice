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
public class Driver {

    private @Id @GeneratedValue Long id;
    private String username;
    private AccountStatus accountStatus;
    private Type type;
    private VehicleInfo vehicleInfo;

    public enum AccountStatus{
        ACTIVE,BANNED,RESTRICTED
    }

    public enum Type{
        RIDER,COURIER
    }

    @Embeddable
    @Data
    public class VehicleInfo{
        String vehicle_color;
        String vehicle_brand;
        String vehicle_imageUrl;
        String vehicle_licensePlate;
    }

}
