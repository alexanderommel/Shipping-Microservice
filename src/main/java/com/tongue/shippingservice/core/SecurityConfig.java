package com.tongue.shippingservice.core;

import com.tongue.shippingservice.security.CustomerJwtAuthenticationFilter;
import com.tongue.shippingservice.security.DriverJwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private DriverJwtAuthenticationFilter driverJwtAuthenticationFilter;

    public SecurityConfig(@Autowired DriverJwtAuthenticationFilter driverJwtAuthenticationFilter){
        this.driverJwtAuthenticationFilter=driverJwtAuthenticationFilter;

    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(driverJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .csrf().disable()
                .httpBasic().disable()
                .formLogin().disable();
    }

}
