package com.example.emptySaver.config;

import com.example.emptySaver.utils.MyCustomKeyGenerator;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(){
        return new ConcurrentMapCacheManager();
    }
    @Bean("myCustomKeyGenerator")
    public KeyGenerator keyGenerator(){
        return new MyCustomKeyGenerator();
    }
}
