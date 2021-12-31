package com.tongue.shippingservice.security;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.NullRememberMeServices;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CustomerJwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final String cmSecretKey;
    private final String header;
    private final String prefix;
    private RememberMeServices rememberMeServices = new NullRememberMeServices();

    public CustomerJwtAuthenticationFilter(@Value("${customer.management.service.key}") String secretKey,
                                           @Autowired DriverAuthenticationManager authenticationManager){
        
        this.cmSecretKey=secretKey;
        this.header="Authorization";
        this.prefix="Bearer ";
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication1 = SecurityContextHolder.getContext().getAuthentication();
        if (authentication1 != null) {
            filterChain.doFilter(request,response);
            return;
        }
        log.info("Customer Management Service Authorization");
        if (!SecurityUtils.containsJwtToken(request,header,prefix)){
            log.info("Filter called but no JWTToken detected");
            handleError(request,response,filterChain);
            return;
        }
        try {
            log.info("Validating JWT");
            log.info("Customers Secret Key: "+cmSecretKey);
            Claims claims = SecurityUtils.validateJWT(request,cmSecretKey,header,prefix);

            if (claims==null)
                throw new MalformedJwtException("Invalid JWT");

            log.info("Claims not null");

            if (!SecurityUtils.containsRequiredValues(claims)){
                log.info("Missing Claims");
                handleError(request,response,filterChain);
                return;
            }
            if (!SecurityUtils.validIssuerAndAudience(claims,"customer-management-service")){
                log.info("Bad identity");
                handleError(request,response,filterChain);
                return;
            }

            Authentication authentication = SecurityUtils.wrapAuthenticationTokenFromClaims(claims);
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            log.info("RememberMeServices login success");
            this.rememberMeServices.loginSuccess(request,response,authentication);

        }catch (MalformedJwtException e){
            handleError(request,response,filterChain);
            return;
        }
        catch (ExpiredJwtException | UnsupportedJwtException e){
            log.warn("Exception throw: "+e.getMessage());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.sendError(HttpServletResponse.SC_FORBIDDEN,e.getMessage());
            return;
        }catch (IllegalArgumentException e){
            log.info("Empty JWT");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,e.getMessage());
            return;
        }
        logger.info("DoFilter next");
        filterChain.doFilter(request,response);
    }

    private void handleError(HttpServletRequest request,HttpServletResponse response,FilterChain filterChain) throws ServletException, IOException {
        SecurityContextHolder.clearContext();
        this.rememberMeServices.loginFail(request,response);
        filterChain.doFilter(request,response);
    }
}
