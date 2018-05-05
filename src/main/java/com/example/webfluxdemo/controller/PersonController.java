package com.example.webfluxdemo.controller;

import com.example.webfluxdemo.model.Person;
import com.example.webfluxdemo.repository.MongoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Set;

@RestController
@Slf4j
public class PersonController
{
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final MongoRepository mongoRepository;

    public PersonController(ReactiveRedisTemplate<String, String> reactiveRedisTemplate, MongoRepository mongoRepository)
    {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
        this.mongoRepository = mongoRepository;
    }

    @GetMapping(value = "/slow")
    public Mono<String> slow()
    {
        return Mono.just("Sloow response...")
                   .delayElement(Duration.ofSeconds(3))
                   .doOnNext(__ -> log.info("Request executed..."));
    }

    @GetMapping("/people")
    public Flux<Person> getPersonsByStringNumber(@RequestParam Set<String> numbers)
    {
        return Flux.fromIterable(numbers)
                   .flatMap(number -> reactiveRedisTemplate.opsForValue().get(number).doOnNext(log::info))
                   .map(Integer::valueOf)
                   .flatMap(id -> mongoRepository.findById(id).doOnNext(person -> log.info(person.toString())))
                   .switchIfEmpty(Mono.error(new IllegalArgumentException("Not found person.")));
    }

    @GetMapping(value = "/people-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Person> getAllAsStream()
    {
        return mongoRepository.findAll().delayElements(Duration.ofMillis(1000));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> response(IllegalArgumentException e)
    {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
}
