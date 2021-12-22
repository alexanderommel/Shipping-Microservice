package com.tongue.shippingservice.domain;

import com.sun.istack.NotNull;
import com.tongue.shippingservice.domain.replication.Driver;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"artifactId"})})
public class Shipping {

    private @Id @GeneratedValue Long id;
    private Artifact artifact;
    private @NotNull Billing billing;
    private Shipping.Status status;
    @ManyToOne
    private Driver driver;

    @NotNull
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "longitude", column = @Column(name = "origin_longitude")),
            @AttributeOverride(name = "latitude", column = @Column(name = "origin_latitude")),
            @AttributeOverride(name = "address", column = @Column(name = "origin_address")),
            @AttributeOverride(name = "owner", column = @Column(name = "origin_owner"))
    })
    private Position origin;

    @NotNull
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "longitude", column = @Column(name = "destination_longitude")),
            @AttributeOverride(name = "latitude", column = @Column(name = "destination_latitude")),
            @AttributeOverride(name = "address", column = @Column(name = "destination_address")),
            @AttributeOverride(name = "owner", column = @Column(name = "destination_owner"))
    })
    private Position destination;

    public enum Status{
        PENDING,ACCEPTED,CONFIRMED,FINISHED,CANCELLED_BY_CUSTOMER,CANCELLED_BY_COURIER,CANCELLED_BY_SYSTEM
    }

    @Embeddable
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Billing{

	    BigDecimal total;
        BigDecimal artifact;
	    BigDecimal fee;
        BigDecimal debt;
        Boolean hasDebts;
        PaymentMethod paymentMethod;

    }

    public enum PaymentMethod{
        CASH,CREDIT
    }

}
