package com.tongue.shippingservice.messaging;

import com.tongue.shippingservice.domain.*;
import com.tongue.shippingservice.messaging.domain.ShippingRequest;
import com.tongue.shippingservice.messaging.domain.ShippingRequestRejection;
import com.tongue.shippingservice.repositories.ShippingRepository;
import com.tongue.shippingservice.services.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Stack;

@Service
@Slf4j
public class ShippingProcessor {

    private ShippingTokenDecoder tokenDecoder;
    private ShippingRepository shippingRepository;
    private ShippingEventPublisher eventPublisher;
    private CourierWsSessionHandler wsSessionHandler;
    private CustomerWsSessionHandler customerWsSessionHandler;
    private CourierTracking courierTracking;
    private Dispatcher dispatcher;

    public ShippingProcessor(@Autowired ShippingTokenDecoder tokenDecoder,
                             @Autowired ShippingRepository shippingRepository,
                             @Autowired ShippingEventPublisher eventPublisher,
                             @Autowired CourierWsSessionHandler wsSessionHandler,
                             @Autowired CustomerWsSessionHandler customerWsSessionHandler,
                             @Autowired CourierTracking courierTracking,
                             @Autowired Dispatcher dispatcher){

        this.tokenDecoder=tokenDecoder;
        this.shippingRepository=shippingRepository;
        this.eventPublisher=eventPublisher;
        this.wsSessionHandler=wsSessionHandler;
        this.customerWsSessionHandler=customerWsSessionHandler;
        this.courierTracking=courierTracking;
        this.dispatcher=dispatcher;

    }

    @Async
    public void processRequest(ShippingRequest request){

        ShippingRequestRejection rejection;

        /** Validating ShippingFeeTokens **/

        log.info("ShippingRequest -> "+request);

        String shippingFeeToken = request.getShippingFeeToken();

        if (!request.getTesting()){

            TemporalAccessToken temporalAccessToken =
                    tokenDecoder.decodeBase64TemporalAccessToken(shippingFeeToken);
            if (temporalAccessToken==null){

                rejection = ShippingRequestRejection.builder()
                        .artifactId(request.getArtifact().getArtifactId())
                        .reason(ShippingRequestRejection.Reason.BAD_TOKEN)
                        .details("Invalid format for shipping token")
                        .build();

                eventPublisher.publishShippingRequestRejection(rejection);
                return;
            }

            Boolean valid = temporalAccessToken.validate();
            if (!valid){

                rejection = ShippingRequestRejection.builder()
                        .artifactId(request.getArtifact().getArtifactId())
                        .reason(ShippingRequestRejection.Reason.TOKEN_EXPIRED)
                        .details("Token Time expired")
                        .build();

                eventPublisher.publishShippingRequestRejection(rejection);
                return;
            }
        }


        /** Search Parameters should be created dynamically**/

        SearchParameters params = SearchParameters.builder()
                .searchFilter(SearchParameters.SearchFilter.BASIC_CIRCULAR)
                .stackingMethod(SearchParameters.StackingMethod.BASIC_EUCLIDEAN)
                .increase_ratio(10F)
                .max_rounds(2)
                .max_stack_size(5)
                .initial_radius(3F)
                .build();

        Stack<Courier> stack =
                courierTracking.searchNearestDrivers(request.getOrigin(),params);

        if (stack==null){
            rejection = ShippingRequestRejection.builder()
                    .artifactId(request.getArtifact().getArtifactId())
                    .reason(ShippingRequestRejection.Reason.NO_DRIVERS_FOUND)
                    .details("Drivers not found for this area")
                    .build();
            eventPublisher.publishShippingRequestRejection(rejection);
            return;
        }

        /** Dispatching **/

        Shipping shipping = wrapShippingRequest(request);
        shipping = shippingRepository.save(shipping);

        DispatchParameters dispatchParameters =
                DispatchParameters.builder().k(1).intervalSeconds(30).build();

        DispatcherMessage message =
                dispatcher.dispatchShipping(stack,shipping,dispatchParameters);

        if (message.getStatus()!=DispatcherMessage.DispatchStatus.DISPATCHED){
            rejection = ShippingRequestRejection.builder()
                    .artifactId(request.getArtifact().getArtifactId())
                    .shippingId(shipping.getId())
                    .reason(ShippingRequestRejection.Reason.NOT_ACCEPTED)
                    .details("Assigned couriers rejected to deliver this artifact")
                    .build();
            eventPublisher.publishShippingRequestRejection(rejection);
        }

    }

    private Shipping wrapShippingRequest(ShippingRequest r){
        Shipping shipping = Shipping.builder()
                .artifact(r.getArtifact())
                .billing(r.getBilling())
                .origin(r.getOrigin())
                .destination(r.getDestination())
                .status(Shipping.Status.PENDING)
                .build();
        return shipping;
    }
}
