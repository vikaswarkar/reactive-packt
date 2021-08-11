package se.magnus.microservices.core.recommendation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("se.magnus")
public class ReactiveRecommendationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReactiveRecommendationServiceApplication.class, args);
	}
}
