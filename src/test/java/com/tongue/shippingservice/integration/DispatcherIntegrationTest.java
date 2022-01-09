package com.tongue.shippingservice.integration;

import com.tongue.shippingservice.domain.*;
import com.tongue.shippingservice.domain.replication.Driver;
import com.tongue.shippingservice.messaging.ShippingEventPublisher;
import com.tongue.shippingservice.repositories.DriverReplicationRepository;
import com.tongue.shippingservice.repositories.ShippingRepository;
import com.tongue.shippingservice.services.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.task.TaskExecutor;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/**
 *
 * TO RUN THIS TEST YOU MUST BE RUNNING A REDIS SERVER
 *
 * **/



@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@EnableAsync
public class DispatcherIntegrationTest {

    @Autowired
    private DriverReplicationRepository driverReplicationRepository;
    private WebSocketStompClient webSocketStompClient;
    @LocalServerPort
    private Integer port;
    private String driverSecretKey;
    private String audience="shipping-service";
    private String candidatesWsDestination;
    @MockBean
    ShippingEventPublisher shippingEventPublisher;
    @MockBean
    CustomerWsSessionHandler customerWsSessionHandler;
    private Dispatcher dispatcher;
    @Autowired
    ShippingTokenSupplier tokenSupplier;
    @Autowired
    DispatchTracking tracker;
    @Autowired
    CourierWsSessionHandler courierWsSessionHandler;
    @Autowired
    ShippingRepository shippingRepository;


    @Autowired
    public void catchSecretKey(@Value("${driver.management.service.key}") String driverSecretKey,
                               @Value("${shipping.stomp.couriers.deliver}") String candidatesWsDestination){
        this.driverSecretKey=driverSecretKey;
        this.candidatesWsDestination=candidatesWsDestination;
    }

    @Before
    public void setup() {
        this.webSocketStompClient = new WebSocketStompClient(
                new SockJsClient(List.of(new WebSocketTransport(new StandardWebSocketClient()))));
        this.webSocketStompClient.setMessageConverter(new MappingJackson2MessageConverter());
        dispatcher = new Dispatcher(tokenSupplier,
                tracker,
                shippingEventPublisher,
                courierWsSessionHandler,
                customerWsSessionHandler,
                candidatesWsDestination);

    }

    @Test
    public void shouldSendArtifactId12345Successfully_toCourierWebSocketsSession_whenDispatching()
            throws ExecutionException, InterruptedException, TimeoutException {

        String expected = "12345";

        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<>(1);

        /** Setting Connection **/

        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        String jwt = createValidJWTToken("bunny");
        Driver driver = Driver.builder().username("bunny").build();
        driverReplicationRepository.save(driver);
        headers.add("Authorization", jwt);

        StompSession stompSession = webSocketStompClient.connect(
                String.format("ws://localhost:%d/connect",port),
                headers,
                new StompSessionHandlerAdapter() {
                }).get(1, TimeUnit.SECONDS);

        /** Stomp Subscription **/

        stompSession.subscribe("/user"+candidatesWsDestination, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                System.out.println("Getting PayloadType");
                return ShippingNotification.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                System.out.println("Frame Handling");
                System.out.println("Payload: "+payload);
                ShippingNotification notification = (ShippingNotification) payload;
                String current = notification.getArtifactId();
                blockingQueue.add(current);
            }
        });

        /** Method call and assert **/

        dispatcher.dispatchShipping(generateStackOfCouriers()
                ,generateShippingGivenArtifactId(expected)
                ,generateDispatchParameters());

        assertEquals(expected,blockingQueue.poll(1,TimeUnit.SECONDS));

    }

    @Test
    public void shouldSendArtifactId12345Successfully_givenThatFirstStackedCourierIgnoresTheRequest()
            throws ExecutionException, InterruptedException, TimeoutException {

        String expected = "12345";

        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<>(1);

        /** Setting Connection **/

        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        String jwt = createValidJWTToken("bunny");
        Driver driver = Driver.builder().username("bunny").build();
        driverReplicationRepository.save(driver);
        headers.add("Authorization", jwt);

        StompSession stompSession = webSocketStompClient.connect(
                String.format("ws://localhost:%d/connect",port),
                headers,
                new StompSessionHandlerAdapter() {
                }).get(1, TimeUnit.SECONDS);

        /** Stomp Subscription **/

        stompSession.subscribe("/user"+candidatesWsDestination, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                System.out.println("Getting PayloadType");
                return ShippingNotification.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                System.out.println("Frame Handling");
                System.out.println("Payload: "+payload);
                ShippingNotification notification = (ShippingNotification) payload;
                String current = notification.getArtifactId();
                blockingQueue.add(current);
            }
        });

        /** Method call and assert **/

        dispatcher.dispatchShipping(generateStackOfThreeCouriers()
                ,generateShippingGivenArtifactId(expected)
                ,generateDispatchParameters());

        assertEquals(expected,blockingQueue.poll(1,TimeUnit.SECONDS));

    }

    private Stack<Courier> generateStackOfCouriers(){
        Courier courier = Courier.builder().username("bunny").status(Courier.status.READY).build();
        Stack<Courier> couriers = new Stack<>();
        couriers.push(courier);
        return couriers;
    }

    private Stack<Courier> generateStackOfThreeCouriers(){
        Courier courier = Courier.builder().username("bobby").status(Courier.status.READY).build();
        Courier courier1 = Courier.builder().username("bunny").status(Courier.status.READY).build();
        Courier courier2 = Courier.builder().username("funny").status(Courier.status.READY).build();
        Stack<Courier> couriers = new Stack<>();
        couriers.push(courier);
        couriers.push(courier1);
        couriers.push(courier2);
        return couriers;
    }

    private DispatchParameters generateDispatchParameters(){
        DispatchParameters parameters = DispatchParameters.builder()
                .intervalSeconds(3)
                .k(1)
                .build();
        return parameters;
    }

    private Shipping generateShippingGivenArtifactId(String artifactId){

        Position pos = Position.builder()
                .latitude("1.2")
                .longitude("1.3")
                .build();

        Artifact artifact = Artifact.builder()
                .artifactId(artifactId)
                .owner("Alexis")
                .build();

        Shipping shipping = Shipping.builder()
                .status(Shipping.Status.PENDING)
                .origin(pos)
                .destination(pos)
                .artifact(artifact)
                .build();

        shipping = shippingRepository.save(shipping);
        return shipping;

    }

    private String createValidJWTToken(String username) {

        List<GrantedAuthority> grantedAuthorities = AuthorityUtils
                .commaSeparatedStringToAuthorityList("DRIVER");

        String token = Jwts
                .builder()
                .setId("pass")
                .setSubject(username)
                .setIssuer("driver-management-service")
                .setAudience("shipping-service")
                .claim("authorities",
                        grantedAuthorities.stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.toList()))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 Day
                .signWith(SignatureAlgorithm.HS512, driverSecretKey.getBytes()).compact();

        return "Bearer " + token;
    }


}
