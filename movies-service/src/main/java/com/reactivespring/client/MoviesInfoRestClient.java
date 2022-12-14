package com.reactivespring.client;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.exception.MoviesInfoClientException;
import com.reactivespring.exception.MoviesInfoServerException;
import com.reactivespring.util.RetryUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class MoviesInfoRestClient {

    private WebClient webClient;

    @Value("${restClient.moviesInfoUrl}")
    private String moviesInfoUrl;

    public MoviesInfoRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<MovieInfo> retreiveMovieInfo(String movieId) {

        var url = moviesInfoUrl.concat("/{id}");

        /**var retrySpec = Retry.fixedDelay(3, Duration.ofSeconds(1))
                .filter(throwable -> throwable instanceof  MoviesInfoServerException)
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> Exceptions.propagate(retrySignal.failure()));**/

       return webClient
                .get()
                .uri(url, movieId)
                .retrieve()
               .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                   log.info("Status code is: {}", clientResponse.statusCode().value());
                   if(clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)) {
                       return Mono.error(new MoviesInfoClientException("Movie with movieInfoId "+movieId+ " doesn't exists.", clientResponse.statusCode().value()));
                   }

                   return  clientResponse.bodyToMono(String.class)
                           .flatMap(responseMessage -> Mono.error(new MoviesInfoClientException(responseMessage, clientResponse.statusCode().value())));
               })
               .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                   log.info("Status code is : {}", clientResponse.statusCode().value());
                   return clientResponse.bodyToMono(String.class)
                           .flatMap(responseMessage -> Mono.error(new MoviesInfoServerException("Server exception in MoviesInfoService "+responseMessage)));
               })
                .bodyToMono(MovieInfo.class)
               //.retry(3)
               .retryWhen(RetryUtil.retrySpec())
                .log();
    }

    public Flux<MovieInfo> retreiveMovieInfoStream() {

        var url = moviesInfoUrl.concat("/stream");

        return webClient
                .get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    log.info("Status code is: {}", clientResponse.statusCode().value());

                    return  clientResponse.bodyToMono(String.class)
                            .flatMap(responseMessage -> Mono.error(new MoviesInfoClientException(responseMessage, clientResponse.statusCode().value())));
                })
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    log.info("Status code is : {}", clientResponse.statusCode().value());
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseMessage -> Mono.error(new MoviesInfoServerException("Server exception in MoviesInfoService "+responseMessage)));
                })
                .bodyToFlux(MovieInfo.class)
                .retryWhen(RetryUtil.retrySpec())
                .log();
    }
}
