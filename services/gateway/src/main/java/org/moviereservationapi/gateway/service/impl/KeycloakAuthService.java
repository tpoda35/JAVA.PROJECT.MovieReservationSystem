package org.moviereservationapi.gateway.service.impl;

import lombok.RequiredArgsConstructor;
import org.moviereservationapi.gateway.service.IKeycloakAuthService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class KeycloakAuthService implements IKeycloakAuthService {

    private final WebClient webClient;

    // only for testing
    @Override
    public Mono<Map<String, Object>> getToken(String username, String password) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "password");
        formData.add("client_id", "movie-api");
        formData.add("username", username);
        formData.add("password", password);

        return webClient.post()
                .uri("http://localhost:8080/realms/movie-app/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .map(errorBody -> new RuntimeException("Keycloak authentication error: " + errorBody))
                )
                .bodyToMono(new ParameterizedTypeReference<>() {
                });
    }

    @Override
    public Mono<Map<String, Object>> exchangeCodeForTokens(String code) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("code", code);
        formData.add("client_id", "movie-api");

        return webClient.post()
                .uri("http://localhost:8080/realms/movie-app/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .map(errorBody -> new RuntimeException("Keycloak authentication error: " + errorBody))
                )
                .bodyToMono(new ParameterizedTypeReference<>() {
                });
    }


}
