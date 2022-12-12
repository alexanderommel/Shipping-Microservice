package com.tongue.shippingservice.resources;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonFactoryBuilder;
import com.tongue.shippingservice.core.contracts.ApiResponse;
import com.tongue.shippingservice.domain.Courier;
import com.tongue.shippingservice.domain.RegistrationForm;
import com.tongue.shippingservice.domain.TemporalAccessToken;
import com.tongue.shippingservice.domain.dto.DriverAuthentication;
import com.tongue.shippingservice.domain.replication.Driver;
import com.tongue.shippingservice.repositories.DriverReplicationRepository;
import com.tongue.shippingservice.services.CourierSessionHandler;
import com.tongue.shippingservice.services.ShippingTokenSupplier;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.json.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.http.*;
import org.springframework.http.converter.json.GsonBuilderUtils;
import org.springframework.http.converter.json.GsonFactoryBean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class DriverRestController {

    DriverReplicationRepository repository;
    ShippingTokenSupplier supplier;
    CourierSessionHandler courierSessionHandler;
    String secretKey;

    public DriverRestController(@Autowired DriverReplicationRepository repository,
                                @Autowired ShippingTokenSupplier supplier,
                                @Autowired CourierSessionHandler sessionHandler,
                                @Value("${driver.management.service.key}") String secretKey){
        this.repository=repository;
        this.supplier=supplier;
        this.courierSessionHandler=sessionHandler;
        this.secretKey=secretKey;
    }

    @GetMapping("/test")
    public String hola(){
        log.info("Test point called");
        return "hola";
    }

    @PostMapping("/drivers/register")
    public ResponseEntity<ApiResponse> register(@RequestBody RegistrationForm form){

        log.info("Registering new user");

        Driver.VehicleInfo vehicleInfo = Driver.VehicleInfo.builder()
                .vehicle_brand(form.getCar())
                .vehicle_licensePlate(form.getBrand())
                .build();

        Driver driver = Driver.builder()
                .username(form.getEmail())
                .firstname(form.getFirstName())
                .lastname(form.getLastName())
                .type(Driver.Type.COURIER)
                .imageUrl("url")
                .vehicleInfo(vehicleInfo)
                .password(form.getPassword())
                .build();

        driver = repository.save(driver);

        log.info("ok");
        return ResponseEntity.of(Optional.of(ApiResponse.success(driver)));

    }

    // Non secured endpoint for testing purposes
    @PostMapping( value = "/drivers/oauth", produces = "application/json", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> login(@RequestBody Driver driver1){
        String username = driver1.getUsername();
        log.info("Creating JWT Token");
        log.info("Username: "+username);
        Optional<Driver> optional = repository.findByUsername(username);
        if (optional.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        log.info("User exists");
        Driver driver = optional.get();
        String jwt = createValidJWTToken(driver.getUsername());

        DriverAuthentication authentication = DriverAuthentication.builder()
                .jwt(jwt)
                .driver(driver)
                .build();

        log.info("ok");
        return ResponseEntity.of(Optional.of(ApiResponse.success(authentication)));
    }

    // Friendly endpoint to ease the authentication process for Stomp connections
    @PostMapping(value = "/drivers/jwt",
            produces = "application/json", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> jwtAuthentication(@Autowired Principal principal){
        log.info("Jwt Authentication Successful");
        courierSessionHandler.updateCourierStatus(
                Courier.status.READY,
                Courier.builder()
                        .username(principal.getName())
                        .build()
        );
        if (principal==null)
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        log.info("ok");
        return ResponseEntity.of(Optional.of(ApiResponse.success("ok")));
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
