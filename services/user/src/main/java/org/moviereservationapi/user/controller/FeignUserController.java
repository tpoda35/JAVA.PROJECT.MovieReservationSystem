package org.moviereservationapi.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.moviereservationapi.user.model.AppUser;
import org.moviereservationapi.user.service.IUserFeignService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/feign")
@RequiredArgsConstructor
@Slf4j
public class FeignUserController {

    private final IUserFeignService userService;

    @PostMapping
    public AppUser getLoggedInUser() {
        log.info("getLoggedInUser :: Endpoint called");

        return userService.getLoggedInUser();
    }

    @PostMapping("/{reservationId}")
    public void addReservationToUser(
            @PathVariable("reservationId") Long reservationId
    ) {
        log.info("addReservationToUser :: Endpoint called");

        userService.addReservationToUser(reservationId);
    }

}
