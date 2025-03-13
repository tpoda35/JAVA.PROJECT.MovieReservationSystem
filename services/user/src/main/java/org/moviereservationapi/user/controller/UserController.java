package org.moviereservationapi.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.moviereservationapi.user.model.AppUser;
import org.moviereservationapi.user.service.IUserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final IUserService userService;

    @GetMapping("/getUser/{userid}")
    public CompletableFuture<AppUser> getUser(
            @PathVariable("userid") Long userId
    ) {
        log.info("api/users/getUser/userId :: Endpoint called. (userId:{})", userId);
        return userService.getUser(userId);
    }
}
