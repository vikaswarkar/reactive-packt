package se.magnus.springcloud.configserver;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment=RANDOM_PORT, properties = {"spring.profiles.active=native"})

public class ConfigServerApplicationTests {

	@Test
	public void contextLoads() {
	}

}
