package com.learnreactiveprogramming.service;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.List;

class FluxAndMonoGeneratorServiceTest {

    FluxAndMonoGeneratorService fluxAndMonoGeneratorService = new FluxAndMonoGeneratorService();

    @Test
    void name() {
        var namesFlux = fluxAndMonoGeneratorService.nameFlux();

        StepVerifier.create(namesFlux)
                .expectNext("a", "b", "c")
                .verifyComplete();
    }

    @Test
    void nameFlux_map() {

        var namesFlux = fluxAndMonoGeneratorService.nameFlux_map();

        StepVerifier.create(namesFlux)
                .expectNext("S","K", "Y", "K", "A", "K")
                .verifyComplete();
    }

    @Test
    void nameFluxFlatMap_Async() {
        var namesFlux = fluxAndMonoGeneratorService.nameFluxFlatMap_Async();

        StepVerifier.create(namesFlux)
                .expectNextCount(17)
                .verifyComplete();

    }

    @Test
    void nameFluxConcatMap() {
        var namesFlux = fluxAndMonoGeneratorService.nameFluxConcatMap();

        StepVerifier.create(namesFlux)
                .expectNext("A","B", "C", "D", "E", "F")
                .verifyComplete();

    }

    @Test
    void namesMono_flatmap() {
        var namesMono = fluxAndMonoGeneratorService.namesMono_flatmap();
        StepVerifier.create(namesMono)
                .expectNext(List.of("A", "L", "E", "X"))
                .verifyComplete();
    }

    @Test
    void namesMono_flatmapMany() {
        var namesMono = fluxAndMonoGeneratorService.namesMono_flatmapMany();
        StepVerifier.create(namesMono)
                .expectNext("A", "L", "E", "X")
                .verifyComplete();
    }

    @Test
    void namesFlux_Transform() {
        var namesMono = fluxAndMonoGeneratorService.namesFlux_Transform();
        StepVerifier.create(namesMono)
                .expectNext("S","K", "Y", "K", "A", "K")
                .verifyComplete();

    }

    @Test
    void nameFlux_filter_with_default() {
        var namesMono = fluxAndMonoGeneratorService.nameFlux_filter_with_default();
        StepVerifier.create(namesMono)
                .expectNext("dd")
                .verifyComplete();

    }

    @Test
    void nameFlux_switch() {
        var namesMono = fluxAndMonoGeneratorService.nameFlux_switch();
        StepVerifier.create(namesMono)
                .expectNext("DEFAULT")
                .verifyComplete();
    }

    @Test
    void expolore_concat() {

        var flux = fluxAndMonoGeneratorService.expolore_concat();
        StepVerifier.create(flux)
                .expectNext("A", "B", "C", "D", "E")
                .verifyComplete();
    }

    @Test
    void exploreConcat_Mono() {
        var flux = fluxAndMonoGeneratorService.exploreConcat_Mono();
        StepVerifier.create(flux)
                .expectNext("a", "b")
                .verifyComplete();
    }

    @Test
    void expolore_merge() {

        var flux = fluxAndMonoGeneratorService.expolore_merge();
        StepVerifier.create(flux)
                .expectNext("A", "C", "B", "D")
                .verifyComplete();
    }

    @Test
    void expolore_mergesequential() {

        var flux = fluxAndMonoGeneratorService.expolore_mergesequential();
        StepVerifier.create(flux)
                .expectNext("A", "B", "C", "D")
                .verifyComplete();
    }

    @Test
    void explore_zip() {

        var flux = fluxAndMonoGeneratorService.explore_zip();
        StepVerifier.create(flux)
                .expectNext("AC", "BD")
                .verifyComplete();
    }

    @Test
    void explore_zip_1() {

        var flux = fluxAndMonoGeneratorService.explore_zip_1();
        StepVerifier.create(flux)
                .expectNext("ACE", "BDF")
                .verifyComplete();
    }


    @Test
    void explorezip_mono() {

        var flux = fluxAndMonoGeneratorService.explorezip_Mono();
        StepVerifier.create(flux)
                .expectNext("ab")
                .verifyComplete();
    }
}