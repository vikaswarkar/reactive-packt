package se.magnus.microservices.composite.product.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.product.ReactiveProductService;
import se.magnus.api.core.recommendation.ReactiveRecommendationService;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.review.ReactiveReviewService;
import se.magnus.api.core.review.Review;

import static reactor.core.publisher.Mono.just;

@Component
@Slf4j
public class ReactiveProductCompositeIntegration
		implements ReactiveProductService, ReactiveRecommendationService, ReactiveReviewService {

	private final WebClient.Builder webClientBuilder;
    private WebClient webClient;
    private final ExceptionHelper exceptionHelper;

    private String productServiceUrl = "http://product/products";
    private String recommendationServiceUrl = "http://recommendation/recommendations?productId=";
    private String reviewServiceUrl = "http://review/reviews?productId=";

	@Autowired
    public ReactiveProductCompositeIntegration (
        WebClient.Builder webClientBuilder,
		ExceptionHelper exceptionHelper,
        @Value("${app.product-service.host}") String productServiceHost,
        @Value("${app.product-service.port}") int    productServicePort,

        @Value("${app.recommendation-service.host}") String recommendationServiceHost,
        @Value("${app.recommendation-service.port}") int    recommendationServicePort,

        @Value("${app.review-service.host}") String reviewServiceHost,
        @Value("${app.review-service.port}") int    reviewServicePort
    ) {
		this.webClientBuilder = webClientBuilder;
//        this.webClient = webClientBuilder.build();
        this.exceptionHelper = exceptionHelper;

        productServiceUrl        = "http://" + productServiceHost + ":" + productServicePort + "/products/";
        recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort + "/recommendations?productId=";
        reviewServiceUrl         = "http://" + reviewServiceHost + ":" + reviewServicePort + "/reviews?productId=";

		String productServiceUrl = "http://product/products";
		String recommendationServiceUrl = "http://recommendation/recommendations?productId=";
		String reviewServiceUrl = "http://review/reviews?productId=";
    }

	private WebClient getWebClient(){
		if (webClient == null){
			this.webClient = webClientBuilder.build();
		}
		return webClient;
	}

	@Override
	public Mono<Product> createProduct(Product body) {
		log.debug("Will post a new product to the URL {} ", this.productServiceUrl);
		return getWebClient().post().uri(productServiceUrl)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.body(Mono.just(body), Product.class)
				.retrieve()
				.bodyToMono(Product.class).log()
				.onErrorMap(WebClientResponseException.class, ex->handleException(ex));
	}
	
	@Override
    public Mono<Product> getProduct(int productId) {
            String url = productServiceUrl + "/" + productId;
            log.debug("Will call getProduct API on URL: {}", url);

            return getWebClient().get().uri(url).retrieve().bodyToMono(Product.class).log()
					.onErrorMap(WebClientResponseException.class, ex-> handleException(ex));
    }
    
	public Mono<Void> deleteProduct(int productId) {
		String url = this.productServiceUrl + "/" + productId;
		log.debug("Will call delete api on {} ", url);
		return getWebClient().delete().uri(url).retrieve().bodyToMono(Void.class).log()
				.flatMap(resp->{
					System.out.println(resp);
					return  Mono.empty();
				});
	}

	@Override
	public Mono<Recommendation> createRecommendation(Recommendation body) {
		String url = this.recommendationServiceUrl + body.getProductId();
		log.debug("Will post a new recommendation to the new URL {} ", url);
		return getWebClient().post().uri(url)
//				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.body(Mono.just(body), Recommendation.class)
			.retrieve()
			.bodyToMono(Recommendation.class).log().onErrorMap(WebClientResponseException.class, ex-> handleException(ex));
	}
	
    public Flux<Recommendation> getRecommendations(int productId) {
		String url = recommendationServiceUrl + productId;
		log.debug("Will call getRecommendations API on URL: {}", url);
		return getWebClient().get().uri(url)
				.retrieve()
				.bodyToFlux(Recommendation.class)
				.log()
				.onErrorMap(WebClientResponseException.class, ex-> handleException(ex));
    }

    @Override
	public void deleteRecommendations(int productId) {
		String url = this.recommendationServiceUrl + productId;
		log.debug("Will call the delete recommendations api on {}", url);
		getWebClient().delete().uri(url).retrieve().bodyToMono(Void.class)
				.onErrorMap(WebClientResponseException.class, ex-> handleException(ex));
	}

	@Override
	public Mono<Review> createReview(Review body) {
		String url = this.reviewServiceUrl + body.getProductId();
		log.debug("Will call create review on {}", url);
		return this.getWebClient().post().uri(url)
//			.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.body(Mono.just(body), Review.class)
			.retrieve()
			.bodyToMono(Review.class).log()
				.onErrorMap(WebClientResponseException.class, ex-> handleException(ex));
	}
	
    public Flux<Review> getReviews(int productId) {
		String url = reviewServiceUrl + productId;
		log.debug("Will call getReviews API on URL: {}", url);
		return getWebClient().get().uri(url).retrieve().bodyToFlux(Review.class).log()
				.onErrorMap(WebClientResponseException.class, ex-> handleException(ex));
	}

	public void deleteReviews(int productId) {
		String url = this.reviewServiceUrl + productId;
		log.debug("Will call delete api on {} ", url);
		getWebClient().delete().uri(url).retrieve().bodyToMono(Void.class)
				.onErrorMap(WebClientResponseException.class, ex-> handleException(ex));
	}

	private Throwable handleException(Throwable ex) {
    	if (!(ex instanceof WebClientResponseException)){
    		log.warn(String.format("Got unexpected error: %s. will rethrow it", ex.toString()));
    		return ex;
		}
    	WebClientResponseException wcre = (WebClientResponseException)ex;
    	return exceptionHelper.handleException(wcre, wcre.getStatusCode());
	}

}
