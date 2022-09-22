package com.reactivespring.handler;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.ReviewDataException;
import com.reactivespring.exception.ReviewNotFoundException;
import com.reactivespring.repository.ReviewReactiveRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.validation.Validator;
import java.util.stream.Collectors;


@Component
@Slf4j
public class ReviewHandler {

    private ReviewReactiveRepository reviewReactiveRepository;

    private Validator validator;

    private Sinks.Many<Review> reviewSink = Sinks.many().replay().latest();

    public ReviewHandler(ReviewReactiveRepository reviewReactiveRepository, Validator validator) {
        this.reviewReactiveRepository = reviewReactiveRepository;
        this.validator = validator;
    }

    public Mono<ServerResponse> addReview(ServerRequest request) {
       return request.bodyToMono(Review.class)
                .doOnNext(this::validate)
                .flatMap(reviewReactiveRepository::save)
               .doOnNext(review -> {
                   reviewSink.tryEmitNext(review);
               })
                .flatMap(ServerResponse.status(HttpStatus.CREATED)::bodyValue);
    }

    private void validate(Review review) {
        var constraintViolations = validator.validate(review);
        log.info("Constraint Violations : {}", constraintViolations);

        if(constraintViolations.size() > 0) {

            var errorMessage = constraintViolations
                    .stream()
                    .map(reviewConstraintViolation -> reviewConstraintViolation.getMessage())
                    .sorted()
                    .collect(Collectors.joining(","));
            throw new ReviewDataException(errorMessage);
        }
    }

    public Mono<ServerResponse> getReview(ServerRequest request) {

        var movieInfoId = request.queryParam("movieInfoId");

        Flux<Review> reviews;
        if (movieInfoId.isPresent()) {
            reviews = reviewReactiveRepository.findByMovieInfoId(Long.valueOf(movieInfoId.get()));

        } else {
            reviews = reviewReactiveRepository.findAll();
        }
        return getResponse(reviews);
    }

    private Mono<ServerResponse> getResponse(Flux<Review> reviews) {
        return ServerResponse.ok().body(reviews, Review.class);
    }

    public Mono<ServerResponse> updateReview(ServerRequest request) {
        var reviewId = request.pathVariable("id");
        var existingReview = reviewReactiveRepository.findById(reviewId)
                .switchIfEmpty(Mono.error(new ReviewNotFoundException("Review not found with reviewId "+reviewId)));

        return existingReview
                .flatMap(review -> request.bodyToMono(Review.class)
                .map(requestReview -> {
                    review.setComment(requestReview.getComment());
                    review.setRating(requestReview.getRating());
                    return review;
                }).flatMap(reviewReactiveRepository::save)
                        .flatMap(savedReview -> ServerResponse.ok().bodyValue(savedReview)));
               // .switchIfEmpty(ServerResponse.notFound().build()); alternate approach of error handling in case of reviewId not found in db
    }

    public Mono<ServerResponse> deleteReview(ServerRequest request) {
        var reviewId = request.pathVariable("id");

        var existingReview = reviewReactiveRepository.findById(reviewId);

        return existingReview
                .flatMap(review -> reviewReactiveRepository.deleteById(reviewId))
                .then(ServerResponse.noContent().build());


    }

    public Mono<ServerResponse> getReviewStream() {

       return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_NDJSON)
                .body(reviewSink.asFlux(), Review.class);

    }
}

