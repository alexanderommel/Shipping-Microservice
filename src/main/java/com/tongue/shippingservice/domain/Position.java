package com.tongue.shippingservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.geo.Point;

import javax.persistence.Embeddable;

@Data
@Builder
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class Position implements GeometricPoint{
    private String latitude;
    private String longitude;
    private String address;
    private String owner;

    @Override
    public Point geometricPoint() {
        return new Point(Double.valueOf(latitude),Double.valueOf(longitude));
    }
}
