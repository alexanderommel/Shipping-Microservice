package com.tongue.shippingservice.core;

import com.rabbitmq.client.AMQP;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitAMQPConfig {

    private String shippingRequestQueueName;
    private String shippingAcceptedQueueName;
    private String shippingRequestCancelledQueueName;
    private String shippingCancelledQueueName;
    private String shippingFinishedQueueName;
    private String shippingContinuationQueueName;
    private String shippingRejectedQueueName;
    private String host;
    private Integer port;
    private String user;
    private String password;
    private String virtualhost;

    public RabbitAMQPConfig(@Value("${queues.in.shipping.request}") String shippingRequestQueueName,
                            @Value("${queues.out.shipping.accept}") String shippingAcceptedQueueName,
                            @Value("${queues.in.shipping.request.cancel}") String shippingRequestCancelledQueueName,
                            @Value("${queues.out.shipping.cancel}") String shippingCancelledQueueName,
                            @Value("${queues.out.shipping.finish}") String shippingFinishedQueueName,
                            @Value("${queues.out.shipping.continue}") String shippingContinuationQueueName,
                            @Value("${queues.out.shipping.request.reject}") String shippingRejectedQueueName,
                            @Value("${spring.rabbitmq.host}") String host,
                            @Value("${spring.rabbitmq.port}") Integer port,
                            @Value("${spring.rabbitmq.username}") String user,
                            @Value("${spring.rabbitmq.password}") String password/**,
                            @Value("${spring.rabbitmq.virtual-host}") String virtualhost**/){

        this.shippingRequestQueueName=shippingRequestQueueName;
        this.shippingAcceptedQueueName=shippingAcceptedQueueName;
        this.shippingRequestCancelledQueueName=shippingRequestCancelledQueueName;
        this.shippingCancelledQueueName=shippingCancelledQueueName;
        this.shippingFinishedQueueName=shippingFinishedQueueName;
        this.shippingContinuationQueueName=shippingContinuationQueueName;
        this.shippingRejectedQueueName=shippingRejectedQueueName;
        this.host=host;
        this.port=port;
        this.user=user;
        this.password=password;
        //this.virtualhost=virtualhost;

    }

    @Bean
    public ConnectionFactory connectionFactory() throws Exception {
        final CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setAddresses(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(user);
        connectionFactory.setPassword(password);
        //connectionFactory.setVirtualHost(virtualhost);
        return connectionFactory;
    }

    @Bean
    public MessageConverter jsonConverter(){
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate() throws Exception {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
        rabbitTemplate.setMessageConverter(jsonConverter());
        return rabbitTemplate;
    }

    /** Set all parameters to false to make testing easier **/

    @Bean
    public Queue shippingRequestQueue(){
        return new Queue(shippingRequestQueueName,false,false,false);
    }

    @Bean
    public Queue shippingAcceptedQueue(){
        return new Queue(shippingAcceptedQueueName,false,false,false);
    }

    @Bean
    public Queue shippingContinuationQueue(){ return new Queue(shippingContinuationQueueName,false,false,false); }

    @Bean
    public Queue shippingFinishedQueue(){
        return new Queue(shippingFinishedQueueName,false,false,false);
    }

    @Bean
    public Queue shippingRejectedQueue(){ return new Queue(shippingRejectedQueueName,false,false,false); }

    @Bean
    public Queue shippingCancelledQueue(){
        return new Queue(shippingCancelledQueueName,false,false,false);
    }

    @Bean
    public Queue shippingRequestCancelledQueue(){
        return new Queue(shippingRequestCancelledQueueName,false,false,false);
    }

}
