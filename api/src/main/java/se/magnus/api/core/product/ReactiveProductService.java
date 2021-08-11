package se.magnus.api.core.product;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

public interface ReactiveProductService {

    /**
     * Sample usage: curl $HOST:$PORT/product/1
     *
     * @param productId
     * @return the product, if found, else null
     */
    @GetMapping(
        value    = "/products/{productId}",
        produces = MediaType.APPLICATION_JSON_VALUE)
     Mono<Product> getProduct(@PathVariable int productId);

    @PostMapping(value="/products", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    Mono<Product> createProduct(@RequestBody Product product);
    
    @DeleteMapping(value = "/products/{productId}")
    Mono<Void> deleteProduct(@PathVariable int productId);
    
}
