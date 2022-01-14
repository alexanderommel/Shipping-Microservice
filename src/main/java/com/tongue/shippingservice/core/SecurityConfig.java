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
    private CustomerJwtAuthenticationFilter customerJwtAuthenticationFilter;

   /* public SecurityConfig(@Autowired DriverJwtAuthenticationFilter driverJwtAuthenticationFilter,
                          @Autowired CustomerJwtAuthenticationFilter customerJwtAuthenticationFilter){

        this.customerJwtAuthenticationFilter=customerJwtAuthenticationFilter;
        this.driverJwtAuthenticationFilter=driverJwtAuthenticationFilter;

    }*/

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/drivers/**").permitAll()
        .and()
        .formLogin().disable()
        .httpBasic().disable();

        /*http
                .authorizeRequests()
                .antMatchers("/shipping/**")
                .authenticated()
                .and()
                .addFilterBefore(driverJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(customerJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .csrf().disable()
                .cors().disable()
                .httpBasic().disable()
                .formLogin().disable();*/

    }

}
