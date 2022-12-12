package com.tongue.shippingservice.core;

import com.tongue.shippingservice.security.CustomerJwtAuthenticationFilter;
import com.tongue.shippingservice.security.DriverAuthenticationManager;
import com.tongue.shippingservice.security.DriverJwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    String customerJwtValidationSecretKey;
    String driverJwtValidationSecretKey;
    DriverAuthenticationManager driverAuthenticationManager;

    public SecurityConfig(
            @Value("${customer.management.service.key}") String customerJwtValidationSecretKey,
            @Value("${driver.management.service.key}") String driverJwtValidationSecretKey,
            @Autowired DriverAuthenticationManager driverAuthenticationManager){

        this.customerJwtValidationSecretKey=customerJwtValidationSecretKey;
        this.driverJwtValidationSecretKey=driverJwtValidationSecretKey;
        this.driverAuthenticationManager=driverAuthenticationManager;

    }

    public CustomerJwtAuthenticationFilter customerJwt(){
        CustomerJwtAuthenticationFilter customerFilter =
                new CustomerJwtAuthenticationFilter(customerJwtValidationSecretKey);
        return customerFilter;
    }

    public DriverJwtAuthenticationFilter driverJwtFilter(){
        return new DriverJwtAuthenticationFilter(
                driverJwtValidationSecretKey,
                driverAuthenticationManager);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
                .cors()
                .and()
                .authorizeRequests()
                .antMatchers("/drivers/oauth").permitAll()
                .antMatchers("/drivers/register").permitAll()
                .antMatchers("/shipping/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(driverJwtFilter(),UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(customerJwt(),UsernamePasswordAuthenticationFilter.class)
                //.addFilterBefore(driverJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                //.addFilterAfter(customerJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic().disable()
                .formLogin().disable()
                .csrf().disable()
                //.rememberMe()
        //.and()
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED).maximumSessions(1);

        /**http
                .authorizeRequests()
                .anyRequest().permitAll()
                .and()
                //.addFilterBefore(driverJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                //.addFilterAfter(customerJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                //.httpBasic().disable()
                .formLogin().disable()
                .csrf().disable();**/

        /*http
                .authorizeRequests()
                .antMatchers("/drivers/**").permitAll()
        .and()
        .formLogin().disable()
        .httpBasic().disable();*/

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
