package com.tongue.shippingservice.services;

import com.tongue.shippingservice.domain.Artifact;
import com.tongue.shippingservice.domain.Courier;
import com.tongue.shippingservice.domain.Shipping;
import com.tongue.shippingservice.repositories.ShippingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DispatchStatusActuator {

    private CourierSessionHandler sessionHandler;
    private ShippingRepository shippingRepository;

    public DispatchStatusActuator(@Autowired CourierSessionHandler sessionHandler,
                                  @Autowired ShippingRepository shippingRepository){
        this.sessionHandler=sessionHandler;
        this.shippingRepository=shippingRepository;
    }


    public Shipping onInternalErrorShippingDispatch(Shipping shipping){
        shipping.setStatus(Shipping.Status.CANCELLED_BY_SYSTEM);
        shipping.setDriver(null);
        shippingRepository.save(shipping);
        return shipping;
    }

}
