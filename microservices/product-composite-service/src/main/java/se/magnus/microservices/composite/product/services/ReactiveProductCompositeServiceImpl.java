package se.magnus.microservices.composite.product.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import se.magnus.api.composite.product.*;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.review.Review;
import se.magnus.util.http.ServiceUtil;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class ReactiveProductCompositeServiceImpl implements ReactiveProductCompositeService {

    private final ReactiveProductCompositeIntegration integration;
    private final ServiceUtil serviceUtil;
    private final SecurityContext nullSC = new SecurityContextImpl();

    public ReactiveProductCompositeServiceImpl(ReactiveProductCompositeIntegration integration, ServiceUtil serviceUtil) {
        this.integration = integration;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Mono<Void> createCompositeProduct(ProductAggregate body) {
        return ReactiveSecurityContextHolder.getContext().doOnSuccess(sc ->
                internalCreateCompositeProduct(sc, body)).then();
    }

    public void internalCreateCompositeProduct(SecurityContext sc, ProductAggregate body) {
        try{
            logAuthorizationInfo(sc);

            log.info(String.format("Creating composite product for ProductId %s", body.getProductId()));
            Product product = new Product(body.getProductId(), body.getName(), body.getWeight(), null);
            integration.createProduct(product);

            if (body.getRecommendations() != null){
                body.getRecommendations().stream().forEach(r -> {
                    Recommendation recommendation = new Recommendation(body.getProductId(), r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent(), null);
                    integration.createRecommendation(recommendation);
                });
            }
            if (body.getReviews() != null){
                body.getReviews().forEach(r -> {
                    Review review = new Review(body.getProductId(), r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent(), null);
                    integration.createReview(review);
                });
            }
            log.info("Created composite product for {}", body.getProductId());
        }catch (RuntimeException ex){
            log.error("Create Composite Product failed {}", body.getProductId());
            throw ex;
        }
    }

    @Override
    public Mono<ProductAggregate> getCompositeProduct(int productId) {
        return Mono.zip(values -> createProductAggregate(
                                (SecurityContext) values[0],
                (Product)values[1], (List<Recommendation>)values[2], (List<Review>) values[3],
                                serviceUtil.getServiceAddress()),
                ReactiveSecurityContextHolder.getContext().defaultIfEmpty(nullSC),
                integration.getProduct(productId),
                integration.getRecommendations(productId).collectList(),
                integration.getReviews(productId).collectList())
                .doOnError(ex->log.error(String.format("get CompositeProduct failed", ex.toString())))
                .log();
    }

    @Override
    public Mono<Void> deleteCompositeProduct(int productId) {
        return ReactiveSecurityContextHolder.getContext().doOnSuccess(
                sc -> internalDeleteCompositeProduct(sc, productId)
        ).then();
    }

    public void internalDeleteCompositeProduct(SecurityContext sc, int productId) {
        try{
            logAuthorizationInfo(sc);
            log.info("Deleting composite product for {}", productId);
            integration.deleteProduct(productId);
            integration.deleteRecommendations(productId);
            integration.deleteReviews(productId);
            log.info("Successfully deleted composite product for {}", productId);
        }catch(RuntimeException ex){
            log.error("Error deleting composite product for {}", productId);
            throw ex;
        }
    }

    private ProductAggregate createProductAggregate(SecurityContext sc, Product product, List<Recommendation> recommendations, List<Review> reviews, String serviceAddress) {
        logAuthorizationInfo(sc);
        return ProductAggregate.builder().productId(product.getProductId())
                .name(product.getName())
                .weight(product.getWeight())
                .serviceAddresses(new ServiceAddresses(serviceAddress, product.getServiceAddress(), reviews.get(0).getServiceAddress(),
                        recommendations.get(0).getServiceAddress()))
                .recommendations((recommendations == null ? null :
                        recommendations.stream().map(
                                r -> new RecommendationSummary(r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent())
                        ).collect(Collectors.toList())
                ))
                .reviews(reviews == null ? null :
                        reviews.stream().map(
                                r -> new ReviewSummary(r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent())
                        ).collect(Collectors.toList())).build();
    }

    private void logAuthorizationInfo(SecurityContext sc){
        if (sc!=null && sc.getAuthentication()!=null && sc.getAuthentication() instanceof JwtAuthenticationToken){
            Jwt jwtToken = ((JwtAuthenticationToken) sc.getAuthentication()).getToken();
            logAuthorizationInfo(jwtToken);
        } else {
            log.warn("No JWT based Authentication supplied, running tests??");
        }
    }

    private void logAuthorizationInfo(Jwt jwt){
        if (jwt == null){
            log.warn("No JWT supplied, running tests??");
        }else {
            if (log.isDebugEnabled()){
                URL issuer = jwt.getIssuer();
                List<String> audience = jwt.getAudience();
                Object subject = jwt.getClaims().get("sub");
                Object scopes = jwt.getClaims().get("scope");
                Object expires = jwt.getClaims().get("exp");

                log.debug("Authorization info: Subject: {}, scopes: {}, expires: {}, issuer: {}, audience: {}",
                        subject, scopes, expires, issuer, audience);
            }
        }
    }
}
