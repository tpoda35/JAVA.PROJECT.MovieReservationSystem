package org.moviereservationapi.user.service;

import org.moviereservationapi.user.dto.AppUserDto;

public interface IUserService {
    AppUserDto getUser(Long userId);
}
