package com.neu.vansven;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import springfox.documentation.oas.annotations.EnableOpenApi;

@SpringBootApplication
@EnableScheduling
@EnableRedisHttpSession(redisNamespace = "partner:userlogin:session",maxInactiveIntervalInSeconds = 300)
public class PartnerMatchingSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(PartnerMatchingSystemApplication.class, args);
    }

}
