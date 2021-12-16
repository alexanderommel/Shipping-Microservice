package com.tongue.shippingservice.services;

import com.tongue.shippingservice.domain.Courier;
import com.tongue.shippingservice.domain.DispatcherMessage;
import com.tongue.shippingservice.domain.Shipping;
import com.tongue.shippingservice.domain.replication.Driver;
import com.tongue.shippingservice.repositories.ShippingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class DispatchTracking {

    private ShippingRepository shippingRepository;

    public DispatchTracking(@Autowired ShippingRepository shippingRepository){
        this.shippingRepository=shippingRepository;
    }

    public DispatcherMessage shippingStatus(Shipping shipping, Courier courier){

        DispatcherMessage dispatcherMessage = new DispatcherMessage();
        if (shipping==null){
            dispatcherMessage.setStatus(DispatcherMessage.DispatchStatus.INTERNAL_ERROR);
            dispatcherMessage.setError("Null Shipping Object");
            log.error("Null reference");
            return dispatcherMessage;
        }
        if (shipping.getId()==null){
            dispatcherMessage.setStatus(DispatcherMessage.DispatchStatus.INTERNAL_ERROR);
            dispatcherMessage.setError("Null Shipping Object Id");
            log.error("Null reference");
            return dispatcherMessage;
        }
        Optional<Shipping> optional = shippingRepository.findById(shipping.getId());
        Shipping shipping1 = optional.get();
        if (shipping1==null){
            dispatcherMessage.setStatus(DispatcherMessage.DispatchStatus.INTERNAL_ERROR);
            dispatcherMessage.setError("This shipping object hasn't been created");
            log.error("Dispatching a no persisted Shipping");
            return dispatcherMessage;
        }
        if (shipping1.getStatus()!=Shipping.Status.ACCEPTED){
            log.info("Shipping request rejected");
            dispatcherMessage.setStatus(DispatcherMessage.DispatchStatus.NOT_FOUND);
            return dispatcherMessage;
        }
        Driver driver = shipping1.getDriver();
        if (driver==null){
            log.error("Shipping accepted with an empty driver reference");
            dispatcherMessage.setStatus(DispatcherMessage.DispatchStatus.INTERNAL_ERROR);
            return dispatcherMessage;
        }
        if (!(driver.getUsername().equalsIgnoreCase(courier.getUsername()))){
            dispatcherMessage.setStatus(DispatcherMessage.DispatchStatus.INTERNAL_ERROR);
            dispatcherMessage.setError("A driver has accepted the request out of time");
            log.error("Shipping accepted by a no authorized Courier");
            return dispatcherMessage;
        }
        dispatcherMessage.setStatus(DispatcherMessage.DispatchStatus.DISPATCHED);
        log.info("Successful Dispatch");
        return dispatcherMessage;
    }

}
