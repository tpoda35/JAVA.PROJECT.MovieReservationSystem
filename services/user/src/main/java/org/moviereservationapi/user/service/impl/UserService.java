package org.moviereservationapi.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.moviereservationapi.user.dto.AppUserDto;
import org.moviereservationapi.user.exception.UserNotFoundException;
import org.moviereservationapi.user.model.AppUser;
import org.moviereservationapi.user.repository.UserRepository;
import org.moviereservationapi.user.service.IUserService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;

    @Override
    public AppUserDto getUser(Long userId) {
        AppUser appUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found."));
        return null;
    }
}
