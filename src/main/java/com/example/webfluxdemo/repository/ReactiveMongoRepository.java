package com.example.webfluxdemo.repository;

import com.example.webfluxdemo.model.Person;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ReactiveMongoRepository extends ReactiveCrudRepository<Person, Integer>
{
}
