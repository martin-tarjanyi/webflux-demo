package com.example.webfluxdemo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class Person
{
    @Id
    private final int id;
    private final String name;
    private final int age;
    private final String city;
}
