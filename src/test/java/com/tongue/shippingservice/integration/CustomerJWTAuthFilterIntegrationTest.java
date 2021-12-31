package com.tongue.shippingservice.integration;

import com.tongue.shippingservice.domain.replication.Driver;
import com.tongue.shippingservice.repositories.DriverReplicationRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class CustomerJWTAuthFilterIntegrationTest {

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private DriverReplicationRepository driverReplicationRepository;
    private MockMvc mvc;
    private String secretKey1;
    private String audience="shipping-service";

    @Autowired
    public void catchSecretKey(@Value("${driver.management.service.key}") String secretKey){
        this.secretKey1=secretKey;
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
    public void givenRareSignedJWTToken_whenDoFilter_thenReturnStatusCode403() throws Exception {
        String jwt = createRareSignedJWTToken("dummy", "myKey");
        this.mvc
                .perform(get("/shipping/test")
                        .header("Authorization",jwt))
                .andExpect(status().is(403));
    }

    @Test
    public void givenValidJWTToken_whenDoFilter_thenReturnStatusOk() throws Exception {

        String jwt = createValidJWTToken("rabbit", audience);
        this.mvc
                .perform(get("/shipping/test")
                        .header("Authorization",jwt))
                .andExpect(status().isOk());
    }

    @Test
    public void givenValidJWTToken_whenDoFilter_thenReturnTrueResponseMessage() throws Exception {
        String expected = "true";
        String jwt = createValidJWTToken("bunny", audience);

        MvcResult result =
                this.mvc
                        .perform(get("/shipping/test")
                                .header("Authorization",jwt))
                        .andReturn();

        String actual = result.getResponse().getContentAsString();
        assertEquals(expected,actual);
    }









    private String createRareSignedJWTToken(String username,String key){
        List<GrantedAuthority> grantedAuthorities = AuthorityUtils
                .commaSeparatedStringToAuthorityList("CUSTOMER");

        String token = Jwts
                .builder()
                .setId("pass")
                .setSubject(username)
                .setIssuer("customer-management-service")
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
                .commaSeparatedStringToAuthorityList("CUSTOMER");

        String token = Jwts
                .builder()
                .setId("pass")
                .setSubject(username)
                .setIssuer("customer-management-service")
                .setAudience(audience)
                .claim("authorities",
                        grantedAuthorities.stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.toList()))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 Day
                .signWith(SignatureAlgorithm.HS512, secretKey1.getBytes()).compact();

        return "Bearer " + token;
    }

}
