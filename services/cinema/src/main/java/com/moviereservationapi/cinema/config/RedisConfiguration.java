package com.moviereservationapi.cinema.config;

import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RedisConfiguration {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .entryTtl(Duration.ofMinutes(20));

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("seat", cacheConfiguration.entryTtl(Duration.ofDays(1)));
        cacheConfigurations.put("cinema_seats", cacheConfiguration.entryTtl(Duration.ofDays(1)));
        cacheConfigurations.put("cinemas", cacheConfiguration.entryTtl(Duration.ofDays(1)));
        cacheConfigurations.put("cinema", cacheConfiguration.entryTtl(Duration.ofDays(1)));
        cacheConfigurations.put("cinema_rooms", cacheConfiguration.entryTtl(Duration.ofDays(1)));
        cacheConfigurations.put("room", cacheConfiguration.entryTtl(Duration.ofDays(1)));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(cacheConfiguration)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }

}
