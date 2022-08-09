package com.example.demo.entity;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "mastervalues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class masterValue {
    @Id
    private String id;
    private Date creationDate;
    private String status;
    private String description;
    private String code;
    private double value; 
    private String masterType;
}
