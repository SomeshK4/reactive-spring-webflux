package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MoviesInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = MoviesInfoController.class)
@AutoConfigureWebTestClient
public class MoviesInfoControllerUnitTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private MoviesInfoService moviesInfoService;


    @Test
    void getAllMoviesInfo() {

        var movieinfos = List.of(new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        when(moviesInfoService.getAllMoviesInfo()).thenReturn(Flux.fromIterable(movieinfos));

        webTestClient
                .get()
                .uri("/v1/movieinfos")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void getMovieInfoById() {

        var movieInfoId = "abc";

        var movieInfo = new MovieInfo(null, "Dark Knight Rises", 2005, List.of("Michael Cane"), LocalDate.parse("2005-06-15"));

        when(moviesInfoService.getMovieInfoById(movieInfoId)).thenReturn(Mono.just(movieInfo));

        webTestClient
                .get()
                .uri("/v1/movieinfos"+"/{id}", movieInfoId)
                .exchange()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Dark Knight Rises");
    }

    @Test
    void addMovieInfo() {

        var movieInfo = new MovieInfo(null, "Batman begins1", 2005, List.of("Michael Cane"), LocalDate.parse("2005-06-15"));

        when(moviesInfoService.addMovieInfo(isA(MovieInfo.class))).thenReturn(Mono.just(
                new MovieInfo("mockId", "Batman begins1", 2005, List.of("Michael Cane"), LocalDate.parse("2005-06-15")))
        );

        webTestClient
                .post()
                .uri("/v1/movieinfos")
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assertNotNull(savedMovieInfo);
                    assertNotNull(savedMovieInfo.getMovieInfoId());
                    assertEquals("mockId", savedMovieInfo.getMovieInfoId());
                });
    }

    @Test
    void updateMovieInfo() {

        var id = "abc";
        var movieInfo = new MovieInfo(null, "Dark Knight Rises", 2005, List.of("Michael Cane"), LocalDate.parse("2005-06-15"));

        when(moviesInfoService.updateMovieInfo(isA(MovieInfo.class), isA(String.class))).thenReturn(Mono.just(
                new MovieInfo(id, "Dark Knight Rises", 2005, List.of("Michael Cane"), LocalDate.parse("2005-06-15")))
        );

        webTestClient
                .put()
                .uri("/v1/movieinfos"+"/{id}", id)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assert savedMovieInfo!=null;
                    assert savedMovieInfo.getMovieInfoId()!=null;
                    assertEquals("Dark Knight Rises", savedMovieInfo.getName());
                });
    }

    @Test
    void deleteMovieInfo() {

        var id = "abc";

        when(moviesInfoService.deleteMovieInfo(anyString())).thenReturn(Mono.empty());

        webTestClient
                .delete()
                .uri("/v1/movieinfos"+"/{id}", id)
                .exchange()
                .expectStatus()
                .isNoContent()
                .expectBody()
                .isEmpty();
    }

    @Test
    void addMovieInfo_validation() {

        var movieInfo = new MovieInfo(null, "", -2005, List.of(""), LocalDate.parse("2005-06-15"));

        webTestClient
                .post()
                .uri("/v1/movieinfos")
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult -> {
                    var responseBody = stringEntityExchangeResult.getResponseBody();
                    var expectedErrorMessage = "movieInfo.cast must be present,movieInfo.name must be present,movieInfo.year must be a positive value";
                    assertEquals(expectedErrorMessage, responseBody);
                })
                /**.expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assertNotNull(savedMovieInfo);
                    assertNotNull(savedMovieInfo.getMovieInfoId());
                    assertEquals("mockId", savedMovieInfo.getMovieInfoId());
                })**/;
    }



}
