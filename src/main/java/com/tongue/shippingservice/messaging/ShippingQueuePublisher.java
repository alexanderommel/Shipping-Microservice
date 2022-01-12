package com.tongue.shippingservice.messaging;

import com.tongue.shippingservice.messaging.domain.ShipmentAcceptation;
import com.tongue.shippingservice.messaging.domain.ShipmentCompletion;
import com.tongue.shippingservice.messaging.domain.ShipmentContinuation;
import com.tongue.shippingservice.messaging.domain.ShippingRequestRejection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ShippingQueuePublisher {

    private RabbitTemplate rabbitTemplate;
    private String queueAccepted;
    private String queueContinue;
    private String queueFinish;
    private String queueReject;

    public ShippingQueuePublisher(@Autowired RabbitTemplate rabbitTemplate,
                                  @Value("${queues.out.shipping.accept}") String queueAccepted,
                                  @Value("${queues.out.shipping.continue}") String queueContinue,
                                  @Value("${queues.out.shipping.finish}") String queueFinish,
                                  @Value("${queues.out.shipping.request.reject}") String queueReject){

        this.rabbitTemplate=rabbitTemplate;
        this.queueAccepted=queueAccepted;
        this.queueContinue=queueContinue;
        this.queueFinish=queueFinish;
        this.queueReject=queueReject;

    }

    @Async
    public void sendShippingAcceptedMessageToRabbitMQ(ShipmentAcceptation acceptation){
        log.info("Publishing ShipmentAcceptation to Queue: "+queueAccepted);
        log.info("ShipmentAcceptation: "+acceptation);
        rabbitTemplate.convertAndSend(queueAccepted,acceptation);
    }

    @Async
    public void sendShippingContinuationMessageToRabbitMQ(ShipmentContinuation continuation){
        log.info("Publishing ShipmentContinuation to Queue: "+queueContinue);
        log.info("ShipmentContinuation: "+continuation);
        rabbitTemplate.convertAndSend(queueContinue,continuation);
    }

    @Async
    public void sendShippingCompletionToRabbitMQ(ShipmentCompletion completion){
        log.info("Publishing ShipmentCompletion to Queue: "+queueFinish);
        log.info("ShipmentCompletion: "+completion);
        rabbitTemplate.convertAndSend(queueFinish,completion);
    }

    @Async
    public void sendShippingRequestRejectionToRabbitMQ(ShippingRequestRejection rejection){
        log.info("Publishing ShippingRequestRejection to Queue: "+queueReject);
        log.info("ShippingRejection: "+rejection);
        rabbitTemplate.convertAndSend(queueReject,rejection);
    }
}
