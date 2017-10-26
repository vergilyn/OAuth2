package com.vergilyn.samples.oauth2.sina;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author VergiLyn
 * @blog http://www.cnblogs.com/VergiLyn/
 * @date 2017/10/26
 */
@SpringBootApplication
public class SinaClientApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(SinaClientApplication.class);
        application.setAdditionalProfiles("sina");
        application.run(args);
    }

}