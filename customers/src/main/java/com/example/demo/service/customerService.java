package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.customer;
import com.example.demo.repository.customerRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.Set;
import java.util.regex.Pattern;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

@Service
public class customerService {
    @Autowired
	private customerRepository repo;
    @Autowired
    private Validator validator;

    //validaciones
    public static boolean patternMatches(String emailAddress) {
        String regexPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@" 
        + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
        return Pattern.compile(regexPattern)
          .matcher(emailAddress)
          .matches();
    }

    //Servicios
	public Flux<customer> getAll() {
		return repo.findAll();	
	}

    public Mono<String> createCustomer(customer new_customer) {
        String message = "";
        Set<ConstraintViolation<customer>> violations = validator.validate(new_customer);

        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<customer> constraintViolation : violations) {
                sb.append(constraintViolation.getMessage());
            }
            message = sb.toString();
        }else{
            if(!patternMatches(new_customer.getEmail())){
                message =  "Email not valid: " + new_customer.getEmail();
            }else{
                new_customer.setStatus("ACTIVE");
                repo.save(new_customer);
                message =  "Account for " + new_customer.getName() + " Added!";
            }
        }
        return Mono.just(message);
		 
	}
    public Mono<customer> saveCustomer(customer new_doc){
		return repo.save(new_doc);
	}
    public Mono<customer> getById(String id) {
		return repo.findById(id);
	}


}
