package com.drool.ruleengine.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id @GeneratedValue
    private Long id;
    private String merchant;
    private String merchantType;
    private double amount;
    private String location;
    private String category;
}