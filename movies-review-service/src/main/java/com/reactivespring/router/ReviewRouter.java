package com.reactivespring.router;

import com.reactivespring.handler.ReviewHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.*;

@Configuration
public class ReviewRouter {

    @Bean
    public RouterFunction<ServerResponse> reviewRoute(ReviewHandler reviewHandler) {
        return RouterFunctions.route()
                .nest(RequestPredicates.path("/v1/reviews"), builder -> {
                    builder.GET("", request -> reviewHandler.getReview(request))
                            .POST("", request -> reviewHandler.addReview(request))
                            .PUT("/{id}", request -> reviewHandler.updateReview(request))
                            .DELETE("/{id}", request -> reviewHandler.deleteReview(request))
                            .GET("/stream", request -> reviewHandler.getReviewStream());

                })
                .GET("/v1/helloworld", request -> ServerResponse.ok().bodyValue("helloworld"))
                //.POST("/v1/reviews", request -> reviewHandler.addReview(request))
                //.GET("/v1/reviews", request -> reviewHandler.getReview(request))
                .build();
    }
}
