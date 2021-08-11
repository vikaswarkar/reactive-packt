package se.magnus.microservices.core.review.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import se.magnus.api.core.review.Review;

import java.util.List;

@Repository
public interface ReviewRepository extends CrudRepository<ReviewEntity, Integer> {

	public List<ReviewEntity> findByProductId(int productId);

}
