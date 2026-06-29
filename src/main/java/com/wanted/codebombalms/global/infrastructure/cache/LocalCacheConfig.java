package com.wanted.codebombalms.global.infrastructure.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.List;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class LocalCacheConfig {

    private static final int DEFAULT_MAXIMUM_SIZE = 1_000;
    private static final Duration DEFAULT_EXPIRE_AFTER_WRITE = Duration.ofMinutes(9);
    private static final int LEARNING_MAXIMUM_SIZE = 20_000;
    private static final Duration LEARNING_EXPIRE_AFTER_WRITE = Duration.ofMinutes(10);

    private static final List<String> DEFAULT_CACHE_NAMES = List.of(
            CacheNames.BADGE_IMAGE_ACCESS_URL
    );

    private static final List<String> LEARNING_CACHE_NAMES = List.of(
            CacheNames.LEARNING_ACTIVE_STUDENT_COUNT,
            CacheNames.LEARNING_STUDENT_ID_PAGE,
            CacheNames.LEARNING_COURSE_LECTURE_IDS,
            CacheNames.LEARNING_COURSE_PROBLEM_SET_IDS,
            CacheNames.LEARNING_COMPLETED_LECTURE_COUNTS,
            CacheNames.LEARNING_COMPLETED_PROBLEM_COUNTS
    );

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCacheNames(DEFAULT_CACHE_NAMES);
        cacheManager.setCaffeine(
                Caffeine.newBuilder()
                        .maximumSize(DEFAULT_MAXIMUM_SIZE)
                        .expireAfterWrite(DEFAULT_EXPIRE_AFTER_WRITE)
                        .recordStats()
        );

        LEARNING_CACHE_NAMES.forEach(cacheName -> cacheManager.registerCustomCache(
                cacheName,
                Caffeine.newBuilder()
                        .maximumSize(LEARNING_MAXIMUM_SIZE)
                        .expireAfterWrite(LEARNING_EXPIRE_AFTER_WRITE)
                        .recordStats()
                        .build()
        ));

        return cacheManager;
    }
}
