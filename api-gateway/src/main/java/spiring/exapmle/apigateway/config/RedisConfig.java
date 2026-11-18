package spiring.exapmle.apigateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import spiring.exapmle.apigateway.dto.ProgressDto;
import spiring.exapmle.apigateway.dto.TrainingProgramDto;
import spiring.exapmle.apigateway.dto.WorkoutDto;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableCaching
public class RedisConfig implements CachingConfigurer {
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory, ObjectMapper mapper) {
        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(2))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaults)
                .withCacheConfiguration("programs",
                        listValues(defaults.entryTtl(Duration.ofMinutes(5)), mapper, TrainingProgramDto.class))
                .withCacheConfiguration("program", defaults.entryTtl(Duration.ofMinutes(5)))
                .withCacheConfiguration("workouts", listValues(defaults, mapper, WorkoutDto.class))
                .withCacheConfiguration("workout", defaults)
                .withCacheConfiguration("progress", listValues(defaults, mapper, ProgressDto.class))
                .build();
    }

    private static RedisCacheConfiguration listValues(
            RedisCacheConfiguration config, ObjectMapper mapper, Class<?> elementType) {
        var type = mapper.getTypeFactory().constructCollectionType(List.class, elementType);
        return config.serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new Jackson2JsonRedisSerializer<>(mapper, type)));
    }

    @Bean
    public KeyGenerator listCacheKeyGenerator(ObjectMapper mapper) {
        return (target, method, params) -> mapper.valueToTree(params).toString();
    }

    @Override
    public CacheErrorHandler errorHandler() {
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
