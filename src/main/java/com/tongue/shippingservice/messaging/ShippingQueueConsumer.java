package com.tongue.shippingservice.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tongue.shippingservice.messaging.domain.ShippingRequest;
import com.tongue.shippingservice.messaging.domain.ShippingRequestCancellation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ShippingQueueConsumer {

    private String shippingRequestQueueName;
    private String requestCancellationQueueName;
    private ShippingProcessor shippingProcessor;
    private ObjectMapper objectMapper;

    public ShippingQueueConsumer(@Value("${queues.in.shipping.request}") String shippingRequestQueueName,
                                 @Value("${queues.in.shipping.request.cancel}") String requestCancellationQueueName,
                                 @Autowired ObjectMapper objectMapper,
                                 @Autowired ShippingProcessor shippingProcessor){
        this.shippingRequestQueueName=shippingRequestQueueName;
        this.objectMapper=objectMapper;
        this.shippingProcessor=shippingProcessor;
        this.requestCancellationQueueName=requestCancellationQueueName;
    }

    @RabbitListener(queues = {"${queues.in.shipping.request}"})
    public void receiveRequest(ShippingRequest content) {
        log.info("Consuming ShippingRequest from Queue: "+shippingRequestQueueName);
        log.info("Processing ShippingRequest: "+content);
        shippingProcessor.processRequest(content);
    }

    @RabbitListener(queues = {"${queues.in.shipping.request.cancel}"})
    public void receiveRequestCancellation(ShippingRequestCancellation cancellation) {
        log.info("Consuming ShippingRequest Cancellation from Queue: "+requestCancellationQueueName);
        log.info("Processing Request Cancellation: "+cancellation);
        /** Processor call**/
    }

    /*@RabbitListener(queues = {"${queues.in.shipping.request}"})
    public void receiveRequest(@Payload String content) throws JsonProcessingException {
        log.info("Consuming ShippingRequest from Queue: "+shippingRequestQueueName);
        ShippingRequest request =
                objectMapper.readValue(content, ShippingRequest.class);
        log.info("Processing ShippingRequest: "+request);
        shippingProcessor.processRequest(request);
    }*/


}
