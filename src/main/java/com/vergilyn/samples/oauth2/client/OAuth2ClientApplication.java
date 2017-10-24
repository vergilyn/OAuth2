package com.vergilyn.samples.oauth2.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author VergiLyn
 * @blog http://www.cnblogs.com/VergiLyn/
 * @date 2017/10/23
 */
@SpringBootApplication
public class OAuth2ClientApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(OAuth2ClientApplication.class);
        application.setAdditionalProfiles("client");
        application.run(args);
    }

}
