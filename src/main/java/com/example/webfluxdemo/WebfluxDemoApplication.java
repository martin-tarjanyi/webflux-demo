package com.example.webfluxdemo;

import com.example.webfluxdemo.model.Person;
import com.example.webfluxdemo.repository.MongoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;

@SpringBootApplication
@Slf4j
public class WebfluxDemoApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(WebfluxDemoApplication.class, args);
    }

    @Bean
    public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(ReactiveRedisConnectionFactory connectionFactory)
    {
        return new ReactiveRedisTemplate<>(connectionFactory, RedisSerializationContext.string());
    }

    @Bean
    public CommandLineRunner initRedis(ReactiveRedisTemplate<String, String> reactiveRedisTemplate)
    {
        return args ->
        {
            reactiveRedisTemplate.keys("*")
                                 .doOnNext(key -> log.info("Found key in DB: " + key))
                                 .flatMap(reactiveRedisTemplate::delete)
                                 .doOnComplete(() -> log.info("Removed found keys."))
                                 .then(insertIntoRedis(reactiveRedisTemplate))
                                 .subscribe(ignored -> log.info("Thread checking message."));
        };
    }

    @Bean
    public CommandLineRunner savePeopleToMongo(MongoRepository mongoRepository)
    {
        return args ->
        {
            mongoRepository.deleteAll().thenMany(mongoRepository.saveAll(Set.of(
                                           new Person(1, "Jack", 45, "Oklahoma"),
                                           new Person(2, "John", 24, "Chicago"),
                                           new Person(6, "Smith", 11, "London"),
                                           new Person(5, "Rose", 39, "Kuala Lumpur"),
                                           new Person(7, "Alexander", 44, "Amsterdam"),
                                           new Person(3, "Jane", 32, "Brian"),
                                           new Person(4, "Brian", 48, "New York"))))
                           .subscribe(person -> log.info("Saved person: " + person));
        };
    }

    private Mono<Boolean> insertIntoRedis(ReactiveRedisTemplate<String, String> reactiveRedisTemplate)
    {
        return reactiveRedisTemplate.opsForValue()
                                    .multiSet(Map.of(
                                            "one", "1",
                                            "two", "2",
                                            "three", "3",
                                            "four", "4",
                                            "six", "6",
                                            "seven", "7",
                                            "five", "5"))
                                    .doOnNext(isSuccess -> log.info("Redis insert success: " + isSuccess));
    }
}
