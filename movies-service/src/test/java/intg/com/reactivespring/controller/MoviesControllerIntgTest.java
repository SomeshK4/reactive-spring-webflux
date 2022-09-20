package com.reactivespring.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.reactivespring.domain.Movie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 8084)
@TestPropertySource(
        properties = {
                "restClient.moviesInfoUrl=http://localhost:8084/v1/movieinfos",
                "restClient.reviewsUrl=http://localhost:8084/v1/reviews",
        }
)
@ActiveProfiles("test")
class MoviesControllerIntgTest {


        @Autowired
        WebTestClient webTestClient;

        @Test
        void retrieveMovieById() {
              //given
              var movieId = "abc";
              stubFor(get(urlEqualTo("/v1/movieinfos" + "/" + movieId))
                      .willReturn(aResponse()
                              .withHeader("Content-Type", "application/json")
                              .withBodyFile("movieinfo.json"))
              );

              stubFor(get(urlPathEqualTo("/v1/reviews"))
                      .willReturn(aResponse()
                              .withHeader("Content-Type", "application/json")
                              .withBodyFile("reviews.json"))
              );

              webTestClient
                      .get()
                      .uri("/v1/movies/{id}", movieId)
                      .exchange()
                      .expectStatus()
                      .isOk()
                      .expectBody(Movie.class)
                      .consumeWith(movieEntityExchangeResult -> {
                          var responseBody = movieEntityExchangeResult.getResponseBody();
                          assertNotNull(responseBody);
                          assertEquals(2, responseBody.getReviewList().size());
                          assertEquals("Batman Begins", responseBody.getMovieInfo().getName());
                      });
        }


    @Test
    void retrieveMovieById_404() {
        //given
        var movieId = "abc";
        stubFor(get(urlEqualTo("/v1/movieinfos" + "/" + movieId))
                .willReturn(aResponse()
                        .withStatus(404))
        );

        stubFor(get(urlPathEqualTo("/v1/reviews"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("reviews.json"))
        );

        webTestClient
                .get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody(String.class)
                .isEqualTo("Movie with movieInfoId abc doesn't exists.");

        WireMock.verify(1, getRequestedFor(urlEqualTo("/v1/movieinfos" + "/" + movieId)));
    }


    @Test
    void retrieveMovieById_review_404() {
        //given
        var movieId = "abc";
        stubFor(get(urlEqualTo("/v1/movieinfos" + "/" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieinfo.json"))
        );

        stubFor(get(urlPathEqualTo("/v1/reviews"))
                .willReturn(aResponse()
                        .withStatus(404))
        );

        webTestClient
                .get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Movie.class)
                .consumeWith(movieEntityExchangeResult -> {
                    var responseBody = movieEntityExchangeResult.getResponseBody();
                    assertNotNull(responseBody);
                    assertEquals(0, responseBody.getReviewList().size());
                    assertEquals("Batman Begins", responseBody.getMovieInfo().getName());
                });
    }


    @Test
    void retrieveMovieById_5xx() {
        //given
        var movieId = "abc";
        stubFor(get(urlEqualTo("/v1/movieinfos" + "/" + movieId))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("MovieInfo service unavailable"))
        );

        /**stubFor(get(urlPathEqualTo("/v1/reviews"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("reviews.json"))
        );**/

        webTestClient
                .get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(String.class)
                .isEqualTo("Server exception in MoviesInfoService MovieInfo service unavailable");

        WireMock.verify(4, getRequestedFor(urlEqualTo("/v1/movieinfos" + "/" + movieId)));
    }

    @Test
    void retrieveMovieById_reviews_5xx() {
        //given
        var movieId = "abc";
        stubFor(get(urlEqualTo("/v1/movieinfos" + "/" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieinfo.json"))
        );

        stubFor(get(urlPathEqualTo("/v1/reviews"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Review service unavailable"))
        );

        webTestClient
                .get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(String.class)
                .isEqualTo("Server exception in ReviewsService Review service unavailable");

        WireMock.verify(4, getRequestedFor(urlPathMatching("/v1/reviews*")));
    }
}