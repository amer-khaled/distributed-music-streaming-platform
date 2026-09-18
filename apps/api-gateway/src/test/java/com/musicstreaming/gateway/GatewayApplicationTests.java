package com.musicstreaming.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false",
        "spring.cloud.gateway.discovery.locator.enabled=false"
})
class GatewayApplicationTests {

    @Test
    void contextLoads() {
    }
}
