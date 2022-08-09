package com.example.demo.entity;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection  ="transaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class transaction {
    @Id
    private String id;
    private Date registerDate;
    private String idProduct;
    private Double amount;
    private Double transactionCommission;
    private boolean flagWithCommission;
    private String idDestinationProduct;
    private String transactionType;
    private String status;
    private Double newDailyBalance; 
    private String idCustomer;
    private String operationStatus;
}
