package org.moviereservationapi.user.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.moviereservationapi.user.dto.KeycloakUserDto;
import org.moviereservationapi.user.exception.UnsupportedAuthTypeException;
import org.moviereservationapi.user.feign.KeycloakClient;
import org.moviereservationapi.user.model.AppUser;
import org.moviereservationapi.user.repository.AppUserRepository;
import org.moviereservationapi.user.service.IUserService;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements IUserService {

    private final KeycloakClient keycloakClient;
    private final AppUserRepository appUserRepository;

    @Override
    @Transactional
    public void addKeycloakUserToDb() {
        String userId = getUserIdFromToken();
        if (userId == null || userId.isEmpty()) {
            throw new AuthorizationDeniedException("Unauthorized, log in again.");
        }

        KeycloakUserDto userDto = keycloakClient.getUserById("movie-app" ,userId); // save it to env.

        AppUser appUser = AppUser.builder()
                .id(userDto.getId())
                .email(userDto.getEmail())
                .build();

        appUserRepository.save(appUser);
    }

    private String getUserIdFromToken() {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth instanceof JwtAuthenticationToken jwtAuthToken) {
            Jwt jwt = jwtAuthToken.getToken();
            return jwt.getClaimAsString("sub");
        }

        throw new UnsupportedAuthTypeException("Unsupported authentication type: " + auth.getClass());
    }
}
