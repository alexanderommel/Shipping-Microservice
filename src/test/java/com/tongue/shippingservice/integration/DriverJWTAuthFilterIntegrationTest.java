package com.tongue.shippingservice.integration;

import com.tongue.shippingservice.domain.replication.Driver;
import com.tongue.shippingservice.repositories.DriverReplicationRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class DriverJWTAuthFilterIntegrationTest {

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private DriverReplicationRepository driverReplicationRepository;

    private MockMvc mvc;
    private String secretKey;
    private String audience="shipping-service";

    @Autowired
    public void catchSecretKey(@Value("${driver.management.service.key}") String secretKey){
        this.secretKey=secretKey;
    }

    @Before
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    public void givenBasicAuthToken_whenDoFilter_thenReturn403StatusCode() throws Exception {
        this.mvc
                .perform(get("/shipping/test")
                        .header("Authorization","Basic "))
                .andExpect(status().is(403));
    }
    @Test
    public void givenIncompleteJWTToken_when_doFilter_thenReturn403StatusCode() throws Exception {
        this.mvc
                .perform(get("/shipping/test")
                        .header("Authorization","Bearer "))
                .andExpect(status().is(403));
    }

    @Test
    public void givenValidJWTTokenButNoRegisteredUser_whenDoFilter_thenReturn404StatusCode() throws Exception {
        String jwtToken = createValidJWTToken("twenty",audience);

        this.mvc
                .perform(get("/shipping/test")
                        .header("Authorization",jwtToken))
                .andExpect(status().is(404));
    }

    @Test
    public void givenValidJWTToken_whenDoFilter_thenReturnStatusOk() throws Exception {
        Driver driver = Driver.builder().username("rabbit").build();
        driverReplicationRepository.save(driver);
        String jwt = createValidJWTToken("rabbit", audience);

        this.mvc
                .perform(get("/shipping/test")
                .header("Authorization",jwt))
                .andExpect(status().isOk());
    }

    @Test
    public void givenValidJWTToken_whenDoFilter_thenReturnTrueResponseMessage() throws Exception {
        String expected = "true";
        Driver driver = Driver.builder().username("bunny").build();
        driverReplicationRepository.save(driver);
        String jwt = createValidJWTToken("bunny", audience);

        MvcResult result =
                this.mvc
                .perform(get("/shipping/test")
                .header("Authorization",jwt))
                .andReturn();

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected,actual);
    }

    @Test
    public void givenValidJWTTokenButShoppingServiceAudience_whenDoFilter_thenReturnStatusCode403() throws Exception {
        String jwt = createValidJWTToken("dummy", "shopping-service");
        this.mvc
                .perform(get("/shipping/test")
                        .header("Authorization",jwt))
                .andExpect(status().is(403));
    }

    @Test
    public void givenRareSignedJWTToken_whenDoFilter_thenReturnStatusCode403() throws Exception {
        String jwt = createRareSignedJWTToken("dummy", "myKey");
        this.mvc
                .perform(get("/shipping/test")
                        .header("Authorization",jwt))
                .andExpect(status().is(403));
    }

    private String createRareSignedJWTToken(String username,String key){
        List<GrantedAuthority> grantedAuthorities = AuthorityUtils
                .commaSeparatedStringToAuthorityList("DRIVER");

        String token = Jwts
                .builder()
                .setId("pass")
                .setSubject(username)
                .setIssuer("driver-management-service")
                .setAudience(audience)
                .claim("authorities",
                        grantedAuthorities.stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.toList()))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 Day
                .signWith(SignatureAlgorithm.HS512, key.getBytes()).compact();

        return "Bearer " + token;
    }

    private String createValidJWTToken(String username, String audience) {

        List<GrantedAuthority> grantedAuthorities = AuthorityUtils
                .commaSeparatedStringToAuthorityList("DRIVER");

        String token = Jwts
                .builder()
                .setId("pass")
                .setSubject(username)
                .setIssuer("driver-management-service")
                .setAudience(audience)
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
