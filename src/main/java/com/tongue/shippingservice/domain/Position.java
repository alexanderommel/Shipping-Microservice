package com.tongue.shippingservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.geo.Point;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@Builder
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class Position implements GeometricPoint, Serializable {
    private String latitude;
    private String longitude;
    private String address;
    private String owner;

    @Override
    public Point geometricPoint() {
        return new Point(Double.valueOf(latitude),Double.valueOf(longitude));
    }

    public boolean validLatLongCoordinates(){
        return true;
    }
}
