package org.moviereservationapi.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.moviereservationapi.user.dto.AppUserDto;
import org.moviereservationapi.user.service.IUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final IUserService userService;

    @GetMapping("/getUsers")
    public CompletableFuture<List<AppUserDto>> getUsers(
            @RequestParam List<Long> userIds
    ) {
        log.info("api/users/getUser/userId :: Endpoint called. (userIds:{})", userIds);
        return userService.getUsers(userIds);
    }
}
