package com.tongue.shippingservice.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@Slf4j
@EnableWebMvc
public class WebConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        log.info("Cors enabled for all originsx");
        registry.addMapping("/**")
        .allowedOrigins("http://localhost:8081").allowedMethods("GET","POST","OPTIONS").allowCredentials(true);
    }
}
