package com.musicstreaming.user;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false",
        "grpc.server.port=0"
})
class UserServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
