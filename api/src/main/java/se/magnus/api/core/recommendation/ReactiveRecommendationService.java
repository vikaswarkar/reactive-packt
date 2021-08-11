package se.magnus.api.core.recommendation;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveRecommendationService {

    @GetMapping(value="/recommendations", produces = "application/json")
    Flux<Recommendation> getRecommendations(@RequestParam(value = "productId", required = true) int productId);

    @PostMapping(value="/recommendations")
    Mono<Recommendation> createRecommendation(@RequestBody Recommendation recommendation);

    @DeleteMapping(value="/recommendations")
    void deleteRecommendations(@RequestParam(value = "productId", required = true)int productId);
}
