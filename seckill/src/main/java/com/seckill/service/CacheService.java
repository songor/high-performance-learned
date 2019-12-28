package com.seckill.service;

public interface CacheService {

    void put(String key, Object value);

    Object get(String key);

}
