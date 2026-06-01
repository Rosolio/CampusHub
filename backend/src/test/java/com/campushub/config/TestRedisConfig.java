package com.campushub.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@TestConfiguration
public class TestRedisConfig {

    @Bean
    @Primary
    @SuppressWarnings("unchecked")
    public RedisTemplate<String, Object> inMemoryRedisTemplate() {
        Map<String, Object> store = new ConcurrentHashMap<>();
        ValueOperations<String, Object> valueOperations = createValueOperations(store);

        RedisTemplate<String, Object> template = new RedisTemplate<>() {
            @Override
            public void afterPropertiesSet() {
                // Skip RedisConnectionFactory validation in tests.
            }

            @Override
            public ValueOperations<String, Object> opsForValue() {
                return valueOperations;
            }

            @Override
            public Boolean delete(String key) {
                return store.remove(key) != null;
            }
        };
        template.setKeySerializer(StringRedisSerializer.UTF_8);
        template.setValueSerializer(StringRedisSerializer.UTF_8);
        return template;
    }

    @SuppressWarnings("unchecked")
    private ValueOperations<String, Object> createValueOperations(Map<String, Object> store) {
        InvocationHandler handler = (proxy, method, args) -> handleValueOperation(store, method.getName(), args);
        return (ValueOperations<String, Object>) Proxy.newProxyInstance(
            ValueOperations.class.getClassLoader(),
            new Class<?>[]{ValueOperations.class},
            handler
        );
    }

    private Object handleValueOperation(Map<String, Object> store, String methodName, Object[] args) {
        if ("get".equals(methodName) && args != null && args.length == 1) {
            return store.get(args[0]);
        }
        if ("set".equals(methodName) && args != null && (args.length == 2 || args.length == 4)) {
            store.put((String) args[0], args[1]);
            return null;
        }
        if ("increment".equals(methodName) && args != null && args.length == 1) {
            String key = (String) args[0];
            Object current = store.get(key);
            long value = current instanceof Number ? ((Number) current).longValue() : 0L;
            long next = value + 1;
            store.put(key, next);
            return next;
        }
        if ("getOperations".equals(methodName)) {
            return null;
        }
        if ("toString".equals(methodName)) {
            return "InMemoryValueOperations";
        }
        if ("hashCode".equals(methodName)) {
            return System.identityHashCode(this);
        }
        if ("equals".equals(methodName) && args != null && args.length == 1) {
            return false;
        }
        throw new UnsupportedOperationException("Unsupported Redis value operation in tests: " + methodName);
    }
}
