package com.tongue.shippingservice.resources;

import com.tongue.shippingservice.domain.*;
import com.tongue.shippingservice.domain.replication.Customer;
import com.tongue.shippingservice.domain.replication.Driver;
import com.tongue.shippingservice.messaging.ShippingEventPublisher;
import com.tongue.shippingservice.repositories.DriverReplicationRepository;
import com.tongue.shippingservice.repositories.ShippingRepository;
import com.tongue.shippingservice.services.CourierSessionHandler;
import com.tongue.shippingservice.services.CourierWsSessionHandler;
import com.tongue.shippingservice.services.CustomerWsSessionHandler;
import com.tongue.shippingservice.services.ShippingTokenDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.Optional;

@Controller
@Slf4j
public class ShippingWebSocketController {

    private ShippingRepository shippingRepository;
    private DriverReplicationRepository driverReplicationRepository;
    private CourierSessionHandler sessionHandler;
    private CustomerWsSessionHandler customerWsSessionHandler;
    private CourierWsSessionHandler courierWsSessionHandler;
    private String shippingGeolocationDest;
    private ShippingEventPublisher publisher;
    private ShippingTokenDecoder tokenDecoder;

    public ShippingWebSocketController(@Autowired ShippingRepository shippingRepository,
                                       @Autowired CourierWsSessionHandler courierWsSessionHandler,
                                       @Autowired DriverReplicationRepository driverReplicationRepository,
                                       @Autowired CourierSessionHandler sessionHandler,
                                       @Autowired CustomerWsSessionHandler customerWsSessionHandler,
                                       @Autowired ShippingTokenDecoder tokenDecoder,
                                       @Autowired ShippingEventPublisher publisher,
                                       @Value("${shipping.stomp.customers.geolocation}") String shippingGeolocationDest){

        this.publisher=publisher;
        this.courierWsSessionHandler=courierWsSessionHandler;
        this.shippingRepository=shippingRepository;
        this.driverReplicationRepository=driverReplicationRepository;
        this.sessionHandler=sessionHandler;
        this.customerWsSessionHandler=customerWsSessionHandler;
        this.shippingGeolocationDest=shippingGeolocationDest;
        this.tokenDecoder=tokenDecoder;

    }

    @MessageMapping("/shipping/accept")
    //@SendToUser("/queue/shipping/response")
    public void acceptRequest(Long shippingId, Principal principal, String accessToken, HttpSession session){

        Courier.status status = (Courier.status) session.getAttribute("STATUS");
        Courier courier = Courier.builder().username(principal.getName()).build();

        if (status != Courier.status.READY) {
            log.warn("Only Couriers with status READY can accept requests");
            courierWsSessionHandler.
                    sendObjectToSubscribedCourier(false,courier,"/queue/shipping/response");
            return;
        }
        TemporalAccessToken temporalAccessToken;
        log.info("Decoding token '"+accessToken+"'");
        try {
            temporalAccessToken =
                    tokenDecoder.decodeBase64TemporalAccessToken(accessToken);
        }catch (Exception e){
            log.warn(e.getMessage());
            courierWsSessionHandler.
                    sendObjectToSubscribedCourier(false,courier,"/queue/shipping/response");
            return;
        }

        log.info("Validating");
        Boolean valid = temporalAccessToken.validate();
        if (!valid){
            log.info("Invalid token");
            courierWsSessionHandler.
                    sendObjectToSubscribedCourier(false,courier,"/queue/shipping/response");
            return;
        }

        if (shippingId==null){
            log.info("ShippingId is needed");
            courierWsSessionHandler.
                    sendObjectToSubscribedCourier(false,courier,"/queue/shipping/response");
            return;
        }
        Optional<Shipping> shippingOptional = shippingRepository.findById(shippingId);
        if (shippingOptional.isEmpty()){
            log.info("No such Shipping object for shippingId: "+shippingId);
            courierWsSessionHandler.
                    sendObjectToSubscribedCourier(false,courier,"/queue/shipping/response");
            return;
        }
        Shipping shipping = shippingOptional.get();
        shipping.setStatus(Shipping.Status.ACCEPTED);
        Optional<Driver> optionalDriver = driverReplicationRepository.findByUsername(principal.getName());
        if (optionalDriver.isEmpty()){
            log.error("Authenticated user has no database register");
            courierWsSessionHandler.
                    sendObjectToSubscribedCourier(false,courier,"/queue/shipping/response");
            return;
        }
        Driver driver = optionalDriver.get();
        shipping.setDriver(driver);
        shippingRepository.save(shipping);
        log.info("Successful acceptation");
        courierWsSessionHandler.
                sendObjectToSubscribedCourier(true,courier,"/queue/shipping/response");
    }

    @MessageMapping("/shipping/continue")
    @SendToUser("/queue/shipping/destination")
    public ShippingDestination continueShipping(HttpSession httpSession){
        Artifact artifact = (Artifact) httpSession.getAttribute("ARTIFACT");
        if (artifact==null){
            log.warn("No artifact attached to this session");
            return null;
        }
        Optional<Shipping> optional = shippingRepository.findById(Long.valueOf(artifact.getArtifactId()));
        Shipping shipping = optional.get();
        if (shipping.getStatus()!= Shipping.Status.ACCEPTED){
            log.warn("ShippingStatus must be ACCEPTED to be able to continue");
            return null;
        }

        ShippingDestination shippingDestination = ShippingDestination.builder()
                .destination(shipping.getDestination())
                .artifactId(shipping.getArtifact().getArtifactId())
                .billing(shipping.getBilling())
                .build();

        publisher.publishShippingContinuation(shipping);

        return shippingDestination;
    }

    @MessageMapping("/shipping/complete")
    public Boolean finishShipping(HttpSession httpSession, Principal principal){
        Artifact artifact = (Artifact) httpSession.getAttribute("ARTIFACT");
        if (artifact==null){
            log.warn("No artifact attached to this session");
            return false;
        }
        Optional<Shipping> optional = shippingRepository.findById(Long.valueOf(artifact.getArtifactId()));
        Shipping shipping = optional.get();
        if (shipping.getStatus()!= Shipping.Status.CONFIRMED){
            log.warn("ShippingStatus must be CONFIRMED to be able to finish");
            return false;
        }
        Courier courier = Courier.builder().username(principal.getName()).build();
        publisher.publishShippingCompletion(shipping,courier);
        return true;
    }

    @MessageMapping("/shipping/position/share")
    public void sharePosition(Position position, Principal principal){

        /** Scene: Courier shares his position
         *  Expected: Courier position stored on RedisSession attribute 'POSITION'
         *  and sent to the artifact's owner**/

        if (position==null){
            log.warn("Position shouldn't be a null object");
            return;
        }
        if (position.getLongitude()==null || position.getLatitude()==null){
            log.warn("Position attributes must be populated");
            return;
        }
        /** Save Position**/
        Boolean saved =
                sessionHandler.savePosition(position, Courier.builder()
                        .username(principal.getName())
                        .build());
        if (!saved){
            log.warn("Position couldn't be persisted on User Session");
            return;
        }
        /** Send Position to Customer **/
        Artifact artifact =
                sessionHandler.getArtifact(Courier.builder().username(principal.getName()).build());
        if (artifact!=null){
            customerWsSessionHandler.sendShippingPositionToCustomer(position,
                    Customer.builder().username(artifact.getOwner()).build(),shippingGeolocationDest);
        }
    }
}
