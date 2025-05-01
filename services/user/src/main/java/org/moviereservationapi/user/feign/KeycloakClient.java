package org.moviereservationapi.user.feign;

import org.moviereservationapi.user.dto.KeycloakUserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// "${keycloak.admin.base-url}" change this
@FeignClient(
        name = "KeycloakClient",
        url = "http://localhost:8080"
)
public interface KeycloakClient {

    @GetMapping("/admin/realms/{realm}/users/{user-id}")
    KeycloakUserDto getUserById(
            @PathVariable("realm") String realm,
            @PathVariable("user-id") String userId
    );

}
