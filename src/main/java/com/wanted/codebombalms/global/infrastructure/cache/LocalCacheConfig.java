package com.wanted.codebombalms.global.infrastructure.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableCaching
public class LocalCacheConfig {

    private static final int DEFAULT_MAXIMUM_SIZE = 1_000;
    private static final Duration DEFAULT_EXPIRE_AFTER_WRITE = Duration.ofMinutes(9);

    private static final List<String> CACHE_NAMES = List.of(
            CacheNames.BADGE_IMAGE_ACCESS_URL
    );

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCacheNames(CACHE_NAMES);
        cacheManager.setCaffeine(
                Caffeine.newBuilder()
                        .maximumSize(DEFAULT_MAXIMUM_SIZE)
                        .expireAfterWrite(DEFAULT_EXPIRE_AFTER_WRITE)
                        .recordStats()
        );

        return cacheManager;
    }
}
