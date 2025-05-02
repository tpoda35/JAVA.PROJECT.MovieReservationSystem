package com.moviereservationapi.reservation.feign;

import com.moviereservationapi.reservation.dto.feign.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "user-service", url = "http://localhost:8096")
public interface UserClient {

    @PostMapping("/api/users/feign")
    UserDto getLoggedInUser();

    @PostMapping("/api/users/feign/{reservationId}")
    void addReservationToUser(
            @PathVariable("reservationId") Long reservationId
    );

}
