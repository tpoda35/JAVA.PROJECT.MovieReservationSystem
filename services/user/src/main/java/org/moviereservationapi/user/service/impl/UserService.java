package org.moviereservationapi.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.moviereservationapi.user.exception.UserNotFoundException;
import org.moviereservationapi.user.model.AppUser;
import org.moviereservationapi.user.repository.UserRepository;
import org.moviereservationapi.user.service.IUserService;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final CacheManager cacheManager;
    private final TransactionTemplate transactionTemplate;
    private final ConcurrentHashMap<String, Object> locks = new ConcurrentHashMap<>();

    @Override
    @Async
    public CompletableFuture<AppUser> getUser(Long userId) {
        String cacheKey = String.format("user_%d", userId);
        Cache cache = cacheManager.getCache("user");

        ValueWrapper cachedResult = null;
        if (cache != null && (cachedResult = cache.get(cacheKey)) != null) {
            return CompletableFuture.completedFuture((AppUser) cachedResult.get());
        }

        Object lock = locks.computeIfAbsent(cacheKey,k -> new Object());
        synchronized (lock) {
            if (cache != null && (cachedResult = cache.get(cacheKey)) != null) {
                return CompletableFuture.completedFuture((AppUser) cachedResult.get());
            }

            AppUser user = transactionTemplate.execute(status -> userRepository.findById(userId)
                    .orElseThrow(() -> {
                        log.info("api/users/getUser/userId :: User not found with the id of {}.", userId);
                        return new UserNotFoundException("User not found.");
                    }));

            log.info("api/users/getUser/userId :: User found. Data: {}", user);

            if (cache != null) {
                cache.put(cacheKey, user);
            }

            return CompletableFuture.completedFuture(user);
        }
    }
}
