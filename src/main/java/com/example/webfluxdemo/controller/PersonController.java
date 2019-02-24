package com.example.webfluxdemo.controller;

import com.example.resilience.connector.Connector;
import com.example.resilience.connector.command.http.HttpCommand;
import com.example.resilience.connector.configuration.EndpointConfiguration;
import com.example.resilience.connector.configuration.builder.EndpointConfigurationBuilder;
import com.example.resilience.connector.model.CommandDescriptor;
import com.example.resilience.connector.model.Result;
import com.example.resilience.connector.serialization.Serializers;
import com.example.webfluxdemo.model.Response;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.reactor.bulkhead.operator.BulkheadOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class PersonController
{
    private static final WebClient WEB_CLIENT = WebClient.create();
    private static final Bulkhead BULKHEAD = Bulkhead.of("hello", BulkheadConfig.custom().maxConcurrentCalls(2).build());

    private final Connector connector;

    public PersonController(Connector connector)
    {
        this.connector = connector;
    }

    @GetMapping(value = "/slow")
    public Mono<String> slow()
    {
        return Mono.just("Sloow response...")
                   .delayElement(Duration.ofSeconds(3))
                   .doOnNext(__ -> log.info("Request executed..."));
    }

    @GetMapping(value = "/test")
    public Mono<Response> test()
    {
        EndpointConfiguration configuration = EndpointConfigurationBuilder.anEndpointConfiguration()
                                                                          .withCircuitBreakerBufferSize(10)
                                                                          .withName("slow")
                                                                          .withTimeout(Duration.ofSeconds(5))
                                                                          .withCacheEnabled(true)
                                                                          .withCachePort(2222)
                                                                          .build();

        HttpCommand httpCommand = new HttpCommand(WEB_CLIENT, "http://localhost:8080/slow");

        return Flux.just("hi", "hello", "bye")
                   .doOnNext((a) -> log.info("test endpoint"))
                   .flatMap(a -> connector.execute(
                           new CommandDescriptor<>(configuration, Serializers.STRING_DESERIALIZER, httpCommand)))
                   .map(Result::toString)
                   .collect(Collectors.joining(",\n"))
                   .map(Response::new);
    }

    @GetMapping(value = "/bulkhead")
    public Mono<String> bulkhead()
    {
        return Mono.just("hi")
                   .delayElement(Duration.ofSeconds(1))
                   .transform(BulkheadOperator.of(BULKHEAD))
                   .doFinally(System.out::println);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> response(IllegalArgumentException e)
    {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
}
