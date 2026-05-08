package com.campushub.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@TestConfiguration
public class TestRedisConfig {

    @Bean
    @Primary
    @SuppressWarnings("unchecked")
    public RedisTemplate<String, Object> inMemoryRedisTemplate() {
        Map<String, Object> store = new ConcurrentHashMap<>();
        ValueOperations<String, Object> valueOperations = createValueOperations(store);

        return new RedisTemplate<>() {
            @Override
            public ValueOperations<String, Object> opsForValue() {
                return valueOperations;
            }

            @Override
            public Boolean delete(String key) {
                return store.remove(key) != null;
            }
        };
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
