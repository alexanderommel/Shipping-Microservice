package com.tongue.shippingservice.security;

import com.tongue.shippingservice.domain.replication.Driver;
import com.tongue.shippingservice.repositories.DriverReplicationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class DriverAuthenticationManager implements AuthenticationManager {

    private DriverReplicationRepository driverReplicationRepository;

    public DriverAuthenticationManager(@Autowired DriverReplicationRepository driverReplicationRepository){
        this.driverReplicationRepository=driverReplicationRepository;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        log.info("Authenticating user: "+authentication.getName());
        String username = authentication.getName();
        Optional<Driver> optional = driverReplicationRepository.findByUsername(username);
        if (optional.isEmpty())
            throw new UsernameNotFoundException("No such driver with username "+username);
        Authentication authentication1 =
                new UsernamePasswordAuthenticationToken(username,null, authentication.getAuthorities());
        return authentication1;
    }
}
