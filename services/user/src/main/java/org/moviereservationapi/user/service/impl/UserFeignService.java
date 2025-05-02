package org.moviereservationapi.user.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.moviereservationapi.user.model.AppUser;
import org.moviereservationapi.user.repository.AppUserRepository;
import org.moviereservationapi.user.service.IUserFeignService;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserFeignService implements IUserFeignService {

    private final AppUserRepository appUserRepository;

    @Override
    @Transactional
    public AppUser getLoggedInUser() {
        String LOG_PREFIX = "getLoggedInUser";

        Map<String, Object> claims = getClaimsFromJwt();
        String userId = (String) claims.get("sub");
        String userMail = (String) claims.get("email");

        // maybe add user validation check with the keycloak admin rest api.
        return appUserRepository.findById(userId)
                        .orElseGet(() -> {
                            log.warn("{} :: User with the id {}, not found.", LOG_PREFIX, userId);
                            return appUserRepository.save(
                                    AppUser.builder()
                                            .id(userId)
                                            .email(userMail)
                                            .build());
                        });
    }

    @Override
    @Transactional
    public void addReservationToUser(Long reservationId) {
        String LOG_PREFIX = "addReservationToUser";

        AppUser appUser = getLoggedInUser();
        log.info("{} :: Adding reservation with the id {} to the user with the id {}.",
                LOG_PREFIX, reservationId, appUser.getId()
        );

        appUser.getReservationIds().add(reservationId);
        appUserRepository.save(appUser);
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
