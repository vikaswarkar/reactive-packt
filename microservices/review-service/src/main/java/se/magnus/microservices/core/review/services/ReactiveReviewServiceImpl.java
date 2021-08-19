package se.magnus.microservices.core.review.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import se.magnus.api.core.review.ReactiveReviewService;
import se.magnus.api.core.review.Review;
import se.magnus.microservices.core.review.persistence.ReviewEntity;
import se.magnus.microservices.core.review.persistence.ReviewRepository;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.http.ServiceUtil;

import java.util.List;

@RestController
@Slf4j
public class ReactiveReviewServiceImpl implements ReactiveReviewService {

    private final ReviewRepository repository;
    private final ReviewMapper mapper;
    private final ServiceUtil serviceUtil;
    private final Scheduler scheduler;
    public ReactiveReviewServiceImpl(ReviewRepository repository, ReviewMapper mapper, ServiceUtil serviceUtil, Scheduler scheduler) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
        this.scheduler = scheduler;
    }

    @Override
    public Flux<Review> getReviews(int productId) {
        if (productId < 1 ) throw new InvalidInputException("Invalid ProductId:" + productId);
        return asyncFlux(getByProductId(productId));
    }

    protected List<Review> getByProductId(int productId){
        List<ReviewEntity> reviewEntities = repository.findByProductId(productId);
        List<Review> reviews = mapper.entityListToApiList(reviewEntities);
        reviews.forEach(x -> x.setServiceAddress(serviceUtil.getServiceAddress()));
        return reviews;
    }

    @Override
    public void deleteReviews(int productId) {
        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);
        log.info("Deleting all the reviews for " + productId);
        repository.deleteAll(repository.findByProductId(productId));
    }

    @Override
    public Mono<Review> createReview(Review review) {
        if (review.getProductId() < 1) throw new InvalidInputException("Invalid ProdutId: " + review.getProductId());
        try{
            ReviewEntity reviewEntity = mapper.apiToEntity(review);
            repository.save(reviewEntity);
            log.debug("Created Review: created a review entity {}/{} ", review.getProductId(), review.getReviewId());
            return Mono.just(mapper.entityToApi(reviewEntity));
        }catch (DataIntegrityViolationException dive){
            throw new InvalidInputException(String.format("Duplicate Key, Product Id: %s, Review Id: %s", review.getProductId(), review.getReviewId()));
        }
    }

    private <T> Flux<T> asyncFlux(Iterable<T> iterable){
        return Flux.fromIterable(iterable).publishOn(scheduler);
    }
}
