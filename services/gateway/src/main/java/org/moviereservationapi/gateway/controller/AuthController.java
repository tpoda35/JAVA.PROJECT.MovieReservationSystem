package org.moviereservationapi.gateway.controller;

import lombok.RequiredArgsConstructor;
import org.moviereservationapi.gateway.service.IKeycloakAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    // This is just for testing purposes.
    // Change this in production.
    // Flow: create an account at the next link: http://localhost:8080/realms/movie-app/protocol/openid-connect/auth?client_id=movie-api&response_type=code
    // (change the link if u have another config)
    // then you can call this to log in and get the user data.
    private final IKeycloakAuthService keycloakAuthService;

    @PostMapping("/login")
    public Mono<ResponseEntity<Map<String, Object>>> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        return keycloakAuthService.getToken(username, password)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body(Map.of("error", "Failed to authenticate"))));
    }

}
