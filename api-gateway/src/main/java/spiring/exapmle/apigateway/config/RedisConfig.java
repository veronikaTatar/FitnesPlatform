package spiring.exapmle.apigateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(2))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaults)
                .withCacheConfiguration("programs", defaults.entryTtl(Duration.ofMinutes(5)))
                .withCacheConfiguration("program", defaults.entryTtl(Duration.ofMinutes(5)))
                .withCacheConfiguration("workouts", defaults)
                .withCacheConfiguration("workout", defaults)
                .withCacheConfiguration("progress", defaults)
                .build();
    }

    @Bean
    public CacheErrorHandler cacheErrorHandler() {
        return new LoggingCacheErrorHandler();
    }

    private static final class LoggingCacheErrorHandler implements CacheErrorHandler {
        private static final Logger log = LoggerFactory.getLogger(LoggingCacheErrorHandler.class);

        @Override
        public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
            log.warn("Redis cache read failed for {}:{}; loading from gRPC", cache.getName(), key, exception);
        }

        @Override
        public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
            log.warn("Redis cache write failed for {}:{}", cache.getName(), key, exception);
        }

        @Override
        public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
            log.warn("Redis cache eviction failed for {}:{}", cache.getName(), key, exception);
        }

        @Override
        public void handleCacheClearError(RuntimeException exception, Cache cache) {
            log.warn("Redis cache clear failed for {}", cache.getName(), exception);
        }
    }
}
