package com.reactivespring.controller;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Sinks;

public class SinksTest {

    @Test
    void sink() {

        //given
        Sinks.Many<Integer> replaySink = Sinks.many().unicast().onBackpressureBuffer();

        //when
        replaySink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        replaySink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);


        //then
        replaySink.asFlux()
                .subscribe(integer -> System.out.println("Subscriber 1 " + integer));


        replaySink.asFlux()
                .subscribe(integer -> System.out.println("Subscriber 2 " + integer));

        replaySink.tryEmitNext(3);
    }

    @Test
    void sink_multicast() {

        Sinks.Many<Object> multicast = Sinks.many().multicast().onBackpressureBuffer();


        multicast.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        multicast.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        multicast.asFlux()
                .subscribe(o -> System.out.println("Subscriber 1: "+o));

        multicast.asFlux()
                .subscribe(o -> System.out.println("Subscriber 2: "+o));

        multicast.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST);
    }


    @Test
    void sink_unicast() {

        Sinks.Many<Object> multicast = Sinks.many().unicast().onBackpressureBuffer();


        multicast.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        multicast.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        multicast.asFlux()
                .subscribe(o -> System.out.println("Subscriber 1: "+o));

        multicast.asFlux()
                .subscribe(o -> System.out.println("Subscriber 2: "+o));

        multicast.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST);
    }
}
