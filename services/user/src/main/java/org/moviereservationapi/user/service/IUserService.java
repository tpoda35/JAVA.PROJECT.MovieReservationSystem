package org.moviereservationapi.user.service;

import org.moviereservationapi.user.model.AppUser;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IUserService {
    AppUser getUser(Long userId);
    CompletableFuture<List<AppUser>> getUsers(List<Long> userIds);
}
