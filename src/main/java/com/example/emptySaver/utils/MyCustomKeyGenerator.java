package com.example.emptySaver.utils;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

public class MyCustomKeyGenerator implements KeyGenerator {
    @Override
    public Object generate(Object target, Method method, Object... params) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(method.getName());
        keyBuilder.append(SimpleKeyGenerator.generateKey(params));
        return keyBuilder.toString();
    }
}
