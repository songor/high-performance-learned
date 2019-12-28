package com.seckill.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.seckill.service.CacheService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Service
public class CacheServiceImpl implements CacheService {

    private Cache<String, Object> cache;

    @PostConstruct
    public void init() {
        cache = CacheBuilder.newBuilder()
                .initialCapacity(10)
                .maximumSize(100)
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build();
    }

    @Override
    public void put(String key, Object value) {
        cache.put(key, value);
    }

    @Override
    public Object get(String key) {
        return cache.getIfPresent(key);
    }

}
