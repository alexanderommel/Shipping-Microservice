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
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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

    public void showHeaders(HttpServletRequest request){
        log.info("Current Headers on this Request");
        Enumeration<String> headers = request.getHeaderNames();
        while (headers.hasMoreElements()){
            String header = headers.nextElement();
            log.info("Header: "+header);
            log.info("Value: "+request.getHeader(header));
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("Driver Management Service Authorization");
        Authentication authentication1 = SecurityContextHolder.getContext().getAuthentication();
        if (authentication1 != null) {
            log.info("Stopping since there's a no null SecurityContext");
            filterChain.doFilter(request,response);
            return;
        }

        Boolean hasJwtParameter = Boolean.FALSE;
        //showHeaders(request);
        String jwtToken = request.getParameter("jwtToken");

        if (jwtToken!=null){
            log.info("Jwt Parameter found!");
            String jwt = jwtToken.substring(7);
            jwtToken = "Bearer "+jwt;
            log.info("Token found is: "+jwtToken);
            hasJwtParameter=Boolean.TRUE;
        }
        if (!hasJwtParameter){
            if (!SecurityUtils.containsJwtToken(request,header,prefix)){
                log.info("Filter called but no JWTToken detected");
                filterChain.doFilter(request,response);
                return;
            }
        }

        try {

            Claims claims;

            if (hasJwtParameter){
                claims = SecurityUtils.validateJwtFromPlainString(jwtToken,dmSecretKey,header,prefix);
            }else {
                claims = SecurityUtils.validateJWT(request,dmSecretKey,header,prefix);
            }

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
            log.info(String.valueOf(authentication==null));
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
        //SecurityContextHolder.clearContext();
        //this.rememberMeServices.loginFail(request,response);
        filterChain.doFilter(request,response);
    }

}
