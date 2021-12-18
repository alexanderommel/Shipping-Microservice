package com.tongue.shippingservice.repositories;

import com.tongue.shippingservice.domain.Shipping;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShippingRepository extends JpaRepository<Shipping,Long> {
}
