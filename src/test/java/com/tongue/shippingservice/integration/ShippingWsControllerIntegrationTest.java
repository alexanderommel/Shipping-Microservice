package com.tongue.shippingservice.integration;

import com.tongue.shippingservice.domain.Courier;
import com.tongue.shippingservice.domain.Position;
import com.tongue.shippingservice.domain.Shipping;
import com.tongue.shippingservice.domain.TemporalAccessToken;
import com.tongue.shippingservice.domain.dto.ShippingAcceptDTO;
import com.tongue.shippingservice.domain.replication.Driver;
import com.tongue.shippingservice.repositories.DriverReplicationRepository;
import com.tongue.shippingservice.repositories.ShippingRepository;
import com.tongue.shippingservice.resources.ShippingWebSocketController;
import com.tongue.shippingservice.services.ShippingTokenSupplier;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.http.auth.BasicUserPrincipal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Type;
import java.security.Principal;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)

/**
 *
 * TO RUN THIS INTEGRATION TEST YOU MUST BE RUNNING A REDIS SERVER INSTANCE
 *
 */

public class ShippingWsControllerIntegrationTest {

    @Autowired
    private ShippingWebSocketController webSocketController;
    @Autowired
    private DriverReplicationRepository driverReplicationRepository;
    @Autowired
    private ShippingRepository shippingRepository;
    @Autowired
    private ShippingTokenSupplier tokenSupplier;
    @Mock
    private Principal principal;
    @Mock
    private HttpServletRequest httpSession;
    @LocalServerPort
    private Integer port;
    private WebSocketStompClient webSocketStompClient;
    private String secretKey;

    @Autowired
    public void catchSecretKey(@Value("${driver.management.service.key}") String secretKey){
        this.secretKey=secretKey;
    }

    @Before
    public void setUp(){
        this.webSocketStompClient = new WebSocketStompClient(
                new SockJsClient(List.of(new WebSocketTransport(new StandardWebSocketClient()))));
        this.webSocketStompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @Test
    public void givenThatCourierStatusIsNotREADY_whenAcceptingRequest_thenReturnFalse()
            throws ExecutionException, InterruptedException, TimeoutException {

        Mockito.when(httpSession.getAttribute("STATUS")).thenReturn(Courier.status.BUSY);

        /** Scene: Courier shares his position
         *  Expected: Courier position stored on HttpSession
         *  and sent to the artifact's owner**/

        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<>(1);

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



        stompSession.subscribe("/user/queue/shipping/response", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                System.out.println("Frame Handling");
                System.out.println("Payload: "+payload);
                blockingQueue.add(payload.toString());
            }
        });

        Position position = Position.builder().latitude("1.11").longitude("1.22").build();
        principal = new BasicUserPrincipal("bunny");
        ShippingAcceptDTO dto = ShippingAcceptDTO.builder()
                .shippingId(null)
                .accessToken("access")
                .build();
        webSocketController.acceptRequest(dto,principal);
        System.out.println("Test");
        assertEquals("false",blockingQueue.poll(4,TimeUnit.SECONDS));
    }

    @Test
    public void givenValidParameters_whenAcceptingRequest_thenReturnTrue() throws ExecutionException, InterruptedException, TimeoutException {


        /** STOMP Client Setting **/
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<>(1);

        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        String jwt = createValidJWTToken("scooby");
        Driver driver = Driver.builder().username("scooby").build();
        driverReplicationRepository.save(driver);
        headers.add("Authorization", jwt);

        StompSession stompSession = webSocketStompClient.connect(
                String.format("ws://localhost:%d/connect",port),
                headers,
                new StompSessionHandlerAdapter() {
                }).get(1, TimeUnit.SECONDS);



        stompSession.subscribe("/user/queue/shipping/response", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                System.out.println("Payload: "+payload);
                blockingQueue.add(payload.toString());
            }
        });

        /** End setting**/

        /** Persist a valid shipping request **/
        Shipping shipping = Shipping.builder().status(Shipping.Status.PENDING).build();
        shipping = shippingRepository.save(shipping);
        /** Create a valid access token **/
        LocalTime expiration = LocalTime.now();
        expiration = expiration.plusMinutes(1l);
        TemporalAccessToken accessToken =
                tokenSupplier.createBase64TemporalAccessToken(String.valueOf(shipping.getId()),expiration);
        /** Principal and HttpSession **/
        Principal principal = new BasicUserPrincipal("scooby");
        Mockito.when(httpSession.getAttribute("STATUS")).thenReturn(Courier.status.READY);
        /** Actual Testing **/
        ShippingAcceptDTO dto = ShippingAcceptDTO.builder()
                .shippingId(shipping.getId())
                .accessToken(accessToken.getBase64Encoding())
                .build();
        webSocketController.acceptRequest(dto,principal);
        assertEquals("true",blockingQueue.poll(4,TimeUnit.SECONDS));

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
                .signWith(SignatureAlgorithm.HS512, secretKey.getBytes()).compact();

        return "Bearer " + token;
    }

}
