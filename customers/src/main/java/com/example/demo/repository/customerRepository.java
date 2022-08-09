package com.example.demo.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.customer;

@Repository
public interface customerRepository extends ReactiveMongoRepository<customer, String>{

}
