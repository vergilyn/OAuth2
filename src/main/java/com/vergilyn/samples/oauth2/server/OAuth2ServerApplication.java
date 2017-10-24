package com.vergilyn.samples.oauth2.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author VergiLyn
 * @blog http://www.cnblogs.com/VergiLyn/
 * @date 2017/10/23
 */
@SpringBootApplication
@EnableCaching
public class OAuth2ServerApplication {

    @Bean
    StringRedisTemplate template(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(OAuth2ServerApplication.class);
        application.setAdditionalProfiles("server","redis");
        application.run(args);
    }

}
