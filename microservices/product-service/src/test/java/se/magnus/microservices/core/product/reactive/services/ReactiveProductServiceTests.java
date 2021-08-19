package se.magnus.microservices.core.product.reactive.services;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.magnus.api.core.product.Product;
import se.magnus.microservices.core.product.reactive.ReactiveProductServiceApplication;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static reactor.core.publisher.Mono.just;

@SpringBootTest(webEnvironment=RANDOM_PORT,
		classes = {ReactiveProductServiceApplication.class},
		properties = {"eureka.client.enabled=false",
				"spring.main.allow-bean-definition-overriding=true", "spring.cloud.config.enabled=false"})
@ExtendWith(SpringExtension.class)
public class ReactiveProductServiceTests {

	private static final int PRODUCT_ID_OK = 1;
	private static final int PRODUCT_ID_NOT_FOUND = 2;
	private static final int PRODUCT_ID_INVALID = 3;

    @Autowired
    private WebTestClient client;

//	@Test
	public void contextLoads() {
	}

//	@Test
	public void createProduct() {
		Product product = new Product(PRODUCT_ID_OK, "name", 1, null);
		postAndVerifyProduct(product, HttpStatus.OK);
	}

	private void postAndVerifyProduct(Product product, HttpStatus expectedStatus){
		client.post()
				.uri("/products")
				.body(just(product), Product.class)
//				.accept(MediaType.APPLICATION_JSON_VALUE)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectBody();
	}
}
