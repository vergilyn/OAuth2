package com.vergilyn.samples.oauth2.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * @author VergiLyn
 * @blog http://www.cnblogs.com/VergiLyn/
 * @date 2017/10/23
 */
@SpringBootApplication
@EnableCaching
public class OAuth2ServerApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(OAuth2ServerApplication.class);
        application.setAdditionalProfiles("server","redis");
        application.run(args);
    }

}
