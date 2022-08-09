package com.example.demo.entity;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class product {
    @Id
    private String id;
    private String customerId;
    private Date creationDate;
    private String transactionDate; 
    private int maximumTransactionLimit;
    private int numberOfFreeTransactions;
    private Double maintenanceCommission;
    private Double amount;
    private String productType;
    private String status;
    private Boolean hasDebt;
    //eWallet
    public String identificationCode;
    public String phoneNumber;
    public String IMEIPhone;
    public String email;
    public Double amountBootCoin;
}
