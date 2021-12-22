package com.tongue.shippingservice.repositories;

import com.tongue.shippingservice.domain.replication.Driver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DriverReplicationRepository extends JpaRepository<Driver, Long> {

    Optional<Driver> findByUsername(String username);
}
