package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.customer;

@Repository
public interface customerRepo extends MongoRepository <customer, String>{
    Optional <customer> findByRUC(String ruc);
}
