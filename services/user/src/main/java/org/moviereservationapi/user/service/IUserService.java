package org.moviereservationapi.user.service;

import org.moviereservationapi.user.model.AppUser;

import java.util.concurrent.CompletableFuture;

public interface IUserService {
    CompletableFuture<AppUser> getUser(Long userId);
}
