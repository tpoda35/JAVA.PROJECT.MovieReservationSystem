package com.moviereservationapi.reservation.service.impl;

import com.moviereservationapi.reservation.service.IJwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class JwtService implements IJwtService {

    @Override
    public String getLoggedInUserIdFromJwt() {
        return (String) getClaimsFromJwt().get("sub");
    }

    private Map<String, Object> getClaimsFromJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthorizationDeniedException("Unauthorized, log in again.");
        }

        JwtAuthenticationToken jwtToken = (JwtAuthenticationToken) authentication;
        return jwtToken.getToken().getClaims();
    }

}
