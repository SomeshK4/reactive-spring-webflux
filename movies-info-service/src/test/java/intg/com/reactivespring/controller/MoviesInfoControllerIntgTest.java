package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.repository.MovieInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class MoviesInfoControllerIntgTest {

    @Autowired
    MovieInfoRepository movieInfoRepository;

    @Autowired
    WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        var movieinfos = List.of(new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        movieInfoRepository.saveAll(movieinfos)
                .blockLast();
    }

    @AfterEach
    void tearDown() {
        movieInfoRepository.deleteAll().block();
    }

    @Test
    void addMovieInfo() {
        var movieInfo = new MovieInfo(null, "Batman begins1", 2005, List.of("Michael Cane"), LocalDate.parse("2005-06-15"));

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
                    assert savedMovieInfo!=null;
                    assert savedMovieInfo.getMovieInfoId()!=null;
                });
    }

    @Test
    void findAllMovieInfos() {

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
    void getMovieInfoByYear() {

        var uri = UriComponentsBuilder.fromUriString("/v1/movieinfos")
                .queryParam("year", 2005)
                .buildAndExpand().toUri();

        webTestClient
                .get()
                .uri(uri)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(1);
    }

    @Test
    void findMovieInfoById() {

        var id = "abc";

        webTestClient
                .get()
                .uri("/v1/movieinfos"+"/{id}", id)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Dark Knight Rises");
                /**.expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var movieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assertNotNull(movieInfo);
                });**/
    }


    @Test
    void findMovieInfoById_notfound() {

        var id = "def";

        webTestClient
                .get()
                .uri("/v1/movieinfos"+"/{id}", id)
                .exchange()
                .expectStatus()
                .isNotFound();
    }


    @Test
    void updateMovieInfo() {
        var id = "abc";
        var movieInfo = new MovieInfo(null, "Dark Knight Rises1", 2005, List.of("Michael Cane"), LocalDate.parse("2005-06-15"));

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
                    assertEquals("Dark Knight Rises1", savedMovieInfo.getName());
                });
    }


    @Test
    void updateMovieInfo_notfound() {
        var id = "def";
        var movieInfo = new MovieInfo(null, "Dark Knight Rises1", 2005, List.of("Michael Cane"), LocalDate.parse("2005-06-15"));

        webTestClient
                .put()
                .uri("/v1/movieinfos"+"/{id}", id)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isNotFound();
    }


    @Test
    void deleteMovieInfo() {
        var id = "abc";

        webTestClient
                .delete()
                .uri("/v1/movieinfos"+"/{id}", id)
                .exchange()
                .expectStatus()
                .isNoContent()
                .expectBody()
                .isEmpty();
    }
}