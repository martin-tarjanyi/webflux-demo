package com.example.webfluxdemo.repository;

import com.example.webfluxdemo.model.Person;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface MongoRepository extends ReactiveCrudRepository<Person, Integer>
{
    Mono<Person> findById(int id);
}
