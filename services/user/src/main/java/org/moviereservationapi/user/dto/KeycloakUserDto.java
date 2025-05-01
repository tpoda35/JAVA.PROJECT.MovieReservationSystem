package org.moviereservationapi.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class KeycloakUserDto {

    private String id;
    private String email;

}
