package com.campushub.test;

import com.campushub.config.TestRedisConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Import(TestRedisConfig.class)
public abstract class IntegrationTestSupport {
}
