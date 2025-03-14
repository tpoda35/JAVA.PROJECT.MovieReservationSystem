package org.moviereservationapi.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.moviereservationapi.user.dto.AppUserDto;
import org.moviereservationapi.user.exception.UserNotFoundException;
import org.moviereservationapi.user.model.AppUser;
import org.moviereservationapi.user.repository.UserRepository;
import org.moviereservationapi.user.service.IUserService;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final CacheManager cacheManager;
    private final ConcurrentHashMap<String, Object> locks = new ConcurrentHashMap<>();

    @Override
    @Async
    public CompletableFuture<List<AppUserDto>> getUsers(List<Long> userIds) {
        String cacheKey = String.format("users_%s", userIds.stream()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining("_")));
        Cache cache = cacheManager.getCache("users");

        ValueWrapper cachedResult = null;
        if (cache != null && (cachedResult = cache.get(cacheKey)) != null) {
            return CompletableFuture.completedFuture((List<AppUserDto>) cachedResult.get());
        }

        Object lock = locks.computeIfAbsent(cacheKey,k -> new Object());
        synchronized (lock) {
            try {
                if (cache != null && (cachedResult = cache.get(cacheKey)) != null) {
                    return CompletableFuture.completedFuture((List<AppUserDto>) cachedResult.get());
                }

                List<AppUser> appUsers = userRepository.findAllById(userIds);
                if (appUsers.size() != userIds.size()) {
                    throw new UserNotFoundException("User not found.");
                }

                List<AppUserDto> appUserDtos = appUsers.stream()
                        .filter(Objects::nonNull)
                        .map(user -> new AppUserDto(user.getId(), user.getEmail()))
                        .toList();

                if (cache != null) {
                    cache.put(cacheKey, appUserDtos);
                }
                return CompletableFuture.completedFuture(appUserDtos);
            }
            finally {
                locks.remove(cacheKey);
            }
        }
    }
}
