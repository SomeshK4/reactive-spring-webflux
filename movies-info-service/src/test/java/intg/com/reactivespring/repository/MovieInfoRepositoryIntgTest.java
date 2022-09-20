package com.reactivespring.repository;

import com.reactivespring.domain.MovieInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@ActiveProfiles("test")
class MovieInfoRepositoryIntgTest {

    @Autowired
    MovieInfoRepository movieInfoRepository;

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
    void findAll() {
        //given

        //when
        var moviesInfoFlux = movieInfoRepository.findAll().log();

        //then
        StepVerifier.create(moviesInfoFlux)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void findById() {

        var movieInfoMono = movieInfoRepository.findById("abc").log();

        StepVerifier.create(movieInfoMono)
                .assertNext(movieInfo -> {
                    assertEquals("Dark Knight Rises", movieInfo.getName());
                })
                .verifyComplete();
    }

    @Test
    void saveMovieInfo() {

        var movieInfo = new MovieInfo(null, "Batman begins1", 2005, List.of("Michael Cane"), LocalDate.parse("2005-06-15"));

        var moviesInfoMono = movieInfoRepository.save(movieInfo).log();

        StepVerifier.create(moviesInfoMono)
                .assertNext(movieInfo1 -> {
                    assertNotNull(movieInfo1.getMovieInfoId());
                    assertEquals("Batman begins1", movieInfo1.getName());
                })
                .verifyComplete();
    }

    @Test
    void updateMovieInfo() {

         var movieInfo = movieInfoRepository.findById("abc").block();

         movieInfo.setYear(2021);

        var moviesInfoMono = movieInfoRepository.save(movieInfo).log();

        StepVerifier.create(moviesInfoMono)
                .assertNext(movieInfo1 -> {
                    assertNotNull(movieInfo1.getMovieInfoId());
                    assertEquals(2021, movieInfo1.getYear());
                })
                .verifyComplete();
    }

    @Test
    void delete() {
        //given

        //when
         movieInfoRepository.deleteById("abc").block();
        var moviesInfoFlux = movieInfoRepository.findAll().log();

        //then
        StepVerifier.create(moviesInfoFlux)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void findByYear() {

       var byYear = movieInfoRepository.findByYear(2005);

       StepVerifier.create(byYear)
               .expectNextCount(1)
               .verifyComplete();
    }

    @Test
    void findByName() {

        var byName = movieInfoRepository.findByName("Batman Begins");

        StepVerifier.create(byName)
                .expectNextCount(1)
                .verifyComplete();
    }


}