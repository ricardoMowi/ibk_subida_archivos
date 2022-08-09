package com.example.demo.entity;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

import org.springframework.data.annotation.Id;

@Document(collection="customer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class customer {
    @Id
    private String id;

    @NotBlank(message = "Customer type is mandatory")
    private String customerType;

    @NotBlank(message = "Name is mandatory")
    private String name;    

    @NotBlank(message = "Email is mandatory")
    private String email;

    private String lastName;
    private String RUC;
    private String address;
    private String status; 
}
