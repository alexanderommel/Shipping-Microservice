package com.tongue.shippingservice.core;

import com.tongue.shippingservice.domain.replication.Driver;
import com.tongue.shippingservice.repositories.DriverReplicationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class InitConfig {

    DriverReplicationRepository driverRepository;

    public InitConfig(@Autowired DriverReplicationRepository driverRepository){
        this.driverRepository=driverRepository;
    }

    @Bean
    public void createDefaultAccounts(){

        log.info("Default accounts created");

        Driver.VehicleInfo vehicleInfo = Driver.VehicleInfo.builder()
                .vehicle_brand("Mazda")
                .vehicle_licensePlate("AXS-111")
                .build();

        Driver driver1 = Driver.builder()
                .username("bunny")
                .firstname("Bunny")
                .lastname("Docs")
                .type(Driver.Type.COURIER)
                .imageUrl("url")
                .vehicleInfo(vehicleInfo)
                .build();

        Driver driver2 = Driver.builder()
                .username("funny")
                .firstname("Funny")
                .lastname("Docs")
                .type(Driver.Type.COURIER)
                .imageUrl("url")
                .vehicleInfo(vehicleInfo)
                .build();

        Driver driver3 = Driver.builder()
                .username("dummy")
                .firstname("Dummy")
                .lastname("Docs")
                .type(Driver.Type.COURIER)
                .imageUrl("url")
                .vehicleInfo(vehicleInfo)
                .build();

        driverRepository.save(driver1);
        driverRepository.save(driver2);
        driverRepository.save(driver3);
        
    }
}
