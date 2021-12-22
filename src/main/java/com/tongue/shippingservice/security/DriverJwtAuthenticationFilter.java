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
public class DriverJwtAuthenticationFilter extends OncePerRequestFilter {

    private final String dmSecretKey;
    private final String header;
    private final String prefix;
    private RememberMeServices rememberMeServices = new NullRememberMeServices();
    private DriverAuthenticationManager authenticationManager;

    public DriverJwtAuthenticationFilter(@Value("${driver.management.service.key}") String secretKey,
                                         @Autowired DriverAuthenticationManager authenticationManager){

        this.dmSecretKey=secretKey;
        this.header="Authorization";
        this.prefix="Bearer ";
        this.authenticationManager=authenticationManager;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("Driver Management Service Authorization");
        if (!SecurityUtils.containsJwtToken(request,header,prefix)){
            log.info("Filter called but no JWTToken detected");
            handleError(request,response,filterChain);
            return;
        }
        try {

            Claims claims = SecurityUtils.validateJWT(request,dmSecretKey,header,prefix);

            if (claims==null)
                throw new MalformedJwtException("Invalid JWT");

            if (!SecurityUtils.containsRequiredValues(claims)){
                log.info("Missing Claims");
                handleError(request,response,filterChain);
                return;
            }
            if (!SecurityUtils.validIssuerAndAudience(claims,"driver-management-service")){
                log.info("Bad identity");
                handleError(request,response,filterChain);
                return;
            }

            Authentication authentication = SecurityUtils.wrapAuthenticationTokenFromClaims(claims);
            if (authentication==null){
                throw new MalformedJwtException("Bad Claims");
            }
            log.info("Authentication...");
            authentication = authenticationManager.authenticate(authentication);
            log.info("Successful authentication");
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            log.info("RememberMeServices login success");
            this.rememberMeServices.loginSuccess(request,response,authentication);

        }catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException e){
            log.warn("Exception throw: "+e.getMessage());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.sendError(HttpServletResponse.SC_FORBIDDEN,e.getMessage());
            return;

        }catch (AuthenticationException e){
            log.warn("User not registered on database");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.sendError(HttpServletResponse.SC_NOT_FOUND,e.getMessage());
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
