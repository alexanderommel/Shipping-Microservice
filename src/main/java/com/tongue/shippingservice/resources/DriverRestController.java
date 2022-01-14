package com.tongue.shippingservice.resources;

import com.tongue.shippingservice.domain.TemporalAccessToken;
import com.tongue.shippingservice.domain.replication.Driver;
import com.tongue.shippingservice.repositories.DriverReplicationRepository;
import com.tongue.shippingservice.services.ShippingTokenSupplier;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class DriverRestController {

    DriverReplicationRepository repository;
    ShippingTokenSupplier supplier;
    String secretKey;

    public DriverRestController(@Autowired DriverReplicationRepository repository,
                                @Autowired ShippingTokenSupplier supplier,
                                @Value("${driver.management.service.key}") String secretKey){
        this.repository=repository;
        this.supplier=supplier;
        this.secretKey=secretKey;
    }

    @PostMapping(value = "/drivers/login", consumes = "application/json", produces = "application/json")
    public String login(@RequestBody  String username){
        log.info("Creating JWT Token");
        log.info("Username: "+username);
        Optional<Driver> optional = repository.findByUsername(username);
        if (optional.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        log.info("User exists");
        Driver driver = optional.get();
        String jwt = createValidJWTToken(driver.getUsername());
        return jwt;
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
