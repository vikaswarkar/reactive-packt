package se.magnus.microservices.springcloud.gateway;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"eureka.client.enabled=false",
        "spring.main.allow-bean-definition-overriding=true","spring.cloud.config.enabled=false"},
        classes = {GatewayApplication.class,TestSecurityConfig.class}
)
class GatewayApplicationTests {

    @Test
    void contextLoads() {
    }

}
