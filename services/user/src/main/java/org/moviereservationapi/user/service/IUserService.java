package org.moviereservationapi.user.service;

import org.moviereservationapi.user.dto.AppUserDto;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IUserService {
    CompletableFuture<List<AppUserDto>> getUsers(List<Long> userIds);
}
