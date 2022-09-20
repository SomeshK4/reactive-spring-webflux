package com.learnreactiveprogramming.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class FluxAndMonoGeneratorService {


    public Flux<String> nameFlux() {
        return Flux.fromIterable(List.of("a", "b", "c"))
                .log();
    }

    public Mono<String> nameMono() {
        return Mono.just("somesh")
                .log();
    }

    public Flux<String> nameFlux_map() {
        return Flux.fromIterable(List.of("sk", "yk", "ak"))
                .map(String::toUpperCase)
                .flatMap(s -> splitString(s))
                .log();
    }


    public Flux<String> nameFlux_filter_with_default() {
        return Flux.fromIterable(List.of("sk", "yk", "ak"))
                .map(String::toUpperCase)
                .filter(s -> s.length() > 2)
                .flatMap(s -> splitString(s))
                .defaultIfEmpty("dd")
                .log();
    }

    public Flux<String> nameFlux_switch() {

        Function<Flux<String>, Flux<String>> transform = name -> name.map(String::toUpperCase)
                .filter(s -> s.length() > 2);

        Flux<String> aDefault = Flux.just("default").transform(transform);


        return Flux.fromIterable(List.of("sk", "yk", "ak"))
                .transform(transform)
                .flatMap(s -> splitString(s))
                .switchIfEmpty(aDefault)
                .log();
    }

    public Mono<List<String>> namesMono_flatmap() {
        return Mono.just("alex")
                .map(String::toUpperCase)
                .flatMap(s -> splitStringMono(s))
                .log();
    }


    public Flux<String> namesMono_flatmapMany() {
        return Mono.just("alex")
                .map(String::toUpperCase)
                .flatMapMany(s -> splitString(s))
                .log();
    }


    public Flux<String> namesFlux_Transform() {
        Function<Flux<String>, Flux<String>> transform = name -> name.map(String::toUpperCase);
        return Flux.fromIterable(List.of("sk", "yk", "ak"))
                .transform(transform)
                .flatMap(s -> splitString(s))
                .log();
    }

    public Mono<List<String>> splitStringMono(String name) {
        var s = name.split("");
        var charList = List.of(s);
        return Mono.just(charList);
    }


    public Flux<String> nameFluxFlatMap_Async() {
        return Flux.fromIterable(List.of("somesh", "deepak", "kiran"))
                .map(String::toUpperCase)
                .flatMap(s -> splitStringWithDelay(s))
                .log();
    }

    public Flux<String> nameFluxConcatMap() {
        return Flux.fromIterable(List.of("ab", "cd", "ef"))
                .map(String::toUpperCase)
                .concatMap(s -> splitStringWithDelay(s))
                .log();
    }


    public Flux<String> splitString(String name) {
        return Flux.fromArray(name.split(""));
    }

    public Flux<String> splitStringWithDelay(String name) {
        var delay = new Random().nextInt(1000);
        return Flux.fromArray(name.split(""))
                .delayElements(Duration.ofMillis(delay));
    }


    public Flux<String> expolore_concat() {
        Flux<String> just = Flux.just("A", "B", "C");
        Flux<String> just1 = Flux.just("D", "E");

        return just.concatWith(just1);

       // return Flux.concat(just, just1);
    }

    public Flux<String> expolore_merge() {
        Flux<String> just = Flux.just("A", "B").delayElements(Duration.ofMillis(100));
        Flux<String> just1 = Flux.just("C", "D").delayElements(Duration.ofMillis(110));

        return just.mergeWith(just1).log();

        // return Flux.concat(just, just1);
    }

    public Flux<String> expolore_mergesequential() {
        Flux<String> just = Flux.just("A", "B").delayElements(Duration.ofMillis(100));
        Flux<String> just1 = Flux.just("C", "D").delayElements(Duration.ofMillis(110));

        return Flux.mergeSequential(just, just1);
    }

    public Flux<String> explore_zip() {
        Flux<String> just = Flux.just("A", "B");
        Flux<String> just1 = Flux.just("C", "D");

        return Flux.zip(just, just1, (j, j1) -> j + j1).log();
    }

    public Flux<String> explore_zip_1() {
        Flux<String> just = Flux.just("A", "B");
        Flux<String> just1 = Flux.just("C", "D");
        Flux<String> just2 = Flux.just("E", "F");

        return Flux.zip(just, just1, just2)
                .map(t3 -> t3.getT1() + t3.getT2() + t3.getT3())
                .log();
    }

    public Flux<String> exploreConcat_Mono() {
        Mono<String> mono = Mono.just("a");
        Mono<String> mono1 = Mono.just("b");

        return mono.concatWith(mono1);
    }


    public Mono<String> explorezip_Mono() {
        Mono<String> mono = Mono.just("a");
        Mono<String> mono1 = Mono.just("b");

        return mono.zipWith(mono1, (m,m1)->m+m1);
    }

    public static void main(String[] args) {
        FluxAndMonoGeneratorService fluxAndMonoGeneratorService = new FluxAndMonoGeneratorService();
        fluxAndMonoGeneratorService.nameFlux()
                .subscribe(name -> System.out.println("Name is "+name));

        fluxAndMonoGeneratorService.nameMono()
                .subscribe(name -> System.out.println("Mono name is "+name));

        fluxAndMonoGeneratorService.nameFlux_map()
                .subscribe(name -> System.out.println("Name is "+ name));
    }
}
