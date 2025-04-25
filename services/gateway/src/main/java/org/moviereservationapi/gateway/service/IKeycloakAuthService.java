package org.moviereservationapi.gateway.service;

import reactor.core.publisher.Mono;

import java.util.Map;

public interface IKeycloakAuthService {
    Mono<Map<String, Object>> getToken(String username, String password);
}
