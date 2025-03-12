package org.moviereservationapi.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.moviereservationapi.user.exception.UserNotFoundException;
import org.moviereservationapi.user.model.AppUser;
import org.moviereservationapi.user.repository.UserRepository;
import org.moviereservationapi.user.service.IUserService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;

    @Override
    public AppUser getUser(Long userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.info("api/users/getUser/userId :: User not found with the id of {}.", userId);
                    return new UserNotFoundException("User not found.");
                });

        log.info("api/users/getUser/userId :: User found.");
        log.info("api/users/getUser/userId :: User data: {}", user);
        return user;
    }

    @Override
    @Async
    public CompletableFuture<List<AppUser>> getUsers(List<Long> userIds) {
        List<AppUser> users = userRepository.findAllById(userIds);
        if (users.isEmpty()) {
            log.info("api/users/getUsers :: Users not found with the id list of {}.", userIds);
            throw new UserNotFoundException("Users not found.");
        }

        log.info("api/users/getUsers :: {} user found.", users.size());
        return CompletableFuture.completedFuture(users);
    }

}
